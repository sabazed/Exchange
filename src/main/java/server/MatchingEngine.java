package server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class MatchingEngine implements MessageBusService {

    private final static Logger LOG = LogManager.getLogger(MatchingEngine.class);

    // Use a LinkedBlockingQueue to receive new orders
    private final BlockingQueue<Message> neworders;
    // Keep an Order Book for storing all orders
    private final HashMap<Instrument, OrderBook> orderbooks;
    // Request bus to send responses for frontend
    private final MessageBus requestBus;
    // Thread which will process orders
    private final Thread orderProcessor;
    // Marker if the engine is running or not
    private boolean running;

    // Keep count of orders to give them unique IDs
    private static long ID = 0;

    public MatchingEngine(MessageBus messageBus) {

        neworders = new LinkedBlockingQueue<>();
        orderbooks = new HashMap<>();

        requestBus = messageBus;
        requestBus.registerService(Service.Engine, this);


        orderProcessor = new Thread(this::processOrders);
        running = false;

    }

    public void processMessage(Message message) {
        try {
            if (message instanceof Request) neworders.put(message);
            else {neworders.put(message); } // TODO
        }
        catch (InterruptedException e) {
            // TODO
        }
    }

    public void start() {
        running = true;
        orderProcessor.start();
    }

    public void stop() {
        running = false;
    }

    // Main method that the thread will run on
    private void processOrders() {
        LOG.info("MatchingEngine up and running!");
        // Run endlessly
        while (running) {
            try {
                // Receive order from the queue, if it is empty - wait for it.
                Message request = neworders.take();
                LOG.info("Processing new {}", request);
                Order order = request.getOrder();
                // Check if the instrument exists in the order book and if not, add it
                OrderBook instr = orderbooks.get(order.getInstrument());
                if (instr == null) {
                    orderbooks.put(order.getInstrument(), new OrderBook(order.getInstrument()));
                    instr = orderbooks.get(order.getInstrument());
                }
                // Save trees of both sides as the request's own side tree and opposite tree
                TreeSet<Order> ownTree = order.getSide() == Side.SELL ? instr.getSellOrders() : instr.getBuyOrders();
                TreeSet<Order> otherTree = order.getSide() == Side.SELL ? instr.getBuyOrders() : instr.getSellOrders();
                // Check if the request has Cancel status to remove the order
                if (request.isValid() && request.getStatus().get(0) == Status.Cancel) {
                    // If removal couldn't be done change the status to CancelFail
                    if (!ownTree.remove(order)) {
                        request.setStatus(Status.CancelFail);
                        LOG.warn("Couldn't cancel the current {}", request);
                    }
                    // Send the message through the Request Bus
                    requestBus.sendMessage(Service.Gateway, request);
                }
                else {
                    // Check if all data are valid and if not, skip the iteration and send the error message
                    if (order.getSide() == null || order.getSession() == null || order.getClientId() == null || order.getClientId().isBlank()) {
                        request.setStatus(Status.ListFail);
                        requestBus.sendMessage(Service.Gateway, request);
                        LOG.warn("Invalid data in {}", request);
                    }
                    else {
                        // Check all fields for errors
                        StringBuilder invalids = new StringBuilder();
                        if (order.getUser() == null || order.getUser().isBlank()) {
                            request.setValid(false);
                            request.addErrorCode(Status.InvalidUser);
                            invalids.append("username, ");
                        }
                        if (order.getInstrument() == null || order.getInstrument().getId() == null) {
                            request.setValid(false);
                            request.addErrorCode(Status.InvalidInstr);
                            invalids.append("instrument, ");
                        }
                        if (order.getPrice() == null || order.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
                            request.setValid(false);
                            request.addErrorCode(Status.InvalidPrice);
                            invalids.append("price, ");
                        }
                        if (order.getQty() <= 0) {
                            request.setValid(false);
                            request.addErrorCode(Status.InvalidQty);
                            invalids.append("qty, ");
                        }
                        // If any error exists then log all names and send the request, otherwise continue the loop
                        if (!invalids.isEmpty()) {
                            request.getStatus().remove(0); // Remove the List status
                            requestBus.sendMessage(Service.Gateway, request);
                            LOG.info("Invalid user input fields in {} for {}", invalids.substring(0, invalids.length() - 2), request);
                        }
                        else {
                            // Loop until all available orders are exchanged
                            boolean repeatMatching = true;
                            while (repeatMatching) {
                                repeatMatching = false;
                                // Get the best deal from the tree
                                Iterator<Order> iterator = otherTree.iterator();
                                Order matched = iterator.hasNext() ? iterator.next() : null;
                                // Iterate over the orders if the users are the same
                                while (iterator.hasNext() && matched.getUser().equals(order.getUser()))
                                    matched = iterator.next();
                                // If the users are still same we assign matched as null, or consider two cases:
                                // Case Sell: if matched price is higher or equal else matched = null
                                // Case Buy: if matched price is lower or equal else matched = null
                                if (matched != null && (matched.getUser().equals(order.getUser()) ||
                                    matched.getPrice().compareTo(order.getPrice()) * ((order.getSide() == Side.BUY) ? 1 : -1) > 0)
                                ) matched = null;
                                // If no match was found then add the order
                                if (matched == null) {
                                    order.setGlobalId(ID++);
                                    order.setDateInst(Instant.now());
                                    // If there was a problem while adding then change status flag to OrderFail and decrease ID counter
                                    if (!ownTree.add(order)) {
                                        request.setStatus(Status.OrderFail);
                                        ID--;
                                        LOG.info("Listing request unsuccessful! - {}", request);
                                    }
                                    else {
                                        LOG.info("Listing request successful! - {}", request);
                                    }
                                    requestBus.sendMessage(Service.Gateway, request);
                                } else {
                                    // If currently matched order has more quantity then mark our request as traded
                                    if (matched.getQty() > order.getQty()) {
                                        matched.setQty(matched.getQty() - order.getQty());
                                        request.setStatus(Status.Trade);
                                        order.setGlobalId(ID++);
                                        order.setDateInst(Instant.now());
                                        requestBus.sendMessage(Service.Gateway, request);
                                        LOG.info("Listing request successful after trading - {}", request);
                                    } else {
                                        order.setQty(order.getQty() - matched.getQty());
                                        otherTree.pollFirst();
                                        matched.setQty(0);
                                        // If the order ran out of remaining qty then mark it as traded and send the message
                                        if (order.getQty() <= 0) {
                                            request.setStatus(Status.Trade);
                                            requestBus.sendMessage(Service.Gateway, request);
                                            LOG.info("Request traded successfully - {}", request);
                                        }
                                        // If qty is more than 0 then continue iteration
                                        else repeatMatching = true;
                                    }
                                    // Send trade signal for the matched order
                                    requestBus.sendMessage(Service.Gateway, new Request(Status.Trade, true, false, matched));
                                    LOG.info("Order {} traded - {}", matched.getGlobalId(), matched);
                                }
                            }
                        }
                    }
                }
            }
            catch (InterruptedException e) {
                    LOG.fatal("Matching Engine interrupted!");
                    e.printStackTrace();
            }
            System.out.println("1");
        }
        LOG.info("MatchingEngine stopped working...");
    }

}

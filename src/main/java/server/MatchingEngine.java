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
    private final BlockingQueue<Message> newOrders;
    // Keep an Order Book for storing all orders
    private final HashMap<Instrument, OrderBook> orderBooks;
    // Response bus to send responses for frontend
    private final MessageBus exchangeBus;
    // Thread which will process orders
    private final Thread orderProcessor;
    // Marker if the engine is running or not
    private boolean running;

    // Keep count of orders to give them unique IDs
    private static long ID = 0;

    public MatchingEngine(MessageBus messageBus) {

        newOrders = new LinkedBlockingQueue<>();
        orderBooks = new HashMap<>();

        exchangeBus = messageBus;
        exchangeBus.registerService(Service.Engine, this);

        orderProcessor = new Thread(this::processOrders);
        running = false;

    }

    public void processMessage(Message message) {
        try {
            newOrders.put(message);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
            LOG.fatal("Thread interrupted, aborting...");
            stop();
            exchangeBus.getService(Service.Gateway).stop();
        }
    }

    public void start() {
        running = true;
        orderProcessor.start();
    }

    public void stop() {
        running = false;
        exchangeBus.getService(Service.Gateway).stop();
    }

    // Main method that the thread will run on
    private void processOrders() {
        LOG.info("MatchingEngine up and running!");
        // Run endlessly
        while (running) {
            try {
                // Receive order from the queue, if it is empty - wait for it.
                Message message = newOrders.take();
                LOG.info("Processing new {}", message);
                // Check if the instrument exists in the order book and if not, add it
                OrderBook instr = orderBooks.get(message.getInstrument());
                if (instr == null) {
                    orderBooks.put(message.getInstrument(), new OrderBook(message.getInstrument()));
                    instr = orderBooks.get(message.getInstrument());
                }
                // Save trees of both sides as the message's own side tree and opposite tree
                TreeSet<Order> ownTree = message.getSide() == Side.SELL ? instr.getSellOrders() : instr.getBuyOrders();
                TreeSet<Order> otherTree = message.getSide() == Side.SELL ? instr.getBuyOrders() : instr.getSellOrders();
                // Check if the message has Cancel status to remove the order
                if (message instanceof Cancel) {
                    Order order = new Order(message);
                    // If removal couldn't be done change the status to CancelFail
                    if (!ownTree.remove(order)) {
                        message = new Fail(Status.CancelFail, message);
                        LOG.warn("Couldn't cancel current {}", message);
                    }
                    else message = new Remove(message);
                    // Send the message through the Response Bus
                    exchangeBus.sendMessage(Service.Gateway, message);
                }
                else {
                    Order order = (Order) message;
                    // Check if all data are valid and if not, skip the iteration and send the error message
                    if (order.getSide() == null || order.getSession() == null || order.getClientId() == null || order.getClientId().isBlank()) {
                        message = new Fail(Status.FatalFail, message);
                        exchangeBus.sendMessage(Service.Gateway, message);
                        LOG.warn("Invalid data in {}", message);

                    }
                    else {
                        boolean invalid = false;
                        // Check all fields for errors
                        if (order.getUser() == null || order.getUser().isBlank()) {
                            message = new Fail(Status.Username, message);
                            invalid = true;
                        }
                        else if (order.getInstrument() == null || order.getInstrument().getName() == null || order.getInstrument().getName().isBlank()) {
                            message = new Fail(Status.Instrument, message);
                            invalid = true;
                        }
                        else if (order.getPrice() == null || order.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
                            message = new Fail(Status.Price, message);
                            invalid = true;
                        }
                        else if (order.getQty() == null || order.getQty().compareTo(BigDecimal.ZERO) <= 0) {
                            message = new Fail(Status.Quantity, message);
                            invalid = true;
                        }
                        // If any error exists then log the field and send the message, otherwise continue the loop
                        if (invalid) {
                            exchangeBus.sendMessage(Service.Gateway, message);
                            LOG.info("Invalid user input field in {}", message);
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
                                        message = new Fail(Status.OrderFail, message);
                                        ID--;
                                        LOG.warn("Listing message unsuccessful! - {}", message);
                                        // TODO
                                    }
                                    else {
                                        message = new List(order);
                                        LOG.info("Listing message successful! - {}", message);
                                    }
                                    exchangeBus.sendMessage(Service.Gateway, message);
                                } else {
                                    // If currently matched order has more quantity then mark our message as traded
                                    if (matched.getQty().compareTo(order.getQty()) > 0) {
                                        matched.setQty(matched.getQty().subtract(order.getQty()));
                                        order.setGlobalId(ID++);
                                        order.setDateInst(Instant.now());
                                        message = new Trade(order);
                                        exchangeBus.sendMessage(Service.Gateway, message);
                                        LOG.info("Listing message successful after trading - {}", message);
                                    } else {
                                        order.setQty(order.getQty().subtract(matched.getQty()));
                                        otherTree.pollFirst();
                                        matched.setQty(BigDecimal.ZERO);
                                        // If the order ran out of remaining qty then mark it as traded and send the message
                                        if (order.getQty().compareTo(BigDecimal.ZERO) <= 0) {
                                            message = new Trade(order);
                                            exchangeBus.sendMessage(Service.Gateway, message);
                                            LOG.info("Traded successfully - {}", message);
                                        }
                                        // If qty is more than 0 then continue iteration
                                        else repeatMatching = true;
                                    }
                                    // Send trade signal for the matched order
                                    exchangeBus.sendMessage(Service.Gateway, new Trade(matched));
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
                stop();
                exchangeBus.getService(Service.Gateway).stop();
            }
        }
        LOG.info("MatchingEngine stopped working...");
    }

}

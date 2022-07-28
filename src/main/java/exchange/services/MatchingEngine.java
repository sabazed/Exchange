package exchange.services;

import exchange.bus.MessageBus;
import exchange.common.Instrument;
import exchange.common.OrderBook;
import exchange.enums.Status;
import exchange.messages.Cancel;
import exchange.messages.Fail;
import exchange.messages.List;
import exchange.messages.Message;
import exchange.messages.Order;
import exchange.messages.Remove;
import exchange.messages.Trade;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class MatchingEngine extends MessageProcessor {

    private final static Logger LOG = LogManager.getLogger("exchangeLogger");

    // Use a LinkedBlockingQueue to receive new orders
    private final BlockingQueue<Message> newOrders;
    // Keep an Order Book for storing all orders
    private final HashMap<Instrument, OrderBook> orderBooks;
    // Response bus to send responses for frontend
    private final MessageBus exchangeBus;
    // Gateway ID for bus
    private final String gatewayId;
    // Service ID for this instance
    private final String selfId;

    // Keep count of orders to give them unique IDs
    private static long ID = 0;


    public MatchingEngine(MessageBus messageBus, String gatewayId, String selfId) {
        newOrders = new LinkedBlockingQueue<>();
        orderBooks = new HashMap<>();
        exchangeBus = messageBus;
        this.gatewayId = gatewayId;
        this.selfId = selfId;
    }

    @Override
    public String getSelfId() {
        return selfId;
    }

    @Override
    public void processMessage(Message message) {
        try {
            newOrders.put(message);
        }
        catch (InterruptedException e) {
            LOG.error("Thread interrupted, aborting...", e);
            stop();
        }
    }

    private void registerOrder(Order order) {
        order.setGlobalId(ID++);
        order.setInstant(Instant.now());
    }

    private Status validateMessage(Order order) {
        if (order.getSide() == null || order.getSession() == null || order.getClientId() == null || order.getClientId().isBlank()) {
            return Status.OrderFail;
        } else if (order.getUser() == null || order.getUser().isBlank()) {
            return Status.Username;
        } else if (order.getInstrument() == null) {
            return Status.Instrument;
        } else if (order.getPrice() == null || order.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            return Status.Price;
        } else if (order.getQty() == null || order.getQty().compareTo(BigDecimal.ZERO) <= 0) {
            return Status.Quantity;
        } else {
            return null;
        }
    }

    private void addOrder(Order order, OrderBook orderBook) {
        registerOrder(order);
        if (orderBook.addOrder(order)) {
            LOG.info("Listing message successful! - {}", order);
            exchangeBus.sendMessage(gatewayId, new List(order));
        }
        else {
            LOG.warn("Listing message unsuccessful! - {}", order);
            ID--;
            exchangeBus.sendMessage(gatewayId, new Fail(Status.OrderFail, order));
        }
    }

    private void cancelOrder(Cancel cancel) {
        // Check if the instrument exists in the order book and if not, the cancel is invalid
        OrderBook orderBook = orderBooks.get(cancel.getInstrument());
        if (orderBook == null || !orderBook.removeOrder(cancel)) {
            LOG.warn("Couldn't cancel current {}", cancel);
            exchangeBus.sendMessage(gatewayId, new Fail(Status.CancelFail, cancel));
        }
        else {
            LOG.info("Cancelled order {}", cancel);
            exchangeBus.sendMessage(gatewayId, new Remove(cancel));
        }
    }

    private boolean matchOrder(Order matched, Order order, OrderBook orderBook) {
        boolean repeatMatching = false;
        // If currently matched order has more quantity, then mark our message as traded
        if (matched.getQty().compareTo(order.getQty()) > 0) {
            registerOrder(order);
            matched.tradeWith(order);
            exchangeBus.sendMessage(gatewayId, new Trade(order));
            LOG.info("Matching successfully finished for {}", order);
        }
        else {
            orderBook.removeOrder(matched);
            order.tradeWith(matched);
            matched.setQty(BigDecimal.ZERO);
            // If the order has 0 qty then send it as traded
            if (order.getQty().compareTo(BigDecimal.ZERO) <= 0) {
                // Send trade signal for the matched order
                exchangeBus.sendMessage(gatewayId, new Trade(order));
                LOG.info("Order {} traded - {}", order.getGlobalId(), order);
            }
            // If qty is more than 0 then continue iteration
            else repeatMatching = true;
        }
        // Send trade for the matched order
        exchangeBus.sendMessage(gatewayId, new Trade(matched));
        LOG.info("Order {} traded - {}", matched.getGlobalId(), matched);
        return repeatMatching;
    }

    @Override
    protected void processMessages() {
        LOG.info("MatchingEngine up and running!");
        // Run endlessly
        while (running) {
            try {
                // Receive order from the queue, if it is empty - wait for it.
                Message message = newOrders.take();
                LOG.info("Processing new {}", message);
                // Check if the message has Cancel status to remove the order
                if (message instanceof Cancel cancel) {
                    cancelOrder(cancel);
                }
                else if (message instanceof Order order) {
                    // Check if the instrument exists in the order book and if not, add it
                    OrderBook orderBook = orderBooks.get(order.getInstrument());
                    if (orderBook == null) {
                        orderBooks.put(order.getInstrument(), new OrderBook(order.getInstrument()));
                        orderBook = orderBooks.get(order.getInstrument());
                    }
                    // Check if all data are valid and if not, skip the iteration and send the error message
                    Status invalidStatus = validateMessage(order);
                    if (invalidStatus != null) {
                        if (invalidStatus == Status.OrderFail) LOG.warn("Invalid data in {}", message);
                        else LOG.info("Invalid user input in {}", message);
                        exchangeBus.sendMessage(gatewayId, new Fail(invalidStatus, message));
                    }
                    else {
                        // Loop until all available orders are exchanged
                        boolean repeatMatching = true;
                        while (repeatMatching) {
                            repeatMatching = false;
                            Order matched = orderBook.getFirstMatch(order);
                            // If no match was found then add the order
                            if (matched == null) {
                                addOrder(order, orderBook);
                            }
                            else {
                                // Process trade between matched and order
                                repeatMatching = matchOrder(matched, order, orderBook);
                            }
                        }
                    }
                }
            }
            catch (InterruptedException e) {
                LOG.error("Matching Engine interrupted!", e);
                stop();
            }
        }
        LOG.info("MatchingEngine stopped working...");
    }

}

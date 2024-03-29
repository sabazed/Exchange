package exchange.services;

import exchange.bus.MessageBus;
import exchange.common.Instrument;
import exchange.common.MarketDataEntry;
import exchange.common.OrderBook;
import exchange.enums.Status;
import exchange.messages.Cancel;
import exchange.messages.Fail;
import exchange.messages.Listing;
import exchange.messages.MarketDataUpdate;
import exchange.messages.Message;
import exchange.messages.Order;
import exchange.messages.Remove;
import exchange.messages.Trade;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;

public class MatchingEngine extends MessageProcessor {

    private static final Logger LOG = LogManager.getLogger(MatchingEngine.class);

    // Keep an Order Book for storing all orders
    private final HashMap<Instrument, OrderBook> orderBooks;
    // OrderEntryGateway and MarketDataProvider ID for bus
    private String gatewayId;
    private String marketProviderId;

    // Keep count of orders to give them unique IDs
    private static long ID = 0;


    public MatchingEngine(MessageBus messageBus) {
        super(messageBus);
        orderBooks = new HashMap<>();
    }

    @Override
    public String getSelfId() {
        return selfId;
    }

    public void setGatewayId(String gatewayId) {
        this.gatewayId = gatewayId;
    }

    public void setMarketProviderId(String marketProviderId) {
        this.marketProviderId = marketProviderId;
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
            exchangeBus.sendMessage(gatewayId, new Listing(order));
        }
        else {
            LOG.warn("Listing message unsuccessful! - {}", order);
            exchangeBus.sendMessage(gatewayId, new Fail(Status.OrderFail, order, order.getGlobalId()));
        }
        exchangeBus.sendMessage(marketProviderId, new MarketDataUpdate(order, getMarketData(order.getInstrument())));
    }

    private void cancelOrder(Cancel cancel) {
        // Check if the instrument exists in the order book and if not, the cancel is invalid
        OrderBook orderBook = orderBooks.get(cancel.getInstrument());
        if (orderBook == null || !orderBook.removeOrder(cancel.getGlobalId())) {
            LOG.warn("Couldn't cancel current {}", cancel);
            exchangeBus.sendMessage(gatewayId, new Fail(Status.CancelFail, cancel, cancel.getGlobalId()));
        }
        else {
            LOG.info("Cancelled order {}", cancel);
            exchangeBus.sendMessage(gatewayId, new Remove(cancel));
        }
        exchangeBus.sendMessage(marketProviderId, new MarketDataUpdate(cancel, getMarketData(cancel.getInstrument())));
    }

    private boolean matchOrder(Order matched, Order order, OrderBook orderBook) {
        boolean repeatMatching = false;
        // If currently matched order has more quantity, then mark our message as traded
        if (matched.getQty().compareTo(order.getQty()) > 0) {
            registerOrder(order);
            matched.setQty(matched.getQty().subtract(order.getQty()));
            exchangeBus.sendMessage(gatewayId, new Trade(order));
            LOG.info("Matching successfully finished for {}", order);
        }
        else {
            orderBook.removeOrder(matched.getGlobalId());
            order.setQty(order.getQty().subtract(matched.getQty()));
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
        // Change last trade price for the order book
        orderBook.setLastTrade(order.getPrice());
        // Send trade for the matched order
        exchangeBus.sendMessage(gatewayId, new Trade(matched));
        LOG.info("Order {} traded - {}", matched.getGlobalId(), matched);
        // Send updated market data
        exchangeBus.sendMessage(marketProviderId, new MarketDataUpdate(order, getMarketData(matched.getInstrument())));
        return repeatMatching;
    }

    private void processOrder(Order order, OrderBook orderBook) {
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

    private MarketDataEntry getMarketData(Instrument instrument) {
        return orderBooks.get(instrument).getBestPrices();
    }

    @Override
    protected void processMessage(Message message) {
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
                exchangeBus.sendMessage(gatewayId, new Fail(invalidStatus, message, order.getGlobalId()));
            }
            else {
                // If the order is valid then we process it
                processOrder(order, orderBook);
            }
        }
        else {
            LOG.error("Invalid message type received, exiting - {}", message);
            throw new IllegalMessageException(message.toString());
        }
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }

}

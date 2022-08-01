package exchange.services;

import exchange.bus.MessageBus;
import exchange.common.Instrument;
import exchange.common.MarketDataEntry;
import exchange.common.OrderBook;
import exchange.enums.Status;
import exchange.messages.Cancel;
import exchange.messages.Fail;
import exchange.messages.Listing;
import exchange.messages.MarketDataResponse;
import exchange.messages.Message;
import exchange.messages.Order;
import exchange.messages.Remove;
import exchange.messages.Trade;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class MatchingEngine extends MessageProcessor {

    // Keep an Order Book for storing all orders
    private final HashMap<Instrument, OrderBook> orderBooks;
    // OrderEntryGateway and MarketDataProvider ID for bus
    private final String gatewayId;
    private final String marketProviderId;

    // Keep count of orders to give them unique IDs
    private static long ID = 0;


    public MatchingEngine(MessageBus messageBus, String gatewayId, String marketProviderId, String selfId) {
        super(messageBus, selfId, MatchingEngine.class);
        orderBooks = new HashMap<>();
        this.gatewayId = gatewayId;
        this.marketProviderId = marketProviderId;
    }

    @Override
    public String getSelfId() {
        return selfId;
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
            exchangeBus.sendMessage(gatewayId, new Fail(Status.OrderFail, order));
        }
        exchangeBus.sendMessage(marketProviderId, new MarketDataResponse(order, order.getInstrument(), getMarketData(order.getInstrument())));
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
        exchangeBus.sendMessage(marketProviderId, new MarketDataResponse(cancel, cancel.getInstrument(), getMarketData(cancel.getInstrument())));
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
            orderBook.removeOrder(matched);
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
        exchangeBus.sendMessage(marketProviderId, new MarketDataResponse(order, order.getInstrument(), getMarketData(order.getInstrument(), matched.getInstrument())));
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

    private List<MarketDataEntry> getMarketData(Instrument... instruments) {
        if (instruments.length == 0) {
            return orderBooks.values().stream().map(OrderBook::getBestPrices).toList();
        }
        else {
            return Arrays.stream(instruments).map(instrument -> orderBooks.get(instrument).getBestPrices()).toList();
        }
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
                exchangeBus.sendMessage(gatewayId, new Fail(invalidStatus, message));
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

}

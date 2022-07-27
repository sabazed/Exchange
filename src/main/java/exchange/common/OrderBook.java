package exchange.common;

import exchange.enums.Side;
import exchange.messages.*;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeSet;

public class OrderBook {

    // Use TreeSets to keep the set sorted during insertion and removal for both Sides, then link it with its Instrument
    private final Instrument instrument;
    private final TreeSet<Order> buyOrders;
    private final TreeSet<Order> sellOrders;
    private final Map<Long, Order> orderMap;

    public OrderBook(Instrument instrument) {
        this.instrument = instrument;
        buyOrders = new TreeSet<>(new OrderComparator(true));
        sellOrders = new TreeSet<>(new OrderComparator(false));
        orderMap = new HashMap<>();
    }

    public boolean removeOrder(Message message) {
        // Check if the map contains the order
        Order order = orderMap.remove(message.getGlobalId());
        // Check if order can be removed from their tree
        if (order != null) {
            return (order.getSide() == Side.BUY) ? buyOrders.remove(order) : sellOrders.remove(order);
        }
        return false;
    }

    public boolean addOrder(Order order) {
        // If there was a problem while adding then change status flag to OrderFail and decrease ID counter
        if ((order.getSide() == Side.BUY) ? buyOrders.add(order) : sellOrders.add(order)) {
            orderMap.put(order.getGlobalId(), order);
            return true;
        }
        return false;
    }

    public Order getFirstMatch(Order order) {
        // Get the best deal from the tree
        Iterator<Order> iterator = (order.getSide() == Side.SELL) ? sellOrders.iterator() : buyOrders.iterator();
        Order matched = iterator.hasNext() ? iterator.next() : null;
        // Iterate over the orders if the users are the same
        while (iterator.hasNext() && matched.getUser().equals(order.getUser())) {
            matched = iterator.next();
        }
        // If the users are still same we assign matched as null, or consider two cases:
        // Case Sell: if matched price is higher or equal else matched = null
        // Case Buy: if matched price is lower or equal else matched = null
        if (matched == null || matched.getUser().equals(order.getUser()) || matched.getPrice().compareTo(order.getPrice()) * ((order.getSide() == Side.BUY) ? 1 : -1) > 0) {
            return null;
        }
        return matched;
    }

}

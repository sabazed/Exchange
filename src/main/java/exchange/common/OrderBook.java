package exchange.common;

import exchange.messages.Order;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

public class OrderBook {

    // Use TreeSets to keep the set sorted during insertion and removal for both Sides, then link it with its Instrument
    private final Instrument instrument;
    private final TreeSet<Order> buyOrders;
    private final TreeSet<Order> sellOrders;
    private final Map<Long, Order> buyOrderMap;
    private final Map<Long, Order> sellOrderMap;

    public OrderBook(Instrument instrument) {
        this.instrument = instrument;
        buyOrders = new TreeSet<>(new OrderComparator(true));
        sellOrders = new TreeSet<>(new OrderComparator(false));
        buyOrderMap = new HashMap<>();
        sellOrderMap = new HashMap<>();
    }

    public Instrument getInstrument() {
        return instrument;
    }

    public TreeSet<Order> getBuyOrders() {
        return buyOrders;
    }

    public TreeSet<Order> getSellOrders() {
        return sellOrders;
    }

    public Map<Long, Order> getBuyOrderMap() {
        return buyOrderMap;
    }

    public Map<Long, Order> getSellOrderMap() {
        return sellOrderMap;
    }
}

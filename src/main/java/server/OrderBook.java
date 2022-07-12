package server;

import java.util.HashMap;
import java.util.TreeSet;

public class OrderBook {

    // Use TreeSets to keep the set sorted during insertion and removal for both Sides, then link it with its Instrument
    private final HashMap<Instrument, HashMap<Side, TreeSet<Order>>> orders;

    public OrderBook() {
        this.orders = new HashMap<>();
    }

    public HashMap<Instrument, HashMap<Side, TreeSet<Order>>> getOrders() {
        return orders;
    }

    // Method for initializing a new instrument pair
    public HashMap<Side, TreeSet<Order>> addInstrument(Instrument newInstrument) {
        orders.put(newInstrument, new HashMap<>());
        HashMap<Side, TreeSet<Order>> newInstrSet = orders.get(newInstrument);
        newInstrSet.put(Side.BUY, new TreeSet<>(new OrderComparator()));
        newInstrSet.put(Side.SELL, new TreeSet<>(new OrderComparator()));
        return newInstrSet;
    }

}

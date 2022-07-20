package server;

import java.util.TreeSet;

public class OrderBook {

    // Use TreeSets to keep the set sorted during insertion and removal for both Sides, then link it with its Instrument
    private final Instrument instrument;
    private final TreeSet<Order> buyOrders;
    private final TreeSet<Order> sellOrders;

    public OrderBook(Instrument instrument) {
        this.instrument = instrument;
        buyOrders = new TreeSet<>();
        sellOrders = new TreeSet<>();
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

}

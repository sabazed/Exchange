package com.exchange.exchange;

public class Order {

    private static long count;
    private final long id;

    private final Instrument instrumentID;
    private final Side side;
    private final double price;
    private final int qty;

    public Order(Instrument instrumentID, Side side, double price, int qty) {
        this.instrumentID = instrumentID;
        this.side = side;
        this.price = price;
        this.qty = qty;
        this.id = count++;
    }

    public Instrument getInstrumentID() {
        return instrumentID;
    }

    public Side getSide() {
        return side;
    }

    public double getPrice() {
        return price;
    }

    public int getQty() {
        return qty;
    }

    public boolean compare(Order order) {
        return order.instrumentID.getId() == instrumentID.getId() && order.price == price && order.qty == qty && order.side != side;
    }

    public long getId() {
        return this.id;
    }

}

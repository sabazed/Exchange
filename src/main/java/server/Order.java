package server;

import jakarta.websocket.Session;

import java.util.Comparator;

public class Order {

    private final String user;
    private final Session session;
    private final Instrument instrument;
    private final Side side;
    private final double price;

    private int qty;
    private long id;

    public Order(String user, Session session, Instrument instrument, Side side, double price, int qty, long id) {
        this.user = user;
        this.session = session;
        this.instrument = instrument;
        this.side = side;
        this.price = price;
        this.qty = qty;
        this.id = id; // Default ID for distinguishing newly created orders
    }

    public String getUser() {
        return user;
    }

    public Session getSession() {
        return session;
    }

    public Instrument getInstrument() {
        return instrument;
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

    public void setQty(int qty) {
        this.qty = qty;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

}

class OrderComparator implements Comparator<Order> {
    @Override
    public int compare(Order o1, Order o2) {
        // Equal only if default ID is not set (not new order) and both IDs are equal, or otherwise if prices are equal
        if ((o1.getId() != -1 && o1.getId() == o2.getId()) || (o1.getId() == -1 && o1.getPrice() == o2.getPrice())) return 0;
        else if (o1.getPrice() < o2.getPrice()) return -1;
        else return 1;
    }
}

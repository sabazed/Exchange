package server;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Comparator;

public class Order {

    private final String user;
    private final Instrument instrument;
    private final Side side;
    private final BigDecimal price;
    private final String clientId;

    private String session;
    private int qty;
    private Instant date;
    private long globalId;

    // Constructor for failed orders
    public Order() {
        this.user = null;
        this.instrument = null;
        this.side = null;
        this.price = null;
        this.clientId = null;
    }

    public String getUser() {
        return user;
    }

    public String getSession() {
        return session;
    }

    public void setSession(String session) {
        this.session = session;
    }

    public Instrument getInstrument() {
        return instrument;
    }

    public Side getSide() {
        return side;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public String getDate() {
        return date != null ? date.toString() : null;
    }

    @JsonIgnore
    public Instant getDateInst() {
        return date;
    }

    public void setDate(String date) {
        this.date = Instant.parse(date);
    }

    @JsonIgnore
    public void setDateInst(Instant date) {
        this.date = date;
    }

    public String getClientId() {
        return clientId;
    }

    public int getQty() {
        return qty;
    }

    public void setQty(int qty) {
        this.qty = qty;
    }

    public long getGlobalId() {
        return globalId;
    }

    public void setGlobalId(long globalId) {
        this.globalId = globalId;
    }

    @Override
    public String toString() {
        return "{user='" + user + '\'' +
                ", instrument=" + instrument.getId() +
                ", side=" + side +
                ", price=" + price +
                ", clientId='" + clientId + '\'' +
                ", session='" + session + '\'' +
                ", qty=" + qty +
                ", date=" + date +
                ", globalId=" + globalId +
                '}';
    }
}

class OrderComparator implements Comparator<Order> {

    private final int reversed;

    public OrderComparator(boolean reverse) {
        this.reversed = reverse ? -1 : 1;
    }

    @Override
    public int compare(Order o1, Order o2) {
        // Comparison priority: price - date - id
        int temp = o1.getPrice().compareTo(o2.getPrice());
        if (temp == 0) {
            if (!o1.getDateInst().equals(o2.getDateInst())) return o1.getDateInst().isBefore(o2.getDateInst()) ? -1 : 1;
            else return Long.compare(o1.getGlobalId(), o2.getGlobalId());
        }
        return temp * reversed;
    }

}

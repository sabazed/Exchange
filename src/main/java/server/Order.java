package server;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Comparator;

public class Order implements Message {

    private String user;
    private Instrument instrument;
    private Side side;
    private BigDecimal price;
    private BigDecimal qty;

    private String session;
    private Instant date;

    private String clientId;
    private long globalId;

    // Constructor for Cancel to compare
    public Order(Message cancel) {
        this.instrument = cancel.getInstrument();
        this.side = cancel.getSide();
        this.session = cancel.getSession();
        this.clientId = cancel.getClientId();
        this.globalId = cancel.getGlobalId();
    }

    public Order() {
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

    public void setDate(String date) {
        this.date = Instant.parse(date);
    }

    @JsonIgnore
    public Instant getDateInst() {
        return date;
    }

    @JsonIgnore
    public void setDateInst(Instant date) {
        this.date = date;
    }

    public String getClientId() {
        return clientId;
    }

    public BigDecimal getQty() {
        return qty;
    }

    public void setQty(BigDecimal qty) {
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
        return "Order{user='" + user + '\'' +
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
        if (o1.getPrice() == null) return Long.compare(o1.getGlobalId(), o2.getGlobalId());
        // Comparison priority: price - date - id
        int temp = o1.getPrice().compareTo(o2.getPrice());
        if (temp == 0) {
            if (!o1.getDateInst().equals(o2.getDateInst()))
                return o1.getDateInst().isBefore(o2.getDateInst()) ? -1 : 1;
            else return Long.compare(o1.getGlobalId(), o2.getGlobalId());
        }
        return temp * reversed;
    }

}

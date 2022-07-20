package server;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.math.BigDecimal;
import java.time.Instant;

public class Order implements Message, Comparable<Order>  {

    private String user;
    private Instrument instrument;
    private Side side;
    private BigDecimal price;
    private BigDecimal qty;

    private String session;
    private Instant date;

    private String clientId;
    private long globalId;

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

    @Override
    public int compareTo(Order o) {
        int temp = getPrice().compareTo(o.getPrice());
        if (temp == 0) {
            if (!getDateInst().equals(o.getDateInst()))
                return getDateInst().isBefore(o.getDateInst()) ? -1 : 1;
            else return Long.compare(getGlobalId(), o.getGlobalId());
        }
        return temp * side.getVal();
    }

}
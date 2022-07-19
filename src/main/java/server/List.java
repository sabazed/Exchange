package server;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.math.BigDecimal;
import java.time.Instant;

public class List implements Message {
    private String user;
    private Instrument instrument;
    private Side side;
    private BigDecimal price;
    private int qty;

    private String session;
    private Instant date;

    private String clientId;
    private long globalId;

    public List(Order order) {
        this.user = order.getUser();
        this.instrument = order.getInstrument();
        this.side = order.getSide();
        this.price = order.getPrice();
        this.qty = order.getQty();
        this.session = order.getSession();
        this.date = order.getDateInst();
        this.clientId = order.getClientId();
        this.globalId = order.getGlobalId();
    }

    public List() {
    }

    public String getUser() {
        return user;
    }

    public String getSession() {
        return session;
    }

    @Override
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

    public String getClientId() {
        return clientId;
    }

    public int getQty() {
        return qty;
    }

    public long getGlobalId() {
        return globalId;
    }

    @JsonIgnore
    public Instant getDateInst() {
        return this.date;
    }

    @Override
    public String toString() {
        return "List{" +
                "user='" + user + '\'' +
                ", instrument=" + instrument.getId() +
                ", side=" + side +
                ", price=" + price +
                ", qty=" + qty +
                ", session='" + session + '\'' +
                ", date=" + date +
                ", clientId='" + clientId + '\'' +
                ", globalId=" + globalId +
                '}';
    }

}

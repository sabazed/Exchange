package exchange.messages;

import com.fasterxml.jackson.annotation.JsonIgnore;
import exchange.enums.Side;
import exchange.common.Instrument;

import java.math.BigDecimal;
import java.time.Instant;

public class List implements Message {
    private String user;
    private Instrument instrument;
    private Side side;
    private BigDecimal price;
    private BigDecimal qty;

    private String session;
    private Instant timestamp;

    private String clientId;
    private long globalId;

    public List(Order order) {
        this.user = order.getUser();
        this.instrument = order.getInstrument();
        this.side = order.getSide();
        this.price = order.getPrice();
        this.qty = order.getQty();
        this.session = order.getSession();
        this.timestamp = order.getInstant();
        this.clientId = order.getClientId();
        this.globalId = order.getGlobalId();
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

    public String getTimestamp() {
        return timestamp != null ? timestamp.toString() : null;
    }

    public String getClientId() {
        return clientId;
    }

    public BigDecimal getQty() {
        return qty;
    }

    public long getGlobalId() {
        return globalId;
    }

    @JsonIgnore
    public Instant getInstant() {
        return this.timestamp;
    }

    @Override
    public String toString() {
        return "List{" +
                "user='" + user + '\'' +
                ", instrument=" + instrument.toString() +
                ", side=" + side +
                ", price=" + price +
                ", qty=" + qty +
                ", session='" + session + '\'' +
                ", timestamp=" + timestamp +
                ", clientId='" + clientId + '\'' +
                ", globalId=" + globalId +
                '}';
    }

}

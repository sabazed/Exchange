package exchange.messages;

import com.fasterxml.jackson.annotation.JsonIgnore;
import exchange.common.Instrument;
import exchange.enums.Side;

import java.math.BigDecimal;
import java.time.Instant;

public class Order implements Message {

    private String user;
    private Instrument instrument;
    private Side side;
    private BigDecimal price;
    private BigDecimal qty;

    private String session;
    private Instant timestamp;

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

    public String getTimestamp() {
        return timestamp != null ? timestamp.toString() : null;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = Instant.parse(timestamp);
    }

    @JsonIgnore
    public Instant getInstant() {
        return timestamp;
    }

    @JsonIgnore
    public void setInstant(Instant timestamp) {
        this.timestamp = timestamp;
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
                ", instrument=" + instrument.toString() +
                ", side=" + side +
                ", price=" + price +
                ", clientId='" + clientId + '\'' +
                ", session='" + session + '\'' +
                ", qty=" + qty +
                ", timestamp=" + timestamp +
                ", globalId=" + globalId +
                '}';
    }

}

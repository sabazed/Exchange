package exchange.messages;

import exchange.enums.Side;
import exchange.common.Instrument;

import java.math.BigDecimal;
import java.time.Instant;

public class Cancel implements Message {

    private String session;
    private Instrument instrument;
    private BigDecimal price;
    private Side side;
    private Instant timestamp;
    private String clientId;
    private long globalId;

    @Override
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

    public BigDecimal getPrice() {
        return price;
    }

    public Side getSide() {
        return side;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = Instant.parse(timestamp);
    }

    @Override
    public String getClientId() {
        return clientId;
    }

    @Override
    public long getGlobalId() {
        return globalId;
    }

    @Override
    public String toString() {
        return "Cancel{" +
                "session='" + session + '\'' +
                ", instrument=" + instrument.getName() +
                ", price=" + price +
                ", side=" + side +
                ", timestamp=" + timestamp +
                ", clientId='" + clientId + '\'' +
                ", globalId=" + globalId +
                '}';
    }
}

package server;

import java.math.BigDecimal;
import java.time.Instant;

public class Cancel implements Message {

    private String session;
    private Instrument instrument;
    private Side side;
    private String clientId;
    private long globalId;

    public Cancel() {
    }

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

    @Override
    public BigDecimal getPrice() {
        return null;
    }

    @Override
    public Side getSide() {
        return side;
    }

    @Override
    public Instant getDateInst() {
        return null;
    }

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
                ", instrument=" + instrument.getId() +
                ", side=" + side +
                ", clientId='" + clientId + '\'' +
                ", globalId=" + globalId +
                '}';
    }
}

package server;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.math.BigDecimal;
import java.time.Instant;

public class Remove implements Message {

    private String session;
    private Instrument instrument;
    private Side side;
    private String clientId;
    private long globalId;

    public Remove(Message cancel) {
        session = cancel.getSession();
        instrument = cancel.getInstrument();
        side = cancel.getSide();
        clientId = cancel.getClientId();
        globalId = cancel.getGlobalId();
    }

    public Remove() {
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
        return "Remove{" +
                "session='" + session + '\'' +
                ", instrument=" + instrument.getId() +
                ", side=" + side +
                ", clientId='" + clientId + '\'' +
                ", globalId=" + globalId +
                '}';
    }

}

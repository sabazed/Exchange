package server;

import java.math.BigDecimal;
import java.time.Instant;

public class Fail implements Message {

    private String session;
    private String clientId;
    private long globalId;
    private Status status;

    public Fail(Status status, Message message) {
        this.status = status;
        session = message.getSession();
        globalId = message.getGlobalId();
        clientId = message.getClientId();
    }

    public Fail(Status status) {
        this.status = status;
    }

    public Fail() {
    }

    @Override
    public String getSession() {
        return session;
    }

    @Override
    public void setSession(String session) {
        this.session = session;
    }

    @Override
    public Instrument getInstrument() {
        return null;
    }

    @Override
    public BigDecimal getPrice() {
        return null;
    }

    @Override
    public Side getSide() {
        return null;
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

    public Status getStatus() {
        return status;
    }

    @Override
    public String toString() {
        return "Fail{" +
                "session='" + session + '\'' +
                ", clientId='" + clientId + '\'' +
                ", globalId=" + globalId +
                ", status=" + status +
                '}';
    }
}

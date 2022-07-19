package server;

import java.math.BigDecimal;
import java.time.Instant;

public class Trade implements Message {

    private String session;
    private String clientId;
    private long globalId;
    private BigDecimal qty;

    public Trade(Order order) {
        session = order.getSession();
        qty = order.getQty();
        globalId = order.getGlobalId();
        clientId = order.getClientId();
    }

    public Trade() {
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

    public BigDecimal getQty() {
        return qty;
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

    @Override
    public String toString() {
        return "Trade{" +
                "session='" + session + '\'' +
                ", clientId='" + clientId + '\'' +
                ", globalId=" + globalId +
                ", qty=" + qty +
                '}';
    }
}

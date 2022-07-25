package exchange.messages;

import exchange.enums.Side;
import exchange.common.Instrument;

public class Cancel implements Message {

    private String session;
    private Instrument instrument;
    private Side side;
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

    public Side getSide() {
        return side;
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
                ", instrument='" + instrument.getName() +
                ", side=" + side +
                ", clientId='" + clientId + '\'' +
                ", globalId=" + globalId +
                '}';
    }
}

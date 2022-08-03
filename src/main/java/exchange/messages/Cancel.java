package exchange.messages;

import exchange.common.Instrument;

public class Cancel implements Message {

    private String session;
    private Instrument instrument;
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

    @Override
    public String getClientId() {
        return clientId;
    }

    public long getGlobalId() {
        return globalId;
    }

    @Override
    public String toString() {
        return "Cancel{" +
                "session='" + session + '\'' +
                ", instrument='" + instrument.toString() +
                ", clientId='" + clientId + '\'' +
                ", globalId=" + globalId +
                '}';
    }
}

package exchange.messages;

import exchange.common.Instrument;

public class Remove implements Message {

    private String session;
    private Instrument instrument;
    private String clientId;
    private long globalId;

    public Remove(Cancel cancel) {
        session = cancel.getSession();
        instrument = cancel.getInstrument();
        clientId = cancel.getClientId();
        globalId = cancel.getGlobalId();
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
                ", instrument=" + instrument.toString() +
                ", clientId='" + clientId + '\'' +
                ", globalId=" + globalId +
                '}';
    }

}

package exchange.messages;

import exchange.common.Instrument;

import java.util.List;

public class InstrumentData implements Message {

    private String session;
    private List<Instrument> instruments;
    private String clientId;
    private long globalId;

    public InstrumentData(Message request, List<Instrument> instruments) {
        this.session = request.getSession();
        this.instruments = instruments;
        this.clientId = request.getClientId();
        this.globalId = request.getGlobalId();
    }

    @Override
    public String getSession() {
        return session;
    }

    @Override
    public void setSession(String session) {
        this.session = session;
    }

    public List<Instrument> getInstruments() {
        return instruments;
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
        return "Response{" +
                "session='" + session + '\'' +
                ", data=" + instruments +
                ", clientId='" + clientId + '\'' +
                ", globalId=" + globalId +
                '}';
    }
}
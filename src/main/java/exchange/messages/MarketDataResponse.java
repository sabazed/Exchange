package exchange.messages;

import exchange.common.Instrument;
import exchange.common.MarketDataEntry;

import java.util.List;

public class MarketDataResponse implements Message {

    private String session;
    private Instrument instrument;
    private List<MarketDataEntry> updates;
    private String clientId;
    private long globalId;

    public MarketDataResponse(Message message, Instrument instrument, List<MarketDataEntry> updates) {
        this.session = message.getSession();
        this.instrument = instrument;
        this.updates = updates;
        this.clientId = message.getClientId();
        this.globalId = message.getGlobalId();
    }

    public MarketDataResponse(MarketDataResponse original, String session) {
        this.session = session;
        instrument = original.instrument;
        updates = original.updates;
        clientId = original.clientId;
        globalId = original.globalId;
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

    public List<MarketDataEntry> getUpdates() {
        return updates;
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
        return "MarketDataResponse{" +
                "session='" + session + '\'' +
                ", updates=" + updates +
                ", clientId='" + clientId + '\'' +
                ", globalId=" + globalId +
                '}';
    }

}

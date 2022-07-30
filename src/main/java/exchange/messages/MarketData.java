package exchange.messages;

import exchange.common.MarketDataEntry;

import java.util.List;

public class MarketData implements Message {

    private String session;
    private List<MarketDataEntry> updates;
    private String clientId;
    private long globalId;

    public MarketData(Message message, List<MarketDataEntry> updates) {
        this.session = message.getSession();
        this.updates = updates;
        this.clientId = message.getClientId();
        this.globalId = message.getGlobalId();
    }

    public MarketData(MarketData original, String session) {
        this.session = session;
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
        return "MarketData{" +
                "session='" + session + '\'' +
                ", updates=" + updates +
                ", clientId='" + clientId + '\'' +
                ", globalId=" + globalId +
                '}';
    }

}

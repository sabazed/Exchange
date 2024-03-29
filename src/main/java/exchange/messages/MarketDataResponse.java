package exchange.messages;

import exchange.common.MarketDataEntry;

import java.util.Collection;
import java.util.Set;

public class MarketDataResponse implements Message {

    private String session;
    private Set<MarketDataEntry> updates;
    private String clientId;

    public MarketDataResponse(Message message, Set<MarketDataEntry> updates) {
        this.session = message.getSession();
        this.updates = updates;
        this.clientId = message.getClientId();
    }

    public MarketDataResponse(MarketDataUpdate update, String session) {
        this.session = session;
        clientId = update.getClientId();
        updates = Set.of(update.getUpdate());
    }

    @Override
    public String getSession() {
        return session;
    }

    @Override
    public void setSession(String session) {
        this.session = session;
    }

    public Collection<MarketDataEntry> getUpdates() {
        return updates;
    }

    @Override
    public String getClientId() {
        return clientId;
    }

    @Override
    public String toString() {
        return "MarketDataResponse{" +
                "session='" + session + '\'' +
                ", updates=" + updates +
                ", clientId='" + clientId + '\'' +
                '}';
    }

}

package exchange.messages;

import exchange.common.MarketDataEntry;

public class MarketDataUpdate implements Message {

    private String session;
    private MarketDataEntry update;
    private String clientId;

    public MarketDataUpdate(Message message, MarketDataEntry update) {
        this.session = message.getSession();
        this.clientId = message.getClientId();
        this.update = update;
    }

    @Override
    public String getSession() {
        return session;
    }

    @Override
    public void setSession(String session) {
        this.session = session;
    }

    public MarketDataEntry getUpdate() {
        return update;
    }

    @Override
    public String getClientId() {
        return clientId;
    }

    @Override
    public String toString() {
        return "MarketDataUpdate{" +
                "session='" + session + '\'' +
                ", update=" + update +
                ", clientId='" + clientId + '\'' +
                '}';
    }

}

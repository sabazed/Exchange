package exchange.messages;

public class MarketDataUnsubscribe implements Message {

    private String session;
    private String clientId;

    public MarketDataUnsubscribe(String session) {
        this.session = session;
        this.clientId = null;
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
    public String getClientId() {
        return clientId;
    }

    @Override
    public String toString() {
        return "MarketDataUnsubscribe{" +
                "session='" + session + '\'' +
                ", clientId='" + clientId + '\'' +
                '}';
    }

}

package exchange.messages;

public class UnsubscribeRequest implements Message {

    private String session;
    private String clientId;

    public UnsubscribeRequest(String session) {
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
        return "UnsubscribeRequest{" +
                "session='" + session + '\'' +
                ", clientId='" + clientId + '\'' +
                '}';
    }

}

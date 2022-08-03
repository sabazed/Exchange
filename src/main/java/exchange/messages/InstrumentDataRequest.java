package exchange.messages;

public class InstrumentDataRequest implements Message {

    private String session;
    private String clientId;

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
        return "InstrumentDataRequest{" +
                "session='" + session + '\'' +
                ", clientId='" + clientId + '\'' +
                '}';
    }

}

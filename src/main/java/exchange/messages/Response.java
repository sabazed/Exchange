package exchange.messages;

import exchange.common.Instrument;

import java.util.List;

public class Response implements Message {

    private String session;
    private List<Instrument> instruments;
    private String clientId;
    private long globalId;

    public Response(Message request, List<Instrument> instruments) {
        this.session = request.getSession();
        this.clientId = request.getClientId();
        this.instruments = instruments;
    }

    public Response() {
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

}

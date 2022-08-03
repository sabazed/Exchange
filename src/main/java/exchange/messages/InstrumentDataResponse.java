package exchange.messages;

import exchange.common.Instrument;

import java.util.List;

public class InstrumentDataResponse implements Message {

    private String session;
    private List<Instrument> instruments;
    private String clientId;

    public InstrumentDataResponse(Message request, List<Instrument> instruments) {
        this.session = request.getSession();
        this.instruments = instruments;
        this.clientId = request.getClientId();
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
    public String toString() {
        return "Response{" +
                "session='" + session + '\'' +
                ", data=" + instruments +
                ", clientId='" + clientId + '\'' +
                '}';
    }
}
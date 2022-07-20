package server;

public class Cancel implements Message, Comparable<Order> {

    private String session;
    private Instrument instrument;
    private Side side;
    private String clientId;
    private long globalId;

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

    public Side getSide() {
        return side;
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
        return "Cancel{" +
                "session='" + session + '\'' +
                ", instrument=" + instrument.getId() +
                ", side=" + side +
                ", clientId='" + clientId + '\'' +
                ", globalId=" + globalId +
                '}';
    }

    @Override
    public int compareTo(Order o) {
        return Long.compare(globalId, o.getGlobalId());
    }
}

package server;

public class Fail implements Message {

    private String session;
    private String clientId;
    private long globalId;
    private Status status;

    public Fail(Status status, Message message) {
        this.status = status;
        session = message.getSession();
        globalId = message.getGlobalId();
        clientId = message.getClientId();
    }

    public Fail(Status status) {
        this.status = status;
    }

    @Override
    public String getSession() {
        return session;
    }

    @Override
    public void setSession(String session) {
        this.session = session;
    }

    public Status getStatus() {
        return status;
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
        return "Fail{" +
                "session='" + session + '\'' +
                ", clientId='" + clientId + '\'' +
                ", globalId=" + globalId +
                ", status=" + status +
                '}';
    }
}

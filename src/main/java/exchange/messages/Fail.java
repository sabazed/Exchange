package exchange.messages;

import exchange.enums.Status;

public class Fail implements Message {

    private String session;
    private String clientId;
    private long globalId;
    private Status status;

    public Fail(Status status, Message message, long globalId) {
        this.status = status;
        this.globalId = globalId;
        session = message.getSession();
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

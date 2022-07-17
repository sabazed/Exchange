package server;

import java.util.List;

public interface Message {

    public boolean isValid();

    public void invalidate();

    public void addErrorCode(Status status);

    public List<Status> getStatus();

    public void setStatus(Status status);

    public String getSession();

    public Order getOrder();

}

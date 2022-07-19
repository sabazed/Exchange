package server;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.LinkedList;
import java.util.List;

public class Request implements Message {

    private boolean valid = true;
    private boolean sent = true;
    private final List<Status> status;
    private final Order order;

    @JsonCreator
    public Request(@JsonProperty("status") Status status,
                   @JsonProperty("valid") boolean valid,
                   @JsonProperty("sent") boolean sent,
                   @JsonProperty("order") Order order) {
        this.status = new LinkedList<>();
        if (status != null)
            this.status.add(status);
        this.order = order;
        this.valid = valid;
        this.sent = sent;
    }

    @Override
    public void addErrorCode(Status status) {
        this.status.add(status);
    }

    @Override
    public boolean isValid() {
        return valid;
    }

    @Override
    public void setValid(boolean valid) {
        this.valid = valid;
    }

    @Override
    public List<Status> getStatus() {
        return status;
    }

    @Override
    public void setStatus(Status status) {
        this.status.clear();
        this.status.add(status);
    }

    @Override
    public Order getOrder() {
        return order;
    }

    @Override
    public String getSession() {
        return this.order.getSession();
    }

    public boolean isSent() {
        return sent;
    }

    public void setSent(boolean sent) {
        this.sent = sent;
    }

    @Override
    public String toString() {
        return "Request[Status=" + status + ", Order=" + order + "]";
    }
}

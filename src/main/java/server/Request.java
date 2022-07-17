package server;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.LinkedList;
import java.util.List;

public class Request implements Message {

    private boolean valid = true;
    private final List<Status> status;
    private final Order order;

    @JsonCreator
    public Request(@JsonProperty("status") Status status, @JsonProperty("order") Order order) {
        this.status = new LinkedList<>();
        this.status.add(status);
        this.order = order;
    }

    public void addErrorCode(Status status) {
        this.status.add(status);
    }

    @JsonIgnore
    public boolean isValid() {
        return valid;
    }

    public void invalidate() {
        this.valid = false;
    }

    @JsonIgnore
    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public List<Status> getStatus() {
        return status;
    }

    @Override
    public void setStatus(Status status) {
        this.status.clear();
        this.status.add(status);
    }

    public Order getOrder() {
        return order;
    }

    public String getSession() {
        return this.order.getSession();
    }

    @Override
    public String toString() {
        return "Request[Status=" + status + ", Order=" + order + "]";
    }
}

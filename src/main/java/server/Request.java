package server;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Request {

    private final Status status;
    private final String user;
    private final Instrument instrument;
    private final Side side;
    private final double price;
    private final int qty;
    private final long id;

    @JsonCreator
    public Request(@JsonProperty("status") Status status, @JsonProperty("user") String user, @JsonProperty("instr") Instrument instrument,
                   @JsonProperty("side") Side side, @JsonProperty("price") double price, @JsonProperty("qty") int qty, @JsonProperty("id") long id) {
        this.status = status;
        this.user = user;
        this.instrument = instrument;
        this.side = side;
        this.price = price;
        this.qty = qty;
        this.id = id;
    }

    public Request(Status status) {
        this.status = status;
        this.user = null;
        this.instrument = null;
        this.side = null;
        this.price = -1;
        this.qty = -1;
        this.id = -1;
    }

    public Status getStatus() {
        return status;
    }

    public String getUser() {
        return user;
    }

    public Instrument getInstrument() {
        return instrument;
    }

    public Side getSide() {
        return side;
    }

    public double getPrice() {
        return price;
    }

    public int getQty() {
        return qty;
    }

    public long getId() {
        return id;
    }

}

package server;

import com.fasterxml.jackson.annotation.JsonValue;

enum Side {

    SELL("Sell"), BUY("Buy");

    private final String repr;

    Side(String repr) {
        this.repr = repr;
    }

    @Override
    public String toString() {
        return repr;
    }

}

enum Status {

    Cancel("Cancel"), Order("Order"), Trade("Trade"), List("List"), Fail("Fail");

    private final String repr;

    Status(String repr) {
        this.repr = repr;
    }

    @Override
    public String toString() {
        return repr;
    }

}

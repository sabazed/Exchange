package com.exchange.exchange;

enum Side {
    SELL("Sell"), BUY("Buy");

    private final String repr;

    Side(String repr) {
        this.repr = repr;
    }

    public String getRepr() {
        return repr;
    }

}

enum Notification {
    Exchanged, Listed
}

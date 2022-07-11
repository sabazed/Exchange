package com.exchange.exchange;

import java.time.Instant;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map;

public class Listing implements Execution {

    private final Map.Entry<String, Order> lister;
    private final Instant time;

    @Override
    public Notification getStatus() {
        return Notification.Listed;
    }

    public Listing(String listerName, Order listedOrder) {
        this.lister = new SimpleEntry<>(listerName, listedOrder);
        this.time = Instant.now();
    }

    public String getLister() {
        return lister.getKey();
    }

    public Order getListedOrder() {
        return lister.getValue();
    }

    public Instant getTime() {
        return time;
    }

}

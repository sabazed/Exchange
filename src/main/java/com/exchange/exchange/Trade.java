package com.exchange.exchange;

import java.time.Instant;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;

public class Trade implements Execution {

    private final Entry<String, Order> buyer, seller;
    private final Instant time;

    @Override
    public Notification getStatus() {
        return Notification.Exchanged;
    }

    public Trade(String buyerName, Order buyOrder, String sellerName, Order sellOrder) {
        this.buyer = new SimpleEntry<>(buyerName, buyOrder);
        this.seller = new SimpleEntry<>(sellerName, sellOrder);
        time = Instant.now();
    }

    public Order getBuyOrder() {
        return buyer.getValue();
    }

    public Order getSellOrder() {
        return seller.getValue();
    }

    public String getBuyer() {
        return seller.getKey();
    }

    public String getSeller() {
        return seller.getKey();
    }

    public Instant getTime() {
        return time;
    }

}

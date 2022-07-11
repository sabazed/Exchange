package com.exchange.exchange;

import java.util.HashSet;

public class OrderBook {

    private final HashSet<Order> orders;

    public OrderBook() {
        this.orders = new HashSet<>();
    }

    public OrderBook(Order initialOrder) {
        this.orders = new HashSet<>();
        orders.add(initialOrder);
    }

    public HashSet<Order> getOrders() {
        return orders;
    }

    public boolean addOrder(Order order) {
        return orders.add(order);
    }

    public boolean removeOrder(Order order) {
        return orders.remove(order);
    }
    public boolean removeOrder(long orderID) {
        try {
            return orders.remove((Order) (orders.stream().filter(order -> order.getId() == orderID).toArray()[0]));
        }
        catch (ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
            return false;
        }
    }

    public Order matchOrder(Order newOrder) {
        for (var order : orders) {
            if (order.compare(newOrder)) return order;
        }
        return null;
    }

}

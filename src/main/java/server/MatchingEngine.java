package server;

import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.concurrent.LinkedBlockingQueue;

public class MatchingEngine implements Runnable {

    // Use a LinkedBlockingQueue to receive new orders
    protected static final LinkedBlockingQueue<Order> neworders = new LinkedBlockingQueue<>();
    // Keep an Order Book for storing all orders
    private static final OrderBook orderbook = new OrderBook();

    // Keep count of orders to give them unique IDs
    private static long ID = 0;

    // Main method that the thread will run on
    private static void processMessages() {
        // Get the orders from the book
        HashMap<Instrument, HashMap<Side, TreeSet<Order>>> orders = orderbook.getOrders();
        try {
            // Run endlessly
            while (true) {
                // Receive order from the queue, if it is empty - wait for it.
                Order order = neworders.take();
                // Check if the instrument exists in the order book and if not, add it
                HashMap<Side, TreeSet<Order>> instr = orders.get(order.getInstrument());
                if (instr == null) {
                    instr = orderbook.addInstrument(order.getInstrument());
                }
                TreeSet<Order> sellTree = instr.get(Side.SELL);
                TreeSet<Order> buyTree = instr.get(Side.BUY);
                // Check if the ID != -1 (if it is not a new order, so it is a cancel order)
                if (order.getId() != -1) {
                    instr.get(order.getSide()).remove(order);
                    OrderEntryGateway.send(new Request(Status.Cancel, order.getUser(), order.getInstrument(),
                                            order.getSide(), order.getPrice(), order.getQty(), order.getId()), order.getSession());
                }
                else {
                    // Loop until all available orders are exchanged
                    boolean repeatMatching = true;
                    while (repeatMatching) {
                        repeatMatching = false;
                        // If we have sell order we should look for buy orders with the same or greater price
                        if (order.getSide() == Side.SELL) {
                            // Iterate over the orders if the users are the same
                            Order matched = buyTree.ceiling(order);
                            if (matched != null && matched.getUser().equals(order.getUser())) {
                                Iterator<Order> iterator = buyTree.tailSet(matched, false).iterator();
                                while (iterator.hasNext() && matched.getUser().equals(order.getUser()))
                                    matched = iterator.next();
                                if (matched != null && matched.getUser().equals(order.getUser())) matched = null;
                            }
                            // If we couldn't find a match add our order to the tree
                            if (matched == null) {
                                order.setId(ID++);
                                sellTree.add(order);
                                OrderEntryGateway.send(new Request(Status.List, order.getUser(), order.getInstrument(),
                                        order.getSide(), order.getPrice(), order.getQty(), order.getId()), order.getSession());
                            }
                            // Otherwise, trade it and update quantity or remove the whole order if qty is less or equal to 0
                            else {
                                // Calculate new quantity for the matched order, remove if 0
                                if (matched.getQty() >= order.getQty()) {
                                    matched.setQty(matched.getQty() - order.getQty());
                                    if (matched.getQty() == 0) buyTree.remove(matched);
                                    OrderEntryGateway.send(new Request(Status.Trade, order.getUser(), order.getInstrument(),
                                            order.getSide(), order.getPrice(), order.getQty(), order.getId()), order.getSession());
                                }
                                // Remove matched if less than 0, update order and keep it for next iteration
                                else {
                                    order.setQty(order.getQty() - matched.getQty());
                                    buyTree.remove(matched);
                                    matched.setQty(0);
                                    repeatMatching = true;
                                }
                                OrderEntryGateway.send(new Request(Status.Trade, matched.getUser(), matched.getInstrument(),
                                        matched.getSide(), matched.getPrice(), matched.getQty(), matched.getId()), matched.getSession());
                            }
                        }
                        // If we have sell order we should look for buy orders with the lowest price
                        else {
                            // Do everything the same way except only check the lowest sell order for matching
                            Iterator<Order> iterator = sellTree.iterator();
                            Order matched = iterator.hasNext() ? iterator.next() : null;
                            while (iterator.hasNext() && matched.getUser().equals(order.getUser())) matched = iterator.next();
                            if (matched != null && matched.getUser().equals(order.getUser())) matched = null;
                            if (matched == null) {
                                order.setId(ID++);
                                buyTree.add(order);
                                OrderEntryGateway.send(new Request(Status.List, order.getUser(), order.getInstrument(),
                                        order.getSide(), order.getPrice(), order.getQty(), order.getId()), order.getSession());
                            }
                            else {
                                if (matched.getQty() >= order.getQty()) {
                                    matched.setQty(matched.getQty() - order.getQty());
                                    if (matched.getQty() == 0) {
                                        sellTree.pollFirst();
                                    }
                                    OrderEntryGateway.send(new Request(Status.Trade, order.getUser(), order.getInstrument(),
                                            order.getSide(), order.getPrice(), order.getQty(), order.getId()), order.getSession());
                                }
                                else {
                                    order.setQty(order.getQty() - matched.getQty());
                                    sellTree.pollFirst();
                                    matched.setQty(0);
                                    repeatMatching = true;
                                }
                                OrderEntryGateway.send(new Request(Status.Trade, matched.getUser(), matched.getInstrument(),
                                        matched.getSide(), matched.getPrice(), matched.getQty(), matched.getId()), matched.getSession());
                            }
                        }
                    }
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void run() {
        processMessages();
    }

}

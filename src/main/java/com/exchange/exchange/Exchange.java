package com.exchange.exchange;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.HashMap;

public class Exchange {

    private static final HashMap<String, OrderBook> users = new HashMap<>();
    private static final JSONParser parser = new JSONParser();

    public static String processRequest(String request) {

        try {
            JSONObject orderJson = (JSONObject) parser.parse(request);
            if (((String) orderJson.get("status")).equals("order")) {
                return processOrder(orderJson);
            }
            else if (((String) orderJson.get("status")).equals("remove")) {
                return removeListing(orderJson);
            }
            else {
                return null;
            }
        } catch (ParseException | NullPointerException | NumberFormatException e) {
            e.printStackTrace();
            return null;
        }

    }

    public static String processOrder(JSONObject orderJson) {

        try {
            JSONObject data = (JSONObject) parser.parse((orderJson.get("data").toString()));

            String user = (String) data.get("user");
            Instrument instr = new Instrument(Integer.parseInt((String) data.get("instr")));
            double price = Double.parseDouble((String) data.get("price"));
            int qty = Integer.parseInt((String) data.get("qty"));
            Side side = (data.get("side")).equals("Buy") ? Side.BUY : Side.SELL;

            Execution result = matchingEngine(user, new Order(instr, side, price, qty));

            if (result.getStatus() == Notification.Exchanged) {
                Trade order = (Trade) result;
                users.get(order.getBuyer()).removeOrder(order.getBuyOrder());
                users.get(order.getSeller()).removeOrder(order.getSellOrder());
                return String.format("{\"status\":\"trade\", \"bought\":%d, \"sold\":%d}", order.getBuyOrder().getId(), order.getSellOrder().getId());
            }
            else if (result.getStatus() == Notification.Listed) {
                Listing order = (Listing) result;
                users.get(order.getLister()).addOrder(order.getListedOrder());
                return String.format(
                        "{\"status\":\"listing\", \"user\":%s, \"order\":{\"instr\":%d, \"price\":%f, \"qty\":%d, \"side\":\"%s\", \"id\":%d}}",
                        order.getLister(), order.getListedOrder().getInstrumentID().getId(), order.getListedOrder().getPrice(),
                        order.getListedOrder().getQty(),  order.getListedOrder().getSide().getRepr(), order.getListedOrder().getId());
            }
            else {
                return null;
            }
        } catch (ParseException | NullPointerException | NumberFormatException  e) {
            e.printStackTrace();
            return null;
        }

    }

    private static String removeListing(JSONObject orderJson) {

        try {
            String user = (String) orderJson.get("user");
            long orderId = Long.parseLong(orderJson.get("id").toString());
            users.get(user).removeOrder(orderId);
            return String.format("{\"status\":\"removed\", \"id\":%s}", orderId);
        }
        catch (NullPointerException | NumberFormatException  e) {
            e.printStackTrace();
            return "";
        }

    }

    private static Execution matchingEngine(String currentUser, Order newOrder) {
        // Check if the new order matches any placed orders by different users
        for (var user : users.entrySet()) {
            Order matched = user.getValue().matchOrder(newOrder);
            if (matched != null && !user.getKey().equals(currentUser)) {
                user.getValue().removeOrder(matched);
                return new Trade(currentUser, newOrder, user.getKey(), matched);
            }
        }
        // If not matched, then add the order to the user's order book
        OrderBook userBooks = users.getOrDefault(currentUser, null);
        if (userBooks != null) {
            userBooks.addOrder(newOrder);
        } else users.put(currentUser, new OrderBook(newOrder));
        return new Listing(currentUser, newOrder);
    }

}

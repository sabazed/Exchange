package com.exchange.exchange;

import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;

import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;

@ServerEndpoint("/order")
public class MainServer {

    Queue<Session> queue = new ConcurrentLinkedDeque<>();

    public void send(String message) {
        try {
            for (Session session : queue)
                session.getBasicRemote().sendText(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @OnMessage
    public void message(Session session, String message) {
        try {
            String result = Exchange.processRequest(message);
            if (result == null) {
                session.getBasicRemote().sendText("{\"status\":\"forder\"}");
            }
            else if (result.equals("")) {
                session.getBasicRemote().sendText("{\"status\":\"fremove\"}");
            }
            else {
                send(result);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @OnOpen
    public void open(Session session) {
        queue.add(session);
    }

    @OnClose
    public void close(Session session) {
        queue.remove(session);
    }

    @OnError
    public void error(Session session, Throwable t) {
        t.printStackTrace();
    }

}

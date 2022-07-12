package server;

import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

@ServerEndpoint(value = "/order", encoders = RequestEncoder.class, decoders = RequestDecoder.class)
public class OrderEntryGateway {

    private static final Set<Session> sessions = new HashSet<>();

    public static void send(Request request, Session session) {
        try {
            session.getBasicRemote().sendObject(request);
        } catch (IOException | EncodeException e) {
            e.printStackTrace();
        }
    }

    @OnMessage
    public void onMessage(Session session, Request req) {
        try {
            if (req.getInstrument().getId() == null) {
                send(new Request(Status.Fail), session);
            }
            else {
                MatchingEngine.neworders.put(new Order(req.getUser(), session, req.getInstrument(),
                        req.getSide(), req.getPrice(), req.getQty(), req.getId()));
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @OnOpen
    public void onOpen(Session session) {
        sessions.add(session);
    }

    @OnClose
    public void onClose(Session session) {
        sessions.remove(session);
    }

    @OnError
    public void onError(Session session, Throwable t) {
        t.printStackTrace();
    }

}

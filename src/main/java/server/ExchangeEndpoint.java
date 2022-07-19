package server;

import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

@ServerEndpoint(value = "/order", encoders = RequestEncoder.class, decoders = RequestDecoder.class)
public class ExchangeEndpoint {

    private static final Logger LOG = LogManager.getLogger(OrderEntryGateway.class);

    // Current gateway to send messages to
    private OrderEntryGateway gateway;
    // Session of the current endpoint
    private Session session;

    public Session getSession() {
        return session;
    }

    // Method for sending a Message to the remote endpoint
    public void sendMessage(Message message) {
        try {
            session.getBasicRemote().sendObject(message);
        } catch (IOException e) {
            LOG.warn("Connection to session {} interrupted!", session);
            e.printStackTrace();
        } catch (EncodeException e) {
            LOG.warn("Couldn't encode for session {} with {}", session, message);
            e.printStackTrace();
        }
    }

    @OnOpen
    public void onOpen(Session session) {
        LOG.info("New client connected with session {}", session.getId());
        this.session = session;
        this.gateway = ExchangeServletContextListener.getGateWay();
        this.gateway.addEndpoint(this.getSession().getId(), this);
    }

    @OnMessage
    public void onMessage(Session session, Request request) {
        LOG.info("New request from session {}, data - {}", session.getId(), request);
        if (!request.isValid()) {
            request.getOrder().setSession(session.getId());
            sendMessage(request);
        }
        else {
            request.getOrder().setSession(session.getId());
            gateway.processMessage(request);
        }
    }

    @OnClose
    public void onClose(Session session) {
        LOG.info("Client connected with session {}", session.getId());
        gateway.removeEndpoint(this.getSession().getId());
    }

    @OnError
    public void onError(Session session, Throwable t) {
        LOG.error("Caused exception at session {}", session.getId());
        // TODO
        t.printStackTrace();
    }

}

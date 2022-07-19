package server;

import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

@ServerEndpoint(value = "/order", encoders = MessageEncoder.class, decoders = MessageDecoder.class)
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
            // TODO
        } catch (EncodeException e) {
            LOG.warn("Couldn't encode for session {} with {}", session, message);
            e.printStackTrace();
            // TODO
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
    public void onMessage(Session session, Message message) {
        if (message != null) {
            LOG.info("New response from session {}, data - {}", session.getId(), message);
            message.setSession(session.getId());
            if (message instanceof Fail) {
                sendMessage(message);
            }
            else {
                gateway.processMessage(message);
            }
        }
        else {} //TODO
    }

    @OnClose
    public void onClose(Session session) {
        LOG.info("Client connected with session {}", session.getId());
        gateway.removeEndpoint(this.getSession().getId());
    }

    @OnError
    public void onError(Session session, Throwable t) {
        LOG.error("Exception at session {}", session.getId());
        t.printStackTrace();
        // TODO
    }

}

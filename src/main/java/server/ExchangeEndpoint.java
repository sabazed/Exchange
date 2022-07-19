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

    // Method for sending a Message to the remote endpoint
    public void sendMessage(Message message) {
        try {
            session.getBasicRemote().sendObject(message);
        } catch (IOException e) {
            LOG.error("Connection to session {} interrupted!", session);
            e.printStackTrace();
            gateway.removeEndpoint(session.getId());
        } catch (EncodeException e) {
            LOG.warn("Couldn't encode for session {} with {}", session, message);
            e.printStackTrace();
            // TODO
        }
    }

    public void closeEndpoint() {
        sendMessage(new Fail(Status.FatalFail));
        gateway.removeEndpoint(session.getId());
        try {
            LOG.info("Closing session {}", session.getId());
            session.close(new CloseReason(CloseReason.CloseCodes.UNEXPECTED_CONDITION, "Unexpected exception happened, service stopped"));
        } catch (IOException e) {
            e.printStackTrace();
            LOG.warn("Couldn't close session {}", session.getId());
        }
    }

    @OnOpen
    public void onOpen(Session session) {
        LOG.info("New client connected with session {}", session.getId());
        this.session = session;
        this.gateway = ExchangeServletContextListener.getGateWay();
        this.gateway.addEndpoint(session.getId(), this);
    }

    @OnMessage
    public void onMessage(Session session, Message message) {
        LOG.info("New response from session {}, data - {}", session.getId(), message);
        message.setSession(session.getId());
        if (message instanceof Fail) {
            sendMessage(message);
        }
        else {
            gateway.processMessage(message);
        }
    }

    @OnClose
    public void onClose(Session session) {
        LOG.info("Client connected with session {}", session.getId());
        gateway.removeEndpoint(session.getId());
    }

    @OnError
    public void onError(Session session, Throwable t) {
        LOG.error(t.getClass().getName() + " at session {}, disconnecting...", session.getId());
        t.printStackTrace();
        gateway.removeEndpoint(session.getId());
    }

}

package server;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpSession;
import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

@ServerEndpoint(value = "/order", encoders = MessageEncoder.class, decoders = MessageDecoder.class, configurator = ExchangeServerEndpointConfig.class)
public class ExchangeEndpoint implements MessageBusService {

    private static final Logger LOG = LogManager.getLogger(OrderEntryGateway.class);

    // Exchange message bus for communication
    private ExchangeBus exchangeBus;
    // Session of the current endpoint
    private Session session;

    // Method for sending a Message to the remote endpoint
    public void processMessage(Message message) {
        try {
            session.getBasicRemote().sendObject(message);
            LOG.info("Sent session {} the message - {}", session.getId(), message);
        } catch (IOException e) {
            LOG.error("Connection to session {} interrupted!", session);
            e.printStackTrace();
        } catch (EncodeException e) {
            LOG.warn("Couldn't encode for session {} with {}", session, message);
            e.printStackTrace();
        }
    }

    @OnOpen
    public void onOpen(Session session, EndpointConfig config) {
        LOG.info("New client connected with session {}", session.getId());
        this.session = session;
        // Get the servlet context through custom config with added user attribute
        ServletContext ctx = (ServletContext) config.getUserProperties().get(ServletContext.class.getName());
        // Get the actual listener object which added itself also as an attribute during context initialization
        ExchangeServletContextListener listener = (ExchangeServletContextListener) ctx.getAttribute(ExchangeServletContextListener.class.getName());
        // Get the exchange bus and register the endpoint
        this.exchangeBus = (ExchangeBus) listener.getExchangeBus();
        this.exchangeBus.registerService(session.getId(), this);
    }

    @OnMessage
    public void onMessage(Session session, Message message) {
        LOG.info("New response from session {}, data - {}", session.getId(), message);
        message.setSession(session.getId());
        if (message instanceof Fail) {
            processMessage(message);
        }
        else {
            exchangeBus.sendMessage(OrderEntryGateway.class.getName(), message);
        }
    }

    @OnClose
    public void onClose(Session session) {
        LOG.info("Client connected with session {}", session.getId());
        exchangeBus.unregisterService(session.getId());
    }

    @OnError
    public void onError(Session session, Throwable t) {
        LOG.error(t.getClass().getName() + " at session {}, disconnecting...", session.getId());
        exchangeBus.unregisterService(session.getId());
        t.printStackTrace();
    }

}

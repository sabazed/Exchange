package exchange.endpoint;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import exchange.enums.Status;
import exchange.messages.Fail;
import exchange.messages.Message;
import exchange.services.MessageBusService;
import exchange.services.OrderEntryGateway;
import exchange.bus.ExchangeBus;
import exchange.bus.MessageBus;
import jakarta.servlet.ServletContext;
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
        } catch (IOException | EncodeException e) {
            LOG.error("Could not send session {} object {}!", session.getId(), message);
            LOG.error(e);
        }
    }

    @OnOpen
    public void onOpen(Session session, EndpointConfig config) {
        LOG.info("New client connected with session {}", session.getId());
        this.session = session;
        // Get the servlet context through custom config with added user attribute
        ServletContext ctx = (ServletContext) config.getUserProperties().get(ServletContext.class.getName());
        // Get the exchange bus and register the endpoint
        this.exchangeBus = (ExchangeBus) ctx.getAttribute(MessageBus.class.getName());
        this.exchangeBus.registerService("ServerEndpoint_" + session.getId(), this);
    }

    @OnMessage
    public void onMessage(Session session, Message message) {
        LOG.info("New response from session {}, data - {}", session.getId(), message);
        message.setSession(session.getId());
        if (message instanceof Fail) {
            processMessage(message);
        }
        else {
            exchangeBus.sendMessage("OrderEntryGateway_0", message);
        }
    }

    @OnClose
    public void onClose(Session session) {
        LOG.info("Client disconnected with session {}", session.getId());
        exchangeBus.unregisterService(session.getId());
    }

    @OnError
    public void onError(Session session, Throwable t) {
        if (t.getCause() instanceof InvalidFormatException e) {
            // Check which is invalid - price or qty
            String errorMessage = e.getPathReference();
            int start = errorMessage.indexOf("\"");
            int end = errorMessage.lastIndexOf("\"");
            String field = errorMessage.substring(start + 1, end);
            if (field.equals("price")) {
                processMessage(new Fail(Status.Price));
            }
            else {
                processMessage(new Fail(Status.Quantity));
            }
        }
        else {
            LOG.error(t.getClass().getName() + " at session {}, disconnecting...", session.getId());
            LOG.error(t.getCause());
        }
        // TODO: Stop websocket from closing
    }

}

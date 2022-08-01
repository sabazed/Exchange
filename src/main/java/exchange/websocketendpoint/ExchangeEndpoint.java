package exchange.websocketendpoint;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import exchange.bus.ExchangeBus;
import exchange.bus.MessageBus;
import exchange.enums.Status;
import exchange.messages.Fail;
import exchange.messages.Message;
import exchange.services.MessageBusService;
import jakarta.servlet.ServletContext;
import jakarta.websocket.CloseReason;
import jakarta.websocket.EncodeException;
import jakarta.websocket.Endpoint;
import jakarta.websocket.EndpointConfig;
import jakarta.websocket.MessageHandler;
import jakarta.websocket.Session;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class ExchangeEndpoint extends Endpoint implements MessageBusService {

    private static final Logger LOG = LogManager.getLogger(ExchangeEndpoint.class);

    // Exchange message bus for communication
    private ExchangeBus exchangeBus;
    // Session of the current endpoint
    private Session session;
    // Service IDs
    private String selfId;
    private String gatewayId;

    // Method for sending a Message to the remote endpoint
    public void issueMessage(Message message) {
        try {
            session.getBasicRemote().sendObject(message);
            LOG.info("Sent session {} the message - {}", session.getId(), message);
        } catch (IOException | EncodeException e) {
            LOG.error("Could not send session {} object {}!", session.getId(), message, e);
        }
    }

    @Override
    public void onOpen(Session session, EndpointConfig config) {

        LOG.info("New client connected with session {}", session.getId());
        this.session = session;

        // Get the servlet context through custom config with added user attribute
        ServletContext ctx = (ServletContext) config.getUserProperties().get(ServletContext.class.getName());

        // Get service IDs from the context attributes
        this.gatewayId = (String) ctx.getAttribute("GatewayId");
        this.selfId = (String) ctx.getAttribute("EndpointId");

        // Get the exchange bus and register the endpoint
        this.exchangeBus = (ExchangeBus) ctx.getAttribute(MessageBus.class.getName());
        this.exchangeBus.registerService(selfId + session.getId(), this);

        session.addMessageHandler(new MessageHandler.Whole<Message>() {
            @Override
            public void onMessage(Message message) {
                message.setSession(session.getId());
                LOG.info("New response from session {}, data - {}", session.getId(), message);
                exchangeBus.sendMessage(gatewayId, message);
            }
        });

    }

    @Override
    public void onClose(Session session, CloseReason closeReason) {
        LOG.info("Client disconnected with session {}", session.getId());
        exchangeBus.unregisterService(selfId + session.getId());
    }

    @Override
    public void onError(Session session, Throwable t) {
        if (t.getCause() instanceof InvalidFormatException e) {
            // Check which is invalid - price or qty
            String errorMessage = e.getPathReference();
            int start = errorMessage.indexOf("\"");
            int end = errorMessage.lastIndexOf("\"");
            String field = errorMessage.substring(start + 1, end);
            if (field.equals("price")) {
                issueMessage(new Fail(Status.Price));
            }
            else {
                issueMessage(new Fail(Status.Quantity));
            }
            LOG.error("{} at session {}, disconnecting...", t.getClass().getName(), session.getId(), t.getCause());
        }
        else {
            LOG.error("{} at session {}, disconnecting...", t.getClass().getName(), session.getId(), t.getCause());
        }
        // TODO: Stop websocket from closing
    }

}

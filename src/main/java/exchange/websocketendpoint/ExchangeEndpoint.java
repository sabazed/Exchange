package exchange.websocketendpoint;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import exchange.common.Instrument;
import exchange.enums.Status;
import exchange.messages.Fail;
import exchange.messages.Message;
import exchange.services.MessageBusService;
import exchange.services.OrderEntryGateway;
import exchange.bus.ExchangeBus;
import exchange.bus.MessageBus;
import jakarta.servlet.ServletContext;
import jakarta.websocket.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.List;

public class ExchangeEndpoint extends Endpoint implements MessageBusService {

    private static final Logger LOG = LogManager.getLogger("exchangeLogger");

    // Exchange message bus for communication
    private ExchangeBus exchangeBus;
    // Instrument loader for sending current instrument data to the websocket
    private InstrumentLoader loader;
    // Session of the current endpoint
    private Session session;

    // Method for sending a Message to the remote endpoint
    public void processMessage(Message message) {
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
        // Get InstrumentLoader from saved attributes
        this.loader = (InstrumentLoader) ctx.getAttribute(InstrumentLoader.class.getName());
        // Get the exchange bus and register the endpoint
        this.exchangeBus = (ExchangeBus) ctx.getAttribute(MessageBus.class.getName());
        this.exchangeBus.registerService("ServerEndpoint_" + session.getId(), this);
        session.addMessageHandler(new MessageHandler.Whole<Message>() {
            @Override
            public void onMessage(Message message) {
                LOG.info("New response from session {}, data - {}", session.getId(), message);
                message.setSession(session.getId());
                if (message instanceof Fail) {
                    processMessage(message);
                }
                else {
                    exchangeBus.sendMessage("OrderEntryGateway", message);
                }
            }
        });
    }

    @Override
    public void onClose(Session session, CloseReason closeReason) {
        LOG.info("Client disconnected with session {}", session.getId());
        exchangeBus.unregisterService(session.getId());
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
                processMessage(new Fail(Status.Price));
            }
            else {
                processMessage(new Fail(Status.Quantity));
            }
            LOG.error(t.getClass().getName() + " at session {}, disconnecting...", session.getId(), t.getCause());
        }
        else {
            LOG.error(t.getClass().getName() + " at session {}, disconnecting...", session.getId(), t.getCause());
        }
        // TODO: Stop websocket from closing
    }

}

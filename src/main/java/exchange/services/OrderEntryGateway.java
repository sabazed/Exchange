package exchange.services;

import exchange.bus.MessageBus;
import exchange.enums.Status;
import exchange.messages.Cancel;
import exchange.messages.Fail;
import exchange.messages.Message;
import exchange.messages.Order;
import exchange.messages.Request;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class OrderEntryGateway extends MessageProcessor {

    private static final Logger LOG = LogManager.getLogger("exchangeLogger");

    private final BlockingQueue<Message> messages;
    private final MessageBus exchangeBus;
    private final String endpointId;
    private final String engineId;
    private final String providerId;
    private final String selfId;

    public OrderEntryGateway(MessageBus messageBus, String engineId, String providerId, String endpointId, String selfId) {
        super();
        messages = new LinkedBlockingQueue<>();
        exchangeBus = messageBus;
        this.endpointId = endpointId;
        this.engineId = engineId;
        this.providerId = providerId;
        this.selfId = selfId;
    }

    public String getSelfId() {
        return selfId;
    }

    @Override
    public void processMessage(Message message) {
        try {
            messages.put(message);
        } catch (InterruptedException e) {
            LOG.error("Thread interrupted, aborting...", e);
            stop();
        }
    }

    @Override
    protected void processMessages() {
        LOG.info("OrderEntryGateway up and running!");
        while (running) {
            try {
                Message message = messages.take();
                LOG.info("Processing new {}", message);
                // Check that message session or client id isn't null, otherwise return it as a Fail object
                if (message.getSession() == null || message.getClientId() == null)
                    message = new Fail(Status.OrderFail);
                // Determine where to send the message, back to the endpoint or to the engine
                if (message instanceof Order || message instanceof Cancel) {
                    exchangeBus.sendMessage(engineId, message);
                }
                else if (message instanceof Request) {
                    exchangeBus.sendMessage(providerId, message);
                }
                else exchangeBus.sendMessage(endpointId + message.getSession(), message);
            }
            catch (InterruptedException e) {
                LOG.error("OrderEntryGateway interrupted!", e);
                stop();
            }
        }
        LOG.info("OrderEntryGateway stopped working...");
    }



}

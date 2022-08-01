package exchange.services;

import exchange.bus.MessageBus;
import exchange.messages.Cancel;
import exchange.messages.InstrumentDataRequest;
import exchange.messages.MarketDataRequest;
import exchange.messages.Message;
import exchange.messages.Order;
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
    private final String referenceProviderId;
    private final String marketProviderId;
    private final String selfId;

    public OrderEntryGateway(MessageBus messageBus, String engineId, String referenceProviderId, String marketProviderId, String endpointId, String selfId) {
        messages = new LinkedBlockingQueue<>();
        exchangeBus = messageBus;
        this.endpointId = endpointId;
        this.engineId = engineId;
        this.referenceProviderId = referenceProviderId;
        this.marketProviderId = marketProviderId;
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
                if (message.getSession() == null || message.getClientId() == null) {
                    LOG.warn("Couldn't process message as the provided data was invalid - {}", message);
                }
                // Determine where to send the message, back to the endpoint or to the engine
                else if (message instanceof Order || message instanceof Cancel) {
                    exchangeBus.sendMessage(engineId, message);
                }
                else if (message instanceof InstrumentDataRequest) {
                    exchangeBus.sendMessage(referenceProviderId, message);
                }
                else if (message instanceof MarketDataRequest) {
                    exchangeBus.sendMessage(marketProviderId, message);
                }
                else {
                    exchangeBus.sendMessage(endpointId + message.getSession(), message);
                }
            }
            catch (InterruptedException e) {
                LOG.error("OrderEntryGateway interrupted!", e);
                stop();
            }
        }
        LOG.info("OrderEntryGateway stopped working...");
    }



}

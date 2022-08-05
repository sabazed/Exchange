package exchange.services;

import exchange.bus.MessageBus;
import exchange.messages.Cancel;
import exchange.messages.InstrumentDataRequest;
import exchange.messages.MarketDataRequest;
import exchange.messages.MarketDataUnsubscribe;
import exchange.messages.Message;
import exchange.messages.Order;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class OrderEntryGateway extends MessageProcessor {

    private static final Logger LOG = LogManager.getLogger(OrderEntryGateway.class);

    private String endpointId;
    private String engineId;
    private String referenceProviderId;
    private String marketProviderId;

    public OrderEntryGateway(MessageBus messageBus) {
        super(messageBus);
    }

    public String getSelfId() {
        return selfId;
    }

    public void setEndpointId(String endpointId) {
        this.endpointId = endpointId;
    }

    public void setEngineId(String engineId) {
        this.engineId = engineId;
    }

    public void setReferenceProviderId(String referenceProviderId) {
        this.referenceProviderId = referenceProviderId;
    }

    public void setMarketProviderId(String marketProviderId) {
        this.marketProviderId = marketProviderId;
    }

    @Override
    protected void processMessage(Message message) {
        // Determine where to send the message, back to the endpoint or to the engine
        if (message instanceof Order || message instanceof Cancel) {
            exchangeBus.sendMessage(engineId, message);
        }
        else if (message instanceof InstrumentDataRequest) {
            exchangeBus.sendMessage(referenceProviderId, message);
        }
        else if (message instanceof MarketDataRequest || message instanceof MarketDataUnsubscribe) {
            exchangeBus.sendMessage(marketProviderId, message);
        }
        else {
            exchangeBus.sendMessage(endpointId + message.getSession(), message);
        }
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }

}

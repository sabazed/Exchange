package exchange.services;

import exchange.bus.MessageBus;
import exchange.messages.Cancel;
import exchange.messages.InstrumentDataRequest;
import exchange.messages.MarketDataRequest;
import exchange.messages.Message;
import exchange.messages.Order;

public class OrderEntryGateway extends MessageProcessor {

    private final String endpointId;
    private final String engineId;
    private final String referenceProviderId;
    private final String marketProviderId;

    public OrderEntryGateway(MessageBus messageBus, String engineId, String referenceProviderId, String marketProviderId, String endpointId, String selfId) {
        super(messageBus, selfId, OrderEntryGateway.class);
        this.endpointId = endpointId;
        this.engineId = engineId;
        this.referenceProviderId = referenceProviderId;
        this.marketProviderId = marketProviderId;
    }

    public String getSelfId() {
        return selfId;
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
        else if (message instanceof MarketDataRequest) {
            exchangeBus.sendMessage(marketProviderId, message);
        }
        else {
            exchangeBus.sendMessage(endpointId + message.getSession(), message);
        }
    }

}

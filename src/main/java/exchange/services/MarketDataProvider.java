package exchange.services;

import exchange.bus.MessageBus;
import exchange.common.MarketDataEntry;
import exchange.messages.MarketDataRequest;
import exchange.messages.MarketDataResponse;
import exchange.messages.MarketDataUpdate;
import exchange.messages.Message;
import exchange.messages.UnsubscribeRequest;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class MarketDataProvider extends MessageProcessor {

    private final Set<MarketDataEntry> marketData;
    private final Set<String> subscribers;
    private final String gatewayId;

    public MarketDataProvider(MessageBus messageBus, String gatewayId, String selfId) {
        super(messageBus, selfId, MarketDataProvider.class);
        marketData = ConcurrentHashMap.newKeySet();
        subscribers = ConcurrentHashMap.newKeySet();
        this.gatewayId = gatewayId;
    }

    @Override
    public String getSelfId() {
        return selfId;
    }

    @Override
    protected void processMessage(Message message) {
        if (message instanceof MarketDataUpdate update) {
            // Replace the existing market data
            marketData.remove(update.getUpdate());
            marketData.add(update.getUpdate());
            // Send the message to every endpoint
            subscribers.forEach(id -> {
                exchangeBus.sendMessage(gatewayId, new MarketDataResponse(update, id));
            });
        }
        else if (message instanceof MarketDataRequest) {
            subscribers.add(message.getSession());
            exchangeBus.sendMessage(gatewayId, new MarketDataResponse(message, marketData));
        }
        else if (message instanceof UnsubscribeRequest) {
            subscribers.remove(message.getSession());
        }
        else {
            LOG.error("Invalid message type received, exiting - {}", message);
            throw new IllegalMessageException(message.toString());
        }
    }

}

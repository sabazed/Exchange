package exchange.services;

import exchange.bus.MessageBus;
import exchange.common.MarketDataEntry;
import exchange.messages.MarketDataRequest;
import exchange.messages.MarketDataResponse;
import exchange.messages.MarketDataUpdate;
import exchange.messages.Message;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class MarketDataProvider extends MessageProcessor {

    private final Set<MarketDataEntry> marketData;
    private final Set<String> endpoints;
    private final String gatewayId;

    public MarketDataProvider(MessageBus messageBus, String gatewayId, String selfId) {
        super(messageBus, selfId, MarketDataProvider.class);
        marketData = ConcurrentHashMap.newKeySet();
        endpoints = ConcurrentHashMap.newKeySet();
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
            endpoints.forEach(endpoint -> {
                exchangeBus.sendMessage(gatewayId, new MarketDataResponse(update, endpoint));
            });
        }
        else if (message instanceof MarketDataRequest){
            endpoints.add(message.getSession());
            exchangeBus.sendMessage(gatewayId, new MarketDataResponse(message, marketData));
        }
        else {
            LOG.error("Invalid message type received, exiting - {}", message);
            throw new IllegalMessageException(message.toString());
        }
    }

}

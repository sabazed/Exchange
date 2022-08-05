package exchange.services;

import exchange.bus.MessageBus;
import exchange.common.MarketDataEntry;
import exchange.messages.MarketDataRequest;
import exchange.messages.MarketDataResponse;
import exchange.messages.MarketDataUnsubscribe;
import exchange.messages.MarketDataUpdate;
import exchange.messages.Message;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class MarketDataProvider extends MessageProcessor {

    private static final Logger LOG = LogManager.getLogger(MarketDataProvider.class);

    private final Set<MarketDataEntry> marketData;
    private final Set<String> subscribers;
    private String gatewayId;

    public MarketDataProvider(MessageBus messageBus) {
        super(messageBus);
        marketData = ConcurrentHashMap.newKeySet();
        subscribers = ConcurrentHashMap.newKeySet();
    }

    @Override
    public String getSelfId() {
        return selfId;
    }

    public void setGatewayId(String gatewayId) {
        this.gatewayId = gatewayId;
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
        else if (message instanceof MarketDataUnsubscribe) {
            subscribers.remove(message.getSession());
        }
        else {
            LOG.error("Invalid message type received, exiting - {}", message);
            throw new IllegalMessageException(message.toString());
        }
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }

}

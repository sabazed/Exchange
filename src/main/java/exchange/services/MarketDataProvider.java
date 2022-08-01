package exchange.services;

import exchange.bus.MessageBus;
import exchange.common.Instrument;
import exchange.common.MarketDataEntry;
import exchange.messages.MarketDataResponse;
import exchange.messages.Message;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

public class MarketDataProvider extends MessageProcessor {

    private static final Logger LOG = LogManager.getLogger("exchangeLogger");

    private final BlockingQueue<Message> updates;
    private final Set<MarketDataEntry> marketData;
    private final Set<String> endpoints;
    private final MessageBus exchangeBus;
    private final String gatewayId;
    private final String selfId;

    public MarketDataProvider(MessageBus exchangeBus, String gatewayId, String selfId) {
        updates = new LinkedBlockingQueue<>();
        marketData = ConcurrentHashMap.newKeySet();
        endpoints = ConcurrentHashMap.newKeySet();
        this.exchangeBus = exchangeBus;
        this.gatewayId = gatewayId;
        this.selfId = selfId;
    }

    @Override
    public String getSelfId() {
        return selfId;
    }

    @Override
    public void processMessage(Message message) {
        try {
            updates.put(message);
        } catch (InterruptedException e) {
            LOG.error("Thread interrupted, aborting...", e);
            stop();
        }
    }

    @Override
    protected void processMessages() {
        LOG.info("MarketDataProvider up and running!");
        while (running) {
            try {
                Message update = updates.take();
                LOG.info("Processing new {}", update);
                if (update instanceof MarketDataResponse marketDataUpdate) {
                    // Replace the existing market data
                    marketDataUpdate.getUpdates().forEach(data -> {
                        marketData.remove(data);
                        marketData.add(data);
                    });
                    // Send the update to every endpoint
                    endpoints.forEach(endpoint -> {
                        exchangeBus.sendMessage(gatewayId, new MarketDataResponse(marketDataUpdate, endpoint));
                    });
                }
                else {
                    endpoints.add(update.getSession());
                    exchangeBus.sendMessage(gatewayId, new MarketDataResponse(update, null, List.copyOf(marketData)));
                }
            }
            catch (InterruptedException e) {
                LOG.error("MarketDataProvider interrupted!", e);
                stop();
            }
        }
        LOG.info("MarketDataProvider stopped working...");
    }
}

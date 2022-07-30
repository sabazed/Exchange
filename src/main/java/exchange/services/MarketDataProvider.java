package exchange.services;

import exchange.bus.MessageBus;
import exchange.messages.MarketData;
import exchange.messages.Message;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class MarketDataProvider extends MessageProcessor {

    private static final Logger LOG = LogManager.getLogger("exchangeLogger");

    private final BlockingQueue<Message> updates;
    private final Set<String> endpoints;
    private final MessageBus exchangeBus;
    private final String gatewayId;
    private final String selfId;

    public MarketDataProvider(MessageBus exchangeBus, String gatewayId, String selfId) {
        updates = new LinkedBlockingQueue<>();
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
                if (update instanceof MarketData marketData) {
                    endpoints.forEach(endpoint -> {
                        exchangeBus.sendMessage(gatewayId, new MarketData(marketData, endpoint));
                    });
                }
                else endpoints.add(update.getSession());
                // TODO Sync or copy objects?
            }
            catch (InterruptedException e) {
                LOG.error("MarketDataProvider interrupted!", e);
                stop();
            }
        }
        LOG.info("MarketDataProvider stopped working...");
    }
}

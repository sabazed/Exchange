package exchange.services;

import exchange.bus.MessageBus;
import exchange.common.Instrument;
import exchange.messages.InstrumentDataResponse;
import exchange.messages.Message;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Persistence;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ReferenceDataProvider extends MessageProcessor {

    private final static Logger LOG = LogManager.getLogger("exchangeLogger");

    // Manager for creating queries
    private final EntityManager manager;
    // Use a LinkedBlockingQueue to receive new orders
    private final BlockingQueue<Message> requests;
    // Instruments to load
    private final List<Instrument> instruments;
    // Response bus to send responses for frontend
    private final MessageBus exchangeBus;
    // Gateway ID and Engine ID
    private final String gatewayId;
    // Service ID for this instance
    private final String selfId;


    public ReferenceDataProvider(MessageBus messageBus, String gatewayId, String selfId) {
        manager = Persistence.createEntityManagerFactory("instrumentUnit").createEntityManager();
        requests = new LinkedBlockingQueue<>();
        instruments = manager.createQuery("FROM Instrument", Instrument.class).getResultList();
        exchangeBus = messageBus;
        this.gatewayId = gatewayId;
        this.selfId = selfId;
    }

    public String getSelfId() {
        return selfId;
    }

    @Override
    public void processMessage(Message message) {
        try {
            requests.put(message);
        } catch (InterruptedException e) {
            LOG.error("Thread interrupted, aborting...", e);
            stop();
        }
    }

    @Override
    protected void processMessages() {
        LOG.info("ReferenceDataProvider up and running!");
        while (running) {
            try {
                Message message = requests.take();
                LOG.info("Processing new {}", message);
                exchangeBus.sendMessage(gatewayId, new InstrumentDataResponse(message, instruments));
            }
            catch (InterruptedException e) {
                LOG.error("OrderEntryGateway interrupted!", e);
                stop();
            }
        }
        LOG.info("ReferenceDataProvider stopped working...");
    }

}

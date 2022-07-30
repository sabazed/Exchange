package exchange.services;

import exchange.bus.MessageBus;
import exchange.common.Instrument;
import exchange.messages.InstrumentData;
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
    // Response bus to send responses for frontend
    private final MessageBus exchangeBus;
    // Gateway ID and Engine ID
    private final String gatewayId;
    // Service ID for this instance
    private final String selfId;

    // Instruments to load
    private List<Instrument> instruments;

    public ReferenceDataProvider(MessageBus messageBus, String gatewayId, String selfId) {
        manager = Persistence.createEntityManagerFactory("instrumentUnit").createEntityManager();
        requests = new LinkedBlockingQueue<>();
        exchangeBus = messageBus;
        this.gatewayId = gatewayId;
        this.selfId = selfId;

        getInstruments();
    }

    public String getSelfId() {
        return selfId;
    }

    private void getInstruments() {
        instruments = manager.createQuery("FROM Instrument", Instrument.class).getResultList();
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
                exchangeBus.sendMessage(gatewayId, new InstrumentData(message, instruments));
            }
            catch (InterruptedException e) {
                LOG.error("OrderEntryGateway interrupted!", e);
                stop();
            }
        }
        LOG.info("ReferenceDataProvider stopped working...");
    }

}

package exchange.services;

import exchange.bus.MessageBus;
import exchange.common.Instrument;
import exchange.messages.Fail;
import exchange.messages.Message;
import exchange.messages.Request;
import exchange.messages.Response;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Persistence;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static exchange.enums.Status.RequestFail;

public class ReferenceDataProvider extends MessageProcessor {

    private final static Logger LOG = LogManager.getLogger("exchangeLogger");

    // Manager for creating queries
    private final EntityManager manager;
    // Use a LinkedBlockingQueue to receive new orders
    private final BlockingQueue<Message> requests;
    // Response bus to send responses for frontend
    private final MessageBus exchangeBus;
    // Gateway ID for bus
    private final String gatewayId;
    // Service ID for this instance
    private final String selfId;

    public ReferenceDataProvider(MessageBus messageBus, String gatewayId, String selfId) {
        manager = Persistence.createEntityManagerFactory("instrumentUnit").createEntityManager();
        requests = new LinkedBlockingQueue<>();
        exchangeBus = messageBus;
        this.gatewayId = gatewayId;
        this.selfId = selfId;
    }

    public String getSelfId() {
        return selfId;
    }

    private List<Instrument> getInstruments() {
        return manager.createQuery("FROM Instrument", Instrument.class).getResultList();
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
                // Check that message session or client id isn't null, otherwise return it as a Fail object
                if (message.getSession() == null || message.getClientId() == null || !(message instanceof Request))
                    message = new Fail(RequestFail);
                else message = new Response(message, getInstruments());
                exchangeBus.sendMessage(gatewayId, message);
            }
            catch (InterruptedException e) {
                LOG.error("OrderEntryGateway interrupted!", e);
                stop();
            }
        }
        LOG.info("ReferenceDataProvider stopped working...");
    }

}

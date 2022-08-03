package exchange.services;

import exchange.bus.MessageBus;
import exchange.common.Instrument;
import exchange.messages.InstrumentDataRequest;
import exchange.messages.InstrumentDataResponse;
import exchange.messages.Message;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Persistence;
import jakarta.persistence.criteria.CriteriaQuery;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class ReferenceDataProvider extends MessageProcessor {

    private static final Logger LOG = LogManager.getLogger(ReferenceDataProvider.class);

    // Manager for creating queries
    private final EntityManager manager;
    // Instruments to load
    private final List<Instrument> instruments;
    // Gateway ID and Engine ID
    private String gatewayId;


    public ReferenceDataProvider(MessageBus messageBus) {
        super(messageBus);
        manager = Persistence.createEntityManagerFactory("exchangeUnit").createEntityManager();
        instruments = fetchInstruments();
    }

    @Override
    public String getSelfId() {
        return selfId;
    }

    public void setGatewayId(String gatewayId) {
        this.gatewayId = gatewayId;
    }

    private List<Instrument> fetchInstruments() {
        CriteriaQuery<Instrument> criteria = manager.getCriteriaBuilder().createQuery(Instrument.class);
        criteria.select(criteria.from(Instrument.class));
        return manager.createQuery(criteria).getResultList();
    }

    @Override
    protected void processMessage(Message message) {
        if (message instanceof InstrumentDataRequest) {
            exchangeBus.sendMessage(gatewayId, new InstrumentDataResponse(message, instruments));
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

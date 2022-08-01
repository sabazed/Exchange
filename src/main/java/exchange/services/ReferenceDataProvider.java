package exchange.services;

import exchange.bus.MessageBus;
import exchange.common.Instrument;
import exchange.messages.InstrumentDataRequest;
import exchange.messages.InstrumentDataResponse;
import exchange.messages.Message;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Persistence;

import java.util.List;

public class ReferenceDataProvider extends MessageProcessor {

    // Manager for creating queries
    private final EntityManager manager;
    // Instruments to load
    private final List<Instrument> instruments;
    // Gateway ID and Engine ID
    private final String gatewayId;


    public ReferenceDataProvider(MessageBus messageBus, String gatewayId, String selfId) {
        super(messageBus, selfId, ReferenceDataProvider.class);
        manager = Persistence.createEntityManagerFactory("instrumentUnit").createEntityManager();
        instruments = manager.createQuery("FROM Instrument", Instrument.class).getResultList();
        this.gatewayId = gatewayId;
    }

    public String getSelfId() {
        return selfId;
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

}

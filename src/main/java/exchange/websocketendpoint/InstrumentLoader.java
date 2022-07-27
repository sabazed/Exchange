package exchange.websocketendpoint;

import exchange.common.Instrument;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Persistence;

import java.util.List;

public class InstrumentLoader {

    private final EntityManager manager;

    public InstrumentLoader() {
        manager = Persistence.createEntityManagerFactory("instrumentUnit").createEntityManager();
    }

    public List<Instrument> getInstruments() {
        return manager.createQuery("FROM Instrument", Instrument.class).getResultList();
    }

}

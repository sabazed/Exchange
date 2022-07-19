package server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class ExchangeBus implements MessageBus {

    private final static Logger LOG = LogManager.getLogger(ExchangeBus.class);

    private final Map<Service, MessageBusService> services;

    public ExchangeBus() {
        services = new HashMap<>();
    }

    // TODO sync

    @Override
    public void registerService(Service serviceType, MessageBusService service) {
        services.put(serviceType, service);
        LOG.info("{} registered a new {} with {}", this, serviceType, service);
    }

    @Override
    public void sendMessage(Service serviceType, Message message) {
        LOG.info("Sent {} a new message {}", serviceType, message);
        services.get(serviceType).processMessage(message);
    }

    @Override
    public MessageBusService getService(Service serviceType) {
        return services.get(serviceType);
    }
}

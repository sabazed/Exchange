package exchange.bus;

import exchange.messages.Message;
import exchange.services.MessageBusService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ExchangeBus implements MessageBus {

    private final static Logger LOG = LogManager.getLogger(ExchangeBus.class);

    private final ConcurrentMap<String, MessageBusService> services;

    public ExchangeBus() {
        services = new ConcurrentHashMap<>();
    }

    @Override
    public void registerService(String serviceId, MessageBusService service) {
        services.put(serviceId, service);
        LOG.info("Registered a new service with id: {}", serviceId);
    }

    @Override
    public void unregisterService(String serviceId) {
        services.remove(serviceId);
        LOG.info("Unregistered service with id: {}", serviceId);
    }

    @Override
    public void sendMessage(String serviceId, Message message) {
        MessageBusService service = services.get(serviceId);
        if (service != null) {
            service.processMessage(message);
            LOG.info("Sent service {} a new message {}", serviceId, message);
        }
        else {
            LOG.warn("Couldn't find registered service with id {} from {}", serviceId, message);
        }
    }

}

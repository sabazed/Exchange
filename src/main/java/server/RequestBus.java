package server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class RequestBus implements MessageBus {

    private final static Logger LOG = LogManager.getLogger(RequestBus.class);

    private final Map<String, MessageBusService> gateways;
    private MessageBusService engine;

    public RequestBus() {
        gateways = new HashMap<>();
    }

    @Override
    public void registerService(Service serviceType, MessageBusService service) {
        if (serviceType == Service.Gateway) {
            gateways.put(((OrderEntryGateway) service).getSession().getId(), service);
        }
        else {
            engine = service;
        }
        LOG.info("{} registered a new {} with {}", this, serviceType, service);
    }

    @Override
    public void unregisterService(Service serviceType, MessageBusService service) {
        if (serviceType == Service.Gateway) {
            gateways.remove(((OrderEntryGateway) service).getSession().getId());
        }
        else {
            engine = null;
        }
        LOG.info("{} unregistered {} of {}", this, serviceType, service);
    }

    @Override
    public void sendMessage(Service serviceType, Message message) {
        if (serviceType == Service.Gateway) {
            gateways.get(message.getSession()).processMessage(message);
        }
        else engine.processMessage(message);
        LOG.info("Sent {} a new message {}", serviceType, message);
    }



}

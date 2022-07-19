package server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class OrderEntryGateway implements MessageBusService {

    private static final Logger LOG = LogManager.getLogger(OrderEntryGateway.class);

    private final Map<String, ExchangeEndpoint> endpoints;
    private final BlockingQueue<Message> messages;
    private final MessageBus exchangeBus;

    private final Thread messageProcessor;
    private boolean running;

    public OrderEntryGateway(MessageBus ExchangeBus) {

        endpoints = new ConcurrentHashMap<>();
        messages = new LinkedBlockingQueue<>();

        this.exchangeBus = ExchangeBus;
        ExchangeBus.registerService(Service.Gateway, this);

        messageProcessor = new Thread(this::processRequests);
        this.running = false;
    }

    private void send(String session, Message request) {
        LOG.info("Sending to session {} with {}", session, request);
        endpoints.get(session).sendMessage(request);
    }

    public void addEndpoint(String sessionId, ExchangeEndpoint endpoint) {
        this.endpoints.put(sessionId, endpoint);
    }

    public void removeEndpoint(String sessionId) {
        this.endpoints.remove(sessionId);
    }

    @Override
    public void processMessage(Message message) {
        try {
            messages.put(message);
        } catch (InterruptedException e) {
            e.printStackTrace();
            LOG.fatal("Thread interrupted, aborting...");
            stop();
            send(message.getSession(), new Fail(Status.FatalFail, message));
        }
    }

    public void start() {
        running = true;
        messageProcessor.start();
    }

    public void stop() {
        running = false;
        for (ExchangeEndpoint endpoint : endpoints.values()) {
            endpoint.closeEndpoint();
        }
    }

    // Thread method for handling requests
    private void processRequests() {
        LOG.info("OrderEntryGateway up and running!");
        while (running) {
            try {
                Message message = messages.take();
                LOG.info("Processing new {}", message);
                if (message instanceof Order || message instanceof Cancel) {
                    exchangeBus.sendMessage(Service.Engine, message);
                }
                else send(message.getSession(), message);
            }
            catch (InterruptedException e) {
                LOG.fatal("OrderEntryGateway interrupted!");
                e.printStackTrace();
                stop();
            }
        }
        LOG.info("OrderEntryGateway stopped working...");
    }

}

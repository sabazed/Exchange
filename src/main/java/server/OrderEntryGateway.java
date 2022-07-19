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
    private final MessageBus requestBus;

    private final Thread messageProcessor;
    private boolean running;

    public OrderEntryGateway(MessageBus requestBus) {

        endpoints = new ConcurrentHashMap<>();
        messages = new LinkedBlockingQueue<>();

        this.requestBus = requestBus;
        requestBus.registerService(Service.Gateway, this);

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
    public void processMessage(Message request) {
        try {
            messages.put(request);
        } catch (InterruptedException e) {
            e.printStackTrace();
            // TODO
        }
    }

    public void start() {
        running = true;
        messageProcessor.start();
    }

    public void stop() {
        running = false;
    }

    // Thread method for handling requests
    private void processRequests() {
        LOG.info("OrderEntryGateway up and running!");
        while (running) {
            try {
                Message message = messages.take();
                LOG.info("Processing new {}", message);
                if (message.isSent()) {
                    message.setSent(false);
                    requestBus.sendMessage(Service.Engine, message);
                }
                else send(message.getSession(), message);
            }
            catch (InterruptedException e) {
                LOG.fatal("OrderEntryGateway interrupted!");
                e.printStackTrace();
                // TODO
            }
        }
        LOG.info("OrderEntryGateway stopped working...");
    }

}

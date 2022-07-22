package server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class OrderEntryGateway implements MessageBusService {

    private static final Logger LOG = LogManager.getLogger(OrderEntryGateway.class);

    private final BlockingQueue<Message> messages;
    private final MessageBus exchangeBus;

    private final Thread messageProcessor;
    private volatile boolean running;

    public OrderEntryGateway(MessageBus messageBus) {
        messages = new LinkedBlockingQueue<>();
        exchangeBus = messageBus;
        messageProcessor = new Thread(this::processMessages);
        running = false;
    }

    @Override
    public void processMessage(Message message) {
        try {
            messages.put(message);
        } catch (InterruptedException e) {
            LOG.fatal("Thread interrupted, aborting...");
            LOG.fatal(e);
            stop();
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
    private void processMessages() {
        LOG.info("OrderEntryGateway up and running!");
        while (running) {
            try {
                Message message = messages.take();
                LOG.info("Processing new {}", message);
                // Check that message session or client id isn't null, otherwise return it as a Fail object
                if (message.getSession() == null || message.getClientId() == null)
                    message = new Fail(Status.OrderFail);
                // Determine where to send the message, back to the endpoint or to the engine
                if (message instanceof Order || message instanceof Cancel) {
                    exchangeBus.sendMessage(MatchingEngine.class.getName(), message);
                }
                else exchangeBus.sendMessage(message.getSession(), message);
            }
            catch (InterruptedException e) {
                LOG.fatal("OrderEntryGateway interrupted!");
                LOG.fatal(e);
                stop();
            }
        }
        LOG.info("OrderEntryGateway stopped working...");
    }

}

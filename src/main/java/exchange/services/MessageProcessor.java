package exchange.services;

import exchange.bus.MessageBus;
import exchange.messages.InstrumentDataResponse;
import exchange.messages.Message;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public abstract class MessageProcessor implements MessageBusService {

    protected final Logger LOG;

    // Use a LinkedBlockingQueue to receive new orders
    protected final BlockingQueue<Message> messages;
    // Response bus to send responses for frontend
    protected final MessageBus exchangeBus;
    // Service ID for this instance
    protected final String selfId;

    // Thread which will process messages
    private final Thread messageProcessor;
    // Marker if the engine is running or not
    protected volatile boolean running;


    public MessageProcessor(MessageBus messageBus, String selfId, Class<?> childClass) {
        messages = new LinkedBlockingQueue<>();
        exchangeBus = messageBus;
        LOG = LogManager.getLogger(childClass);
        this.selfId = selfId;
        this.messageProcessor = new Thread(this::processMessages);
        this.running = false;
    }

    public void start() {
        running = true;
        messageProcessor.start();
    }

    public void stop() {
        running = false;
    }

    public String getSelfId() {
        return selfId;
    }

    public void issueMessage(Message message) {
        try {
            messages.put(message);
        }
        catch (InterruptedException e) {
            LOG.error("Thread interrupted, aborting...", e);
            stop();
        }
    }

    private void processMessages() {
        LOG.info(getClass().getSimpleName() + " up and running!");
        while (running) {
            try {
                Message message = messages.take();
                LOG.info("Processing new {}", message);
                // Overridden method for processing the message
                processMessage(message);
            }
            catch (InterruptedException e) {
                LOG.error(getClass().getSimpleName() + " interrupted!", e);
                stop();
            }
        }
        LOG.info(getClass().getSimpleName() + " stopped working...");
    }

    protected abstract void processMessage(Message message);

}

package exchange.services;

import exchange.bus.MessageBus;
import exchange.messages.Message;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public abstract class MessageProcessor implements MessageBusService {

    // Use a LinkedBlockingQueue to receive new orders
    protected final BlockingQueue<Message> messages;
    // Response bus to send responses for frontend
    protected final MessageBus exchangeBus;
    // Service ID for this instance
    protected String selfId;

    // Thread which will process messages
    private final Thread messageProcessor;
    // Marker if the engine is running or not
    protected volatile boolean running;


    public MessageProcessor(MessageBus messageBus) {
        messages = new LinkedBlockingQueue<>();
        exchangeBus = messageBus;
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

    public void setSelfId(String selfId) {
        this.selfId = selfId;
    }

    public void issueMessage(Message message) {
        try {
            messages.put(message);
        }
        catch (InterruptedException e) {
            getLogger().error("Thread interrupted, aborting...", e);
            stop();
        }
    }

    private void processMessages() {
        getLogger().info("{} up and running!", getClass().getSimpleName());
        while (running) {
            try {
                Message message = messages.take();
                getLogger().info("Processing new {}", message);
                // Overridden method for processing the message
                processMessage(message);
            }
            catch (InterruptedException e) {
                getLogger().error("{} interrupted!", getClass().getSimpleName(), e);
                stop();
            }
        }
        getLogger().info("{} stopped working...", getClass().getSimpleName());
    }

    protected abstract void processMessage(Message message);

    protected abstract Logger getLogger();

}

package exchange.services;

import exchange.messages.Message;

public abstract class MessageProcessor implements MessageBusService {

    // Thread which will process messages
    private final Thread messageProcessor;
    // Marker if the engine is running or not
    protected volatile boolean running;

    public MessageProcessor() {
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

    public abstract String getSelfId();

    public abstract void processMessage(Message message);

    protected abstract void processMessages();

}

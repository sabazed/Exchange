package server;

public interface MessageBusService {

    void processMessage(Message message);

    void start();

    void stop();

}

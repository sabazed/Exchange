package server;

public interface MessageBus {

    void registerService(String serviceId, MessageBusService service);

    void unregisterService(String serviceId);

    void sendMessage(String serviceId, Message message);

}

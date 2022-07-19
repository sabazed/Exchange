package server;


public interface MessageBus {

    void registerService(Service serviceType, MessageBusService service);

    void sendMessage(Service serviceType, Message message);

    MessageBusService getService(Service serviceType);

}

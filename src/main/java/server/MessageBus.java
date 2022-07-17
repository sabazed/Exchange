package server;


public interface MessageBus {

    public void registerService(Service serviceType, MessageBusService service);

    public void unregisterService(Service serviceType, MessageBusService service);

    public void sendMessage(Service serviceType, Message message);

}

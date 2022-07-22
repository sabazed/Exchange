package exchange.bus;

import exchange.messages.Message;
import exchange.services.MessageBusService;

public interface MessageBus {

    void registerService(String serviceId, MessageBusService service);

    void unregisterService(String serviceId);

    void sendMessage(String serviceId, Message message);

}

package exchange.services;

import exchange.messages.Message;

public interface MessageBusService {

    void issueMessage(Message message);

}

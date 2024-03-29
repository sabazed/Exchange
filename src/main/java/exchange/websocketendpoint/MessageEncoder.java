package exchange.websocketendpoint;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import exchange.messages.Message;
import jakarta.websocket.Encoder;

public class MessageEncoder implements Encoder.Text<Message> {

    @Override
    public String encode(Message message) {
        ObjectMapper mapper = new ObjectMapper();
        String json;
        try {
            json = mapper.writeValueAsString(message);
        } catch (JsonProcessingException e) {
            throw new EncoderException(message, e);
        }
        return json;
    }

}




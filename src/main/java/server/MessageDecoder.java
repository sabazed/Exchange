package server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import jakarta.websocket.Decoder;
import jakarta.websocket.EndpointConfig;

import java.io.IOException;
import java.math.BigDecimal;

public class MessageDecoder implements Decoder.Text<Message> {

    @Override
    public Message decode(String json) {
        ObjectMapper mapper = new ObjectMapper();
        Message message = null;
        try {
            message = mapper.readValue(json, Message.class);
        } catch (InvalidFormatException e) { // TODO
            if (e.getTargetType() == BigDecimal.class) {
                message = new Fail(Status.Price);

            }
            else if (e.getTargetType() == int.class) { // TODO
                message = new Fail(Status.Quantity);
            }
        }
        catch (IOException e) {
            message = new Fail(Status.OrderFail); // TODO
            e.printStackTrace();
        }
        return message;
    }

    @Override
    public boolean willDecode(String s) {
        return s != null;
    }

    public MessageDecoder() {
    }

    @Override
    public void init(EndpointConfig config) {
    }

    @Override
    public void destroy() {
    }

}


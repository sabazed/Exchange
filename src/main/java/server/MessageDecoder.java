package server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import jakarta.websocket.Decoder;

import java.io.IOException;

public class MessageDecoder implements Decoder.Text<Message> {

    @Override
    public Message decode(String json) {
        ObjectMapper mapper = new ObjectMapper();
        Message message;
        try {
            message = mapper.readValue(json, Message.class);
        } catch (InvalidFormatException e) {
            // Check which is invalid - price or qty
            String errorMessage = e.getPathReference();
            int start = errorMessage.indexOf("\"");
            int end = errorMessage.lastIndexOf("\"");
            String field = errorMessage.substring(start + 1, end);
            if (field.equals("price")) {
                message = new Fail(Status.Price);
            }
            else if (field.equals("qty")) {
                message = new Fail(Status.Quantity);
            }
            else {
                e.printStackTrace();
                throw new DecoderException(json, e);
            }
        }
        catch (IOException e) {
            e.printStackTrace();
            throw new DecoderException(json, e);
        }
        return message;
    }

    @Override
    public boolean willDecode(String s) {
        return s != null;
    }

}


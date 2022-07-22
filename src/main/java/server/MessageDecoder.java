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
        }
        catch (IOException e) {
            throw new DecoderException(json, e);
        }
        return message;
    }

    @Override
    public boolean willDecode(String s) {
        return s != null;
    }

}


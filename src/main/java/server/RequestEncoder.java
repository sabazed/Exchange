package server;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.websocket.Encoder;
import jakarta.websocket.EndpointConfig;

public class RequestEncoder implements Encoder.Text<Request> {

    @Override
    public String encode(Request req) {
        ObjectMapper mapper = new ObjectMapper();
        String json = "";
        try {
            json = mapper.writeValueAsString(req);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return json;
    }

    public RequestEncoder() { }

    @Override
    public void init(EndpointConfig config) {
    }

    @Override
    public void destroy() {
    }

}




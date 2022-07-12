package server;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.websocket.Decoder;
import jakarta.websocket.EndpointConfig;

import java.io.IOException;

public class RequestDecoder implements Decoder.Text<Request> {

    @Override
    public Request decode(String s) {
        ObjectMapper mapper = new ObjectMapper();
        Request req = null;
        try {
            req = mapper.readValue(s, Request.class);
        }
        catch (Exception e) {
            e.printStackTrace();
            req = new Request(Status.Fail);
        }
        return req;
    }

    @Override
    public boolean willDecode(String s) {
        return s != null;
    }

    public RequestDecoder() { }

    @Override
    public void init(EndpointConfig config) {
    }

    @Override
    public void destroy() {
    }

}


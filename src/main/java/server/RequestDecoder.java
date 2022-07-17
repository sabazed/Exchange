package server;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.websocket.Decoder;
import jakarta.websocket.EndpointConfig;

import java.io.IOException;

public class RequestDecoder implements Decoder.Text<Request> {

    @Override
    public Request decode(String json) {
        ObjectMapper mapper = new ObjectMapper();
        Request req = null;
        try {
            req = mapper.readValue(json, Request.class);
        } catch (NullPointerException | NumberFormatException e) {
            e.printStackTrace();
            req = new Request(Status.ListFail, new Order());
        } catch (Exception e) {
            e.printStackTrace();
            req = new Request(Status.Fail, new Order());
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


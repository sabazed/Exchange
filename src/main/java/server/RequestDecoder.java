package server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import jakarta.websocket.Decoder;
import jakarta.websocket.EndpointConfig;

import java.io.IOException;
import java.math.BigDecimal;

public class RequestDecoder implements Decoder.Text<Request> {

    @Override
    public Request decode(String json) {
        ObjectMapper mapper = new ObjectMapper();
        Request req;
        try {
            req = mapper.readValue(json, Request.class);
        } catch (InvalidFormatException e) {
            req = new Request(null, false, new Order());
            if (e.getTargetType() == BigDecimal.class) {
                req.addErrorCode(Status.InvalidPrice);
            }
            if (e.getTargetType() == int.class) {
                req.addErrorCode(Status.InvalidQty);
            }
        }
        catch (IOException e) {
            req = new Request(Status.Fail, false, new Order());
            e.printStackTrace();
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


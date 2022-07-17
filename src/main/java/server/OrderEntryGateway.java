package server;

import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

@ServerEndpoint(value = "/order", encoders = RequestEncoder.class, decoders = RequestDecoder.class)
public class OrderEntryGateway implements MessageBusService {

    private static final Logger LOG = LogManager.getLogger(OrderEntryGateway.class);

    private static final Map<String, OrderEntryGateway> gateways = new ConcurrentHashMap<>();
    private static final BlockingQueue<Message> requests = new LinkedBlockingQueue<>();
    private static MessageBus requestBus; // Can be non-static

    private Session session;

    private void send(Message request) {
        LOG.info("Sending to session {} with {}", session.getId(), request);
        try {
            session.getBasicRemote().sendObject(request);
        }
        catch (EncodeException e) {
            LOG.warn("Couldn't encode for session {} with {}", session.getId(), request);
            e.printStackTrace();
        }
        catch (IOException e) {
            LOG.warn("Couldn't send request to session {} with {}", session.getId(), request);
        }

    }

    @Override
    public void processMessage(Message request) {
        try {
            requests.put(request);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @OnMessage
    public void onMessage(Session session, Request request) {
        LOG.info("New request from session {}, data - {}", session.getId(), request);
        if (!request.isValid()) {
            request.getOrder().setSession(session.getId());
            requestBus.sendMessage(Service.Gateway, request);
        }
        else {
            request.getOrder().setSession(session.getId());
            requestBus.sendMessage(Service.Engine, request);
        }
    }

    @OnOpen
    public void onOpen(Session session) {
        LOG.info("New client connected with session {}", session.getId());
        gateways.put(session.getId(), this);
        this.session = session;
        requestBus.registerService(Service.Gateway, this);
    }

    @OnClose
    public void onClose(Session session) {
        LOG.info("Client connected with session {}", session.getId());
        gateways.remove(session.getId());
        requestBus.unregisterService(Service.Gateway, this);
    }

    @OnError
    public void onError(Session session, Throwable t) {
        LOG.error("Caused exception at session {}", session.getId());
        // TODO
        t.printStackTrace();
    }

    protected Session getSession() {
        return session;
    }

    protected static void SetRequestBus(MessageBus requestBus) {
        OrderEntryGateway.requestBus = requestBus;
    }

    // Thread method for handling requests
    private static void processRequests() {
        while (true) {
            try {
                Message request = requests.take();
                LOG.info("Processing new {}", request);
                gateways.get(request.getSession()).send(request);
            }
            catch (InterruptedException e) {
                LOG.fatal("OrderEntryGateway interrupted!");
                e.printStackTrace();
            }
        }
    }

    protected static Thread getThread() {
        return new Thread() {
            @Override
            public void run() {
                LOG.info("OrderEntryGateway up and running!");
                processRequests();
                LOG.info("OrderEntryGateway stopped working...");

            }
        };
    }

}

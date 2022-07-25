package exchange.endpoint;

import exchange.services.MatchingEngine;
import exchange.services.OrderEntryGateway;
import exchange.bus.ExchangeBus;
import exchange.bus.MessageBus;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.websocket.DeploymentException;
import jakarta.websocket.server.ServerContainer;
import jakarta.websocket.server.ServerEndpointConfig;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;

import java.util.List;

public class ExchangeServletContextListener implements ServletContextListener {

    private static final Logger LOG = LogManager.getLogger(ExchangeServletContextListener.class);

    private MatchingEngine engine;
    private OrderEntryGateway gateway;
    private MessageBus exchangeBus;

    @Override
    public void contextInitialized(ServletContextEvent sce) {

        sce.getServletContext().addListener(ExchangeRequestListener.class);
        ServerContainer sc = (ServerContainer) sce.getServletContext().getAttribute("jakarta.websocket.server.ServerContainer");

        try {
            sc.addEndpoint(
                ServerEndpointConfig.Builder
                    .create(ExchangeEndpoint.class, "/order")
                    .configurator(new ExchangeServerEndpointConfig())
                    .encoders(List.of(MessageEncoder.class))
                    .decoders(List.of(MessageDecoder.class))
                    .build()
            );
        }
        catch(DeploymentException e) {
            LOG.error("Couldn't add endpoint to server container!");
            LOG.error(e);
        }

        // Configure the root logger
        Configurator.setLevel(LogManager.getRootLogger(), Level.ALL);

        // Initialize the Response Bus
        LOG.info("Initializing a new Exchange Bus");
        exchangeBus = new ExchangeBus();
        // Create both service instances
        LOG.info("Initializing a new Matching Engine");
        engine = new MatchingEngine(exchangeBus, "OrderEntryGateway_0");
        LOG.info("Initializing a new Order Entry Gateway");
        gateway = new OrderEntryGateway(exchangeBus, "MatchingEngine_0");
        // Register both instances
        exchangeBus.registerService("MatchingEngine_0", engine);
        exchangeBus.registerService("OrderEntryGateway_0", gateway);
        // Start the services
        engine.start();
        gateway.start();

        // Add current listener object to Servlet Context attributes
        sce.getServletContext().setAttribute(MessageBus.class.getName(), exchangeBus);

    }

    @Override
    public void contextDestroyed(ServletContextEvent sce){
        engine.stop();
        gateway.stop();
    }

}

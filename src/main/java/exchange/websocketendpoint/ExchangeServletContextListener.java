package exchange.websocketendpoint;

import exchange.bus.ExchangeBus;
import exchange.bus.MessageBus;
import exchange.loader.ConfigLoader;
import exchange.services.MatchingEngine;
import exchange.services.MessageProcessor;
import exchange.services.OrderEntryGateway;
import exchange.services.ReferenceDataProvider;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.websocket.DeploymentException;
import jakarta.websocket.server.ServerContainer;
import jakarta.websocket.server.ServerEndpointConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.List;

public class ExchangeServletContextListener implements ServletContextListener {

    private static final Logger LOG = LogManager.getLogger("exchangeLogger");

    private ConfigLoader serviceLoader;

    private MessageProcessor engine;
    private MessageProcessor gateway;
    private MessageProcessor provider;
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
            LOG.error("Couldn't add endpoint to server container!", e);
        }

        // Create service loader for reading from config json
        LOG.info("Creating a service loader instance");
        serviceLoader = new ConfigLoader();
        boolean exception = false;
        try {
            // Read the config json file
            LOG.info("Reading config from the file");
            serviceLoader.loadConfig();
            // Initialize the Response Bus
            LOG.info("Initializing a new Exchange Bus");
            exchangeBus = serviceLoader.getBusInstance();
            // Create all service instances
            LOG.info("Initializing a new Matching Engine");
            engine = serviceLoader.getEngineInstance(exchangeBus);
            LOG.info("Initializing a new Order Entry Gateway");
            gateway = serviceLoader.getGatewayInstance(exchangeBus);
            LOG.info("Initializing Reference Data Provider");
            provider = serviceLoader.getProviderInstance(exchangeBus);
        }
        catch (IOException e) {
            LOG.error("Couldn't read the config file, proceeding with default implementations", e);
            exception = true;
        }
        catch (ReflectiveOperationException e) {
            LOG.error("Couldn't create class instances, proceeding with default implementations", e);
            exception = true;
        }

        // If service loader failed then continue with default classes
        if (exception) {
            // Initialize the Response Bus
            LOG.info("Initializing a new Exchange Bus");
            exchangeBus = new ExchangeBus();
            // Create all service instances
            LOG.info("Initializing a new Matching Engine");
            engine = new MatchingEngine(exchangeBus, "Gateway", "Engine");
            LOG.info("Initializing a new Order Entry Gateway");
            gateway = new OrderEntryGateway(exchangeBus, "Engine", "Provider", "ServerEndpoint_", "Gateway");
            LOG.info("Initializing Reference Data Provider");
            provider = new ReferenceDataProvider(exchangeBus, "Gateway", "Provider");
        }

        // Register all instances
        exchangeBus.registerService(engine.getSelfId(), engine);
        exchangeBus.registerService(gateway.getSelfId(), gateway);
        exchangeBus.registerService(provider.getSelfId(), provider);

        // Start the services
        engine.start();
        gateway.start();
        provider.start();

        // Add current listener to Servlet Context attributes
        sce.getServletContext().setAttribute(MessageBus.class.getName(), exchangeBus);
        // Add gateway and endpoint IDs
        sce.getServletContext().setAttribute("GatewayId", gateway.getSelfId());
        sce.getServletContext().setAttribute("EndpointId", serviceLoader.getEndpointId());



    }

    @Override
    public void contextDestroyed(ServletContextEvent sce){
        engine.stop();
        gateway.stop();
        provider.stop();
    }

}

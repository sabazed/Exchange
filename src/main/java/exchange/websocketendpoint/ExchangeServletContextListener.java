package exchange.websocketendpoint;

import exchange.bus.MessageBus;
import exchange.loader.ConfigLoader;
import exchange.loader.InvalidClassConfigException;
import exchange.services.MessageProcessor;
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

    private final Logger LOG = LogManager.getLogger(ExchangeServletContextListener.class);
    private ConfigLoader serviceLoader;

    private List<MessageProcessor> services;
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
        try {
            serviceLoader.loadConfig();
            exchangeBus = serviceLoader.getBusInstance();
            services = serviceLoader.getServices(exchangeBus);
        }
        catch (InvalidClassConfigException e) {
            LOG.error("Couldn't create class instances, aborting...", e);
            throw new ServiceInitializationException("Couldn't create class instances", e);
        }

        // Register all instances
        for (MessageProcessor service : services) exchangeBus.registerService(service.getSelfId(), service);

        // Start the services
        for (MessageProcessor service : services) service.start();

        // Add current listener to Servlet Context attributes
        sce.getServletContext().setAttribute(MessageBus.class.getName(), exchangeBus);
        // Add gateway and endpoint IDs
        sce.getServletContext().setAttribute("GatewayId", serviceLoader.getGatewayId());
        sce.getServletContext().setAttribute("EndpointId", serviceLoader.getEndpointId());

    }

    @Override
    public void contextDestroyed(ServletContextEvent sce){
        for (MessageProcessor service : services) service.stop();
    }

}

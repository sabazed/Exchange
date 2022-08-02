package exchange.websocketendpoint;

import exchange.bus.ExchangeBus;
import exchange.bus.MessageBus;
import exchange.loader.ConfigLoader;
import exchange.services.MarketDataProvider;
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
import java.util.ArrayList;
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
        boolean exception = false;
        try {
            serviceLoader.loadConfig();
            exchangeBus = serviceLoader.getBusInstance();
            services = serviceLoader.getServices(exchangeBus);
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
            exchangeBus = new ExchangeBus();
            services = new ArrayList<>();
            services.add(new MatchingEngine(exchangeBus, "Gateway", "MarketProvider", "Engine"));
            services.add(new OrderEntryGateway(exchangeBus, "Engine", "ReferenceProvider", "MarketProvider", "ServerEndpoint_", "Gateway"));
            services.add(new MarketDataProvider(exchangeBus, "Gateway", "MarketProvider"));
            services.add(new ReferenceDataProvider(exchangeBus, "Gateway", "ReferenceProvider"));
            sce.getServletContext().setAttribute("GatewayId", serviceLoader.getGatewayId());
            sce.getServletContext().setAttribute("EndpointId", serviceLoader.getEndpointId());
        }

        // Register all instances
        for (MessageProcessor service : services) exchangeBus.registerService(service.getSelfId(), service);

        // Start the services
        for (MessageProcessor service : services) service.start();

        // Add current listener to Servlet Context attributes
        sce.getServletContext().setAttribute(MessageBus.class.getName(), exchangeBus);
        // Add gateway and endpoint IDs
        sce.getServletContext().setAttribute("GatewayId", "Gateway");
        sce.getServletContext().setAttribute("EndpointId", "ServerEndpoint_");

    }

    @Override
    public void contextDestroyed(ServletContextEvent sce){
        for (MessageProcessor service : services) service.stop();
    }

}

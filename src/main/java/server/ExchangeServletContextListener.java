package server;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;

@WebListener
public class ExchangeServletContextListener implements ServletContextListener {

    private static final Logger LOG = LogManager.getLogger(ExchangeServletContextListener.class);

    static {
        Configurator.setLevel(LogManager.getRootLogger(), Level.ALL);
    }

    private static MessageBusService engine;
    private static MessageBusService gateway;
    private static MessageBus requestBus;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        if (engine == null) {
            // Initialize the Request Bus
            LOG.info("Initializing a new Request Bus");
            requestBus = new RequestBus();
            // Create both service instances
            LOG.info("Initializing a new Matching Engine");
            engine = new MatchingEngine(requestBus);
            LOG.info("Initializing a new Order Entry Gateway");
            gateway = new OrderEntryGateway(requestBus);
            engine.start();
            gateway.start();
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce){
        try {
            engine.stop();
            gateway.stop();
        } catch (Exception e) {
            e.printStackTrace();
            // TODO
        }
    }

    public static OrderEntryGateway getGateWay() {
        return (OrderEntryGateway) requestBus.getService(Service.Gateway);
    }

}
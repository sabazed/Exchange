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
    private static MessageBus exchangeBus;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        if (engine == null) {
            // Initialize the Response Bus
            LOG.info("Initializing a new Exchange Bus");
            exchangeBus = new ExchangeBus();
            // Create both service instances
            LOG.info("Initializing a new Matching Engine");
            engine = new MatchingEngine(exchangeBus);
            LOG.info("Initializing a new Order Entry Gateway");
            gateway = new OrderEntryGateway(exchangeBus);
            engine.start();
            gateway.start();
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce){
        engine.stop();
        gateway.stop();
    }

    public static OrderEntryGateway getGateWay() {
        return (OrderEntryGateway) exchangeBus.getService(Service.Gateway);
    }

}
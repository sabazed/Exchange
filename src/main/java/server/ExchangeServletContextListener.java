package server;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;

public class ExchangeServletContextListener implements ServletContextListener {

    private static final Logger LOG = LogManager.getLogger(ExchangeServletContextListener.class);

    static {
        Configurator.setLevel(LogManager.getRootLogger(), Level.ALL);
    }

    // Add both threads as attributes
    private static Thread engine = null;
    private static Thread gateway = null;

    private static MessageBus requestBus;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        if (engine == null || gateway == null) {
            // Initialize the Request Bus
            LOG.info("Initializing a new Request Bus");
            requestBus = new RequestBus();
            // Create both service instances
            LOG.info("Initializing a new Matching Engine");
            MatchingEngine matchingEngine = new MatchingEngine(requestBus);
            // Register both services
            LOG.info("Registering service of {}", engine);
            requestBus.registerService(Service.Engine, matchingEngine);
            OrderEntryGateway.SetRequestBus(requestBus);
            // Create threads and run them
            engine = new Thread(matchingEngine);
            gateway = OrderEntryGateway.getThread();
            engine.start();
            gateway.start();
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce){
        try {
            engine.interrupt();
            engine.interrupt();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static MessageBus getRequestBus() {
        return requestBus;
    }

}
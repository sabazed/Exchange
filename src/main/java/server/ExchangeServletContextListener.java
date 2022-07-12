package server;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;

public class ExchangeServletContextListener implements ServletContextListener {

    private Thread engine = null;

    public void contextInitialized(ServletContextEvent sce) {
        if (engine == null) {
            engine = new Thread(new MatchingEngine());
            engine.start();
        }
    }

    public void contextDestroyed(ServletContextEvent sce){
        try {
            engine.interrupt();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
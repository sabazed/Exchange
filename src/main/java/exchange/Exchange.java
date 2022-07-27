package exchange;

import exchange.websocketendpoint.ExchangeServletContextListener;
import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;

import java.io.File;

public class Exchange {

    public static void main(String[] args) throws LifecycleException {

        Tomcat tomcat = new Tomcat();
        tomcat.setPort(8080);
        tomcat.getConnector();

        String ctxPath = "/Exchange";
        String webappDir = new File("src/main/webapp").getAbsolutePath();

        Context ctx = tomcat.addWebapp(ctxPath, webappDir);
        ctx.addApplicationListener(ExchangeServletContextListener.class.getName());

        tomcat.start();
        tomcat.getServer().await();

    }

}

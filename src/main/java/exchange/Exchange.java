package exchange;

import exchange.endpoint.*;
import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;

public class Exchange {

    public static void main(String[] args) throws LifecycleException {

        Tomcat tomcat = new Tomcat();
        tomcat.setPort(8080);
        tomcat.getHost().setAppBase(".");
        tomcat.getConnector();

        String contextPath = "/Exchange";
        String warFilePath = System.getProperty("user.dir") + "\\out\\artifacts\\Exchange_main_war\\Exchange.main_war.war";

        Context ctx = tomcat.addWebapp(contextPath, warFilePath);
        ctx.addApplicationListener(ExchangeServletContextListener.class.getName());

        tomcat.start();
        tomcat.getServer().await();

    }

}

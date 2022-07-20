package server;

import jakarta.servlet.ServletRequestEvent;
import jakarta.servlet.ServletRequestListener;
import jakarta.servlet.annotation.WebListener;
import jakarta.servlet.http.HttpServletRequest;

@WebListener
public class ExchangeRequestListener implements ServletRequestListener {

    @Override
    public void requestDestroyed(ServletRequestEvent sre) {
        ServletRequestListener.super.requestDestroyed(sre);
    }

    @Override
    public void requestInitialized(ServletRequestEvent sre) {
        ((HttpServletRequest) sre.getServletRequest()).getSession();
    }

}

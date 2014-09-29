package com.johannesbrodwall.infrastructure.webserver;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.MovedContextHandler;
import org.eclipse.jetty.server.handler.ShutdownHandler;
import org.eclipse.jetty.webapp.Configuration;
import org.eclipse.jetty.webapp.WebAppContext;
import org.eclipse.jetty.webapp.WebInfConfiguration;
import org.eclipse.jetty.webapp.WebXmlConfiguration;

import java.net.URI;
import java.net.URL;

public class WebServer {

    private Server server;
    private HandlerList handlers = new HandlerList();

    public WebServer() {
    }

    public WebServer(int port) {
        setPort(port);
    }

    protected void setPort(int port) {
        server = new Server(port);
        server.setHandler(handlers);
    }

    protected void addHandler(Handler handler) {
        handlers.addHandler(handler);
    }

    public void start() throws Exception {
        server.start();
    }

    public URI getURI() {
        return server.getURI();
    }


    protected WebAppContext createWebAppContext(String contextPath) {
        WebAppContext webapp = new WebAppContext();
        webapp.setContextPath(contextPath);

        URL webAppUrl = getClass().getResource("/webapp");
        if (webAppUrl.getProtocol().equals("jar")) {
            webapp.setWar(webAppUrl.toExternalForm());
        } else {
            webapp.setWar("src/main/resources/webapp");
            // Avoid locking static content when running exploded
            webapp.setInitParameter("org.eclipse.jetty.servlet.Default.useFileMappedBuffer", "false");
        }
        webapp.setConfigurations(new Configuration[] {
                new WebInfConfiguration(), new WebXmlConfiguration(),
        });
        return webapp;
    }

    protected ShutdownHandler shutdownHandler() {
        return new ShutdownHandler("sdgsdgs", false, true);
    }

    protected Handler createRedirectContextHandler(String contextPath, String server) {
        MovedContextHandler contextHandler = new MovedContextHandler();
        contextHandler.setContextPath(contextPath);
        contextHandler.setNewContextURL(server);
        return contextHandler;
    }

}

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
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;

import lombok.SneakyThrows;

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

    public static void setupLogin(String logConfig) throws JoranException {
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
    
        extractConfiguration(logConfig);
    
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        loggerContext.reset();
        JoranConfigurator configurator = new JoranConfigurator();
        configurator.setContext(loggerContext);
        configurator.doConfigure(logConfig);
    }

    @SneakyThrows(IOException.class)
    public static void extractConfiguration(String filename) {
        File file = new File(filename);
        if (file.exists()) return;

        try (FileOutputStream output = new FileOutputStream(file)) {
            try (InputStream input = WebServer.class.getResourceAsStream("/" + filename)) {
                if (input == null) {
                    throw new IllegalArgumentException("Can't find /" + filename + " in classpath");
                }
                copy(input, output);
            }
        }
    }

    private static void copy(InputStream in, OutputStream out) throws IOException {
        byte[] buf = new byte[1024];
        int count = 0;
        while ((count = in.read(buf)) >= 0) {
            out.write(buf, 0, count);
        }
    }

}

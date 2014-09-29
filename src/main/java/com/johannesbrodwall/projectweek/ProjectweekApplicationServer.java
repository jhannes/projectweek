package com.johannesbrodwall.projectweek;

import org.flywaydb.core.Flyway;
import org.slf4j.bridge.SLF4JBridgeHandler;

import com.johannesbrodwall.infrastructure.webserver.WebServer;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ProjectweekApplicationServer extends WebServer {

    private ProjectweekAppConfig config = new ProjectweekAppConfig();

    public ProjectweekApplicationServer() {
        setPort(config.getPort(11080));
    }

    @Override
    public void start() throws Exception {
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();

        Flyway flyway = new Flyway();
        flyway.setDataSource(config.getDataSource());
        flyway.setLocations("db/migration", "db/baseline");
        flyway.clean();
        flyway.migrate();

        addHandler(shutdownHandler());
        addHandler(createWebAppContext("/projectweek"));
        addHandler(createRedirectContextHandler("/", "/projectweek"));
        super.start();
    }

    public static void main(String[] args) throws Exception {
        ProjectweekApplicationServer server = new ProjectweekApplicationServer();
        server.start();

        log.info("Started " + server.getURI());
    }
}

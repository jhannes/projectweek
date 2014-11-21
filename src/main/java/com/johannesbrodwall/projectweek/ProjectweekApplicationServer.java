package com.johannesbrodwall.projectweek;

import org.flywaydb.core.Flyway;
import org.slf4j.bridge.SLF4JBridgeHandler;


import com.johannesbrodwall.infrastructure.db.Database;
import com.johannesbrodwall.infrastructure.webserver.WebServer;
import com.johannesbrodwall.projectweek.issues.JiraIssuesLoader;
import com.johannesbrodwall.projectweek.projects.JiraProjectLoader;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ProjectweekApplicationServer extends WebServer {

    private ProjectweekAppConfig config = ProjectweekAppConfig.instance();

    public ProjectweekApplicationServer() {
        setPort(config.getHttpPort(11080));
    }

    public void start(String siteName) throws Exception {
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();

        Flyway flyway = new Flyway();
        flyway.setDataSource(config.getDataSource());
        flyway.setLocations("db/migration", "db/baseline");
        flyway.migrate();

        Database database = new Database(config.getDataSource());
        database.executeInTransaction(() -> {
            JiraProjectLoader projectLoader = new JiraProjectLoader(siteName, config);
            projectLoader.load();

            JiraIssuesLoader issuesLoader = new JiraIssuesLoader(siteName, config);
            for (String project : config.getProjects(siteName)) {
                issuesLoader.load("\"" + project + "\"");
            }
        });


        addHandler(shutdownHandler());
        addHandler(createWebAppContext("/projectweek"));
        addHandler(createRedirectContextHandler("/", "/projectweek"));
        super.start();
    }

    public static void main(String[] args) throws Exception {
        ProjectweekApplicationServer server = new ProjectweekApplicationServer();
        server.start(args[0]);

        log.info("Started " + server.getURI());
    }
}

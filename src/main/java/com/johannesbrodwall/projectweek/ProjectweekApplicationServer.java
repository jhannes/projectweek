package com.johannesbrodwall.projectweek;

import java.io.IOException;

import org.flywaydb.core.Flyway;

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
        Flyway flyway = new Flyway();
        flyway.setDataSource(config.getDataSource());
        flyway.setLocations("db/migration", "db/baseline");
        flyway.migrate();

        syncJira(siteName);

        addHandler(shutdownHandler());
        addHandler(createWebAppContext("/projectweek"));
        addHandler(createRedirectContextHandler("/", "/projectweek"));
        super.start();
    }

    private void syncJira(String siteName) throws IOException {
        Database database = new Database(config.getDataSource());
        database.executeInTransaction(() -> {
            JiraProjectLoader projectLoader = new JiraProjectLoader(siteName, config);
            projectLoader.load();
        });

        JiraIssuesLoader issuesLoader = new JiraIssuesLoader(siteName, config);
        for (String project : config.getProjects(siteName)) {
            database.executeInTransaction(() -> {
                issuesLoader.load(project);
            });
        }
    }

    public static void main(String[] args) throws Exception {
        WebServer.setupLogin("logging-projectweek.xml");

        ProjectweekApplicationServer server = new ProjectweekApplicationServer();
        server.start(args[0]);

        log.info("Started " + server.getURI());
    }
}

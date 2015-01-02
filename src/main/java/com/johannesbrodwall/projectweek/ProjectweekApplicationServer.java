package com.johannesbrodwall.projectweek;

import java.io.IOException;

import org.eclipse.jetty.server.handler.MovedContextHandler;
import org.eclipse.jetty.server.handler.ShutdownHandler;
import org.eclipse.jetty.webapp.WebAppContext;
import org.flywaydb.core.Flyway;

import com.johannesbrodwall.infrastructure.db.Database;
import com.johannesbrodwall.infrastructure.webserver.WebServer;
import com.johannesbrodwall.projectweek.issues.JiraIssuesLoader;
import com.johannesbrodwall.projectweek.projects.JiraProjectLoader;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ProjectweekApplicationServer extends WebServer {

    @NonNull
    private final ProjectweekAppConfig config;

    public ProjectweekApplicationServer(ProjectweekAppConfig config) {
        this.config = config;
        setPort(config.getHttpPort(11080));
    }

    public void start(String siteName) throws Exception {
        Flyway flyway = new Flyway();
        flyway.setDataSource(config.getDataSource());
        flyway.setLocations("db/migration");
        flyway.migrate();

        //syncJira(siteName);

        addHandler(new ShutdownHandler("sdgsdgs", false, true));
        WebAppContext applicationServer = createWebAppContext("/projectweek");
        addHandler(applicationServer);
        addHandler(new MovedContextHandler(null, "/", "/projectweek"));
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
        WebServer.setupLogging("logging-projectweek.xml");

        ProjectweekApplicationServer server = new ProjectweekApplicationServer(ProjectweekAppConfig.instance());
        server.start(args.length > 0 ? args[0] : "default");

        log.info("Started " + server.getURI());
    }
}

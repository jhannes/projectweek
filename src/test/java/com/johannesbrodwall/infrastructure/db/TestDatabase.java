package com.johannesbrodwall.infrastructure.db;

import org.flywaydb.core.Flyway;
import org.slf4j.bridge.SLF4JBridgeHandler;

import com.johannesbrodwall.infrastructure.AppConfiguration;
import com.johannesbrodwall.projectweek.ProjectweekAppConfig;
import com.johannesbrodwall.projectweek.ProjectweekDatabase;

public class TestDatabase extends ProjectweekDatabase {

    private static AppConfiguration config = new ProjectweekAppConfig("projectweek-test.properties");

    private static TestDatabase instance = new TestDatabase();

    public static TestDatabase instance() {
        return instance;
    }

    private TestDatabase() {
        super(config.getDataSource());

        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();

        Flyway flyway = new Flyway();
        flyway.setDataSource(config.getDataSource());
        if (config.getFlag("test.clean-db", false)) {
            flyway.clean();
        }
        flyway.migrate();
    }
}

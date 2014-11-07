package com.johannesbrodwall.infrastructure.db;

import org.flywaydb.core.Flyway;
import org.slf4j.bridge.SLF4JBridgeHandler;

import com.johannesbrodwall.projectweek.ProjectweekAppConfig;

public class TestDatabase extends Database {

    private static TestDatabase instance = new TestDatabase();

    public static TestDatabase instance() {
        return instance;
    }

    private TestDatabase() {
        super(new ProjectweekAppConfig("projectweek-test.properties"));

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

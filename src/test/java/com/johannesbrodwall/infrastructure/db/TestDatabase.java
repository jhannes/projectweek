package com.johannesbrodwall.infrastructure.db;

import org.flywaydb.core.Flyway;
import org.slf4j.bridge.SLF4JBridgeHandler;

import com.johannesbrodwall.projectweek.ProjectweekAppConfig;

public class TestDatabase extends Database {

    public TestDatabase() {
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();

        dataSource.setJdbcUrl(ProjectweekAppConfig.getProperty("projectweek.test.db.url", "jdbc:postgresql://localhost:5432/projectweek_test"));
        dataSource.setUser(ProjectweekAppConfig.getProperty("projectweek.test.db.username", "projectweek_test"));
        dataSource.setPassword(ProjectweekAppConfig.getProperty("projectweek.test.db.password", "projectweek_test"));
        dataSource.setDriverClass(ProjectweekAppConfig.getProperty("projectweek.test.db.driverClassName", "org.postgresql.Driver"));

        Flyway flyway = new Flyway();
        flyway.setDataSource(dataSource);
        flyway.clean();
        flyway.migrate();

    }
}

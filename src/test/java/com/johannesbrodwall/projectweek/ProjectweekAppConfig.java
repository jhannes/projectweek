package com.johannesbrodwall.projectweek;


import com.johannesbrodwall.infrastructure.AppConfiguration;
import com.mchange.v2.c3p0.DriverManagerDataSource;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

import javax.sql.DataSource;

import lombok.SneakyThrows;

public class ProjectweekAppConfig {

    private static AppConfiguration config = new AppConfiguration("projectweek.properties");
    private DriverManagerDataSource dataSource = new DriverManagerDataSource();

    public static List<String> getProjects() {
        return Arrays.asList(config.getRequiredProperty("projects").split(","));
    }

    public static String getJiraHost(String configurationName) {
        return config.getRequiredProperty("jira.host." + configurationName);
    }

    public static String getJiraUsername(String configurationName) {
        return config.getRequiredProperty("jira.username." + configurationName);
    }

    public static String getJiraPassword(String configurationName) {
        return config.getRequiredProperty("jira.password." + configurationName);
    }

    public static String getProperty(String propertyName, String defaultValue) {
        return config.getProperty(propertyName, defaultValue);
    }

    public static String getRequiredProperty(String propertyName) {
        return config.getRequiredProperty(propertyName);
    }

    @SneakyThrows
    public DataSource getDataSource() {
        String databaseUrl = System.getenv("DATABASE_URL");
        if (databaseUrl != null) {
            URI dbUri = new URI(databaseUrl);
            dataSource.setUser(dbUri.getUserInfo().split(":")[0]);
            dataSource.setPassword(dbUri.getUserInfo().split(":")[1]);
            dataSource.setJdbcUrl("jdbc:postgresql://" + dbUri.getHost() + ':' + dbUri.getPort() + dbUri.getPath());
            dataSource.setDriverClass("org.postgresql.Driver");
        } else {
            dataSource.setUser(getRequiredProperty("projectweek.db.username"));
            dataSource.setPassword(getRequiredProperty("projectweek.db.password"));
            dataSource.setJdbcUrl(getProperty("projectweek.db.url", "jdbc:postgresql://localhost:5432/projectweek"));
            dataSource.setDriverClass(getProperty("projectweek.db.driverClassName",
                    "org.postgresql.Driver"));
        }
        return dataSource;
    }

    public int getPort(int defaultPort) {
        if (System.getenv("PORT") != null) {
            return Integer.parseInt(System.getenv("PORT"));
        }
        return Integer.parseInt(getProperty("projectweek.http.port", String.valueOf(defaultPort)));
    }

}

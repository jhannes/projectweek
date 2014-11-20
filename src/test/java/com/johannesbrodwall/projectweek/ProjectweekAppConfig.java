package com.johannesbrodwall.projectweek;

import com.johannesbrodwall.infrastructure.AppConfiguration;

import java.util.Arrays;
import java.util.List;

import org.flywaydb.core.Flyway;

public class ProjectweekAppConfig extends AppConfiguration {

    private ProjectweekAppConfig() {
        this("projectweek.properties");
    }

    public ProjectweekAppConfig(String propertyFile) {
        super(propertyFile, "projectweek");
    }

    private static ProjectweekAppConfig instance = new ProjectweekAppConfig();

    public List<String> getProjects() {
        return getRequiredPropertyList("projects");
    }

    public List<String> getRequiredPropertyList(String property) {
        return Arrays.asList(getRequiredProperty(property).split(","));
    }

    public String getJiraHost(String configurationName) {
        return getRequiredProperty("jira." + configurationName + ".host");
    }

    public String getJiraUsername(String configurationName) {
        return getProperty("jira." + configurationName + ".username", "username");
    }

    public String getJiraPassword(String configurationName) {
        return getProperty("jira." + configurationName + ".password", "password");
    }

    public static ProjectweekAppConfig instance() {
        return instance;
    }

    public void resetDatabase() {
        Flyway flyway = new Flyway();
        flyway.setDataSource(getDataSource());
        if (getFlag("test.clean-db", false)) {
            flyway.clean();
        }
        flyway.migrate();
    }
}

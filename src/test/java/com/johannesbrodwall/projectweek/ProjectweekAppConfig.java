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
        return Arrays.asList(getRequiredProperty("projects").split(","));
    }

    public String getJiraHost(String configurationName) {
        return getRequiredProperty("jira.host." + configurationName);
    }

    public String getJiraUsername(String configurationName) {
        return getRequiredProperty("jira.username." + configurationName);
    }

    public String getJiraPassword(String configurationName) {
        return getRequiredProperty("jira.password." + configurationName);
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

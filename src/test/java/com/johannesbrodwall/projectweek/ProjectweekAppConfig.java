package com.johannesbrodwall.projectweek;


import com.johannesbrodwall.infrastructure.AppConfiguration;

import java.util.Arrays;
import java.util.List;

public class ProjectweekAppConfig {

    private static AppConfiguration config = new AppConfiguration("projectweek.properties");


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

}

package com.johannesbrodwall.projectweek.projects;

import org.json.JSONArray;
import org.json.JSONObject;

import com.johannesbrodwall.infrastructure.jira.JiraClient;
import com.johannesbrodwall.projectweek.ProjectweekAppConfig;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JiraProjectLoader {

    private ProjectRepository projectRepository = new ProjectRepository();
    private JiraClient jiraClient;

    public JiraProjectLoader(String configurationName, ProjectweekAppConfig config) {
        jiraClient = new JiraClient(config, configurationName);
    }

    @SneakyThrows
    public void load() {
        JSONArray projects = jiraClient.httpGetJSONArray("/rest/api/2/project");
        log.info("Retrieved " + projects.length());

        for (int i = 0; i < projects.length(); i++) {
            JSONObject project = projects.getJSONObject(i);
            projectRepository.insertOrUpdate(new Project(project.getString("key"), project.getString("name")));
        }
    }

}

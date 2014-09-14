package com.johannesbrodwall.projectweek.projects;

import org.json.JSONArray;
import org.json.JSONObject;

import com.johannesbrodwall.infrastructure.jira.JiraClient;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JiraProjectLoader {

    private String configurationName;
    private ProjectRepository projectRepository;

    public JiraProjectLoader(String configurationName, ProjectRepository projectRepository) {
        this.configurationName = configurationName;
        this.projectRepository = projectRepository;
    }

    @SneakyThrows
    public void load() {
        JSONArray projects = JiraClient.httpGetJSONArray(configurationName, "/rest/api/2/project");
        log.info("Retrieved " + projects.length());

        for (int i = 0; i < projects.length(); i++) {
            JSONObject project = projects.getJSONObject(i);
            projectRepository.insertOrUpdate(new Project(project.getString("key"), project.getString("name")));
        }
    }

}

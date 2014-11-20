package com.johannesbrodwall.projectweek.issues;

import org.json.JSONArray;
import org.json.JSONObject;

import com.johannesbrodwall.infrastructure.jira.JiraClient;
import com.johannesbrodwall.projectweek.ProjectweekAppConfig;
import java.io.IOException;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class JiraIssuesLoader {

    private IssuesRepository issueRepository = new IssuesRepository();
    private JiraClient jiraClient;

    public JiraIssuesLoader(String configurationName, ProjectweekAppConfig config) {
        this.jiraClient = new JiraClient(config, configurationName);
    }

    public void load(String project) throws IOException {
        JSONObject result = jiraClient.httpGetJSONObject("/rest/api/2/search?jql=project=" + project + "&maxResults=1&expand=changelog");

        JSONArray issues = result.getJSONArray("issues");
        for (int i = 0; i < issues.length(); i++) {
            issueRepository.insertOrUpdate(toEntity(issues.getJSONObject(i)));
        }
    }

    private Issue toEntity(JSONObject json) throws IOException {
        Issue issue = new Issue(
                json.getString("key"),
                json.getJSONObject("fields").getJSONObject("project").getString("key"));

        readWorkHistory(issue, json.getJSONObject("changelog").getJSONArray("histories"));
        readWorklog(issue, json.getString("id"));

        return issue;
    }

    private void readWorklog(Issue issue, String id) throws IOException {
        JSONObject worklog = jiraClient.httpGetJSONObject("/rest/api/2/issue/" + id + "/worklog");
        JSONArray worklogs = worklog.getJSONArray("worklogs");
        for (int i = 0; i < worklogs.length(); i++) {
            JSONObject worklogItem = worklogs.getJSONObject(i);
            issue.addWorklog(getInstant(worklogItem, "started"), worklogItem.getJSONObject("author").getString("name"),
                    worklogItem.getInt("timeSpentSeconds"));
        }
    }

    private void readWorkHistory(Issue issue, JSONArray histories) {
        for (int i = 0; i < histories.length(); i++) {
            JSONObject history = histories.getJSONObject(i);
            JSONArray items = history.getJSONArray("items");
            for (int j = 0; j < items.length(); j++) {
                if (items.getJSONObject(j).getString("field").equals("status")) {
                    issue.addStatus(
                            getInstant(history, "created"),
                            items.getJSONObject(j).getString("toString"));
                }
            }
        }
    }

    private Instant getInstant(JSONObject object, String field) {
        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        return ZonedDateTime.parse(object.getString(field), format).toInstant();
    }

}

package com.johannesbrodwall.infrastructure.jira;

import org.json.JSONArray;
import org.json.JSONObject;

import com.johannesbrodwall.projectweek.ProjectweekAppConfig;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Base64;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JiraClient {

    public static JSONArray httpGetJSONArray(String configurationName, String serviceUrl) throws IOException {
        return new JSONArray(httpGetString(configurationName, serviceUrl));
    }

    public static JSONObject httpGetJSONObject(String configurationName, String serviceUrl) throws IOException {
        return new JSONObject(httpGetString(configurationName, serviceUrl));
    }

    public static String httpGetString(String configurationName, String serviceUrl) throws IOException {
        URL url = new URL(ProjectweekAppConfig.getJiraHost(configurationName) + serviceUrl);
        String username = ProjectweekAppConfig.getJiraUsername(configurationName);
        String password = ProjectweekAppConfig.getJiraPassword(configurationName);

        if (url.getProtocol().equals("file") && serviceUrl.contains("?")) {
            // For testing with file URLs (!)
            url = new URL(ProjectweekAppConfig.getJiraHost(configurationName) +
                    serviceUrl.substring(0, serviceUrl.indexOf('?')));
        }
        log.info("Fetching " + url);

        URLConnection connection = url.openConnection();
        HttpURLConnection.setFollowRedirects(true);
        String authorization = username + ":" + password;
        connection.setRequestProperty("Authorization",
                "Basic " + Base64.getEncoder().encodeToString(authorization.getBytes()));

        String result = "";
        try (BufferedReader input = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            String line = null;
            while ((line = input.readLine()) != null) result += line;
        }
        return result;
    }

}

package com.johannesbrodwall.projectweek.issues;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
public class Issue {

    @Getter @Setter
    private Integer id;

    @Getter
    private final String key, projectKey;

    @Getter @Setter
    private Instant updated;

    @Getter
    private List<IssueStatus> statusChanges = new ArrayList<>();

    @Getter
    private List<Worklog> worklogs = new ArrayList<>();


    public void addStatus(Instant createdAt, String status) {
        statusChanges.add(new IssueStatus(createdAt, status));
    }


    public void addWorklog(Instant workStarted, String author, int secondsWorked) {
        worklogs.add(new Worklog(workStarted, secondsWorked, author));
    }

}

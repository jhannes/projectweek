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
    private List<IssueStatus> statusChanges = new ArrayList<IssueStatus>();

    public List<IssueStatus> getStatusChanges() {
        return statusChanges;
    }

    public void addStatus(Instant createdAt, String status) {
        statusChanges.add(new IssueStatus(createdAt, status));
    }


}

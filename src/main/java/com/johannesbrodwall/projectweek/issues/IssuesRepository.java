package com.johannesbrodwall.projectweek.issues;

import com.johannesbrodwall.infrastructure.Repository;
import com.johannesbrodwall.infrastructure.db.Database;

import java.util.Collection;
import java.util.SortedMap;
import java.util.TreeMap;

import lombok.SneakyThrows;

public class IssuesRepository implements Repository<Issue> {

    @Override
    @SneakyThrows
    public void deleteAll() {
        Database.executeOperation("DELETE FROM Issues");
    }

    @Override
    @SneakyThrows
    public Collection<Issue> findAll() {
        SortedMap<Integer, Issue> result = new TreeMap<>();
        Database.executeQuery("SELECT * FROM Issues ORDER BY key", (rs) -> {
            result.put(rs.getInt("id"), new Issue(rs.getString("key"), rs.getString("project_key")));
            return null;
        });

        Database.executeQuery("SELECT * FROM Issue_Status", (rs) -> {
           Issue issue = result.get(rs.getInt("issue_id"));
           issue.addStatus(rs.getTimestamp("created_at").toInstant(),
                   rs.getString("status"));
           return null;
        });

        return result.values();
    }

    @Override
    @SneakyThrows
    public void insertOrUpdate(Issue issue) {
        Database.executeOperation("INSERT INTO Issues (key, project_key) VALUES (?, ?)",
                issue.getKey(), issue.getProjectKey());
        issue.setId(Database.queryForPrimaryInt("SELECT last_value FROM Issues_Id_Seq"));

        for (IssueStatus status : issue.getStatusChanges()) {
            Database.executeOperation("INSERT INTO Issue_Status (issue_id, created_at, status) VALUES (?, ?, ?)",
                    issue.getId(), status.getCreatedAt(), status.getStatus());
        }
    }
}

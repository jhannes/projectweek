package com.johannesbrodwall.projectweek.issues;

import com.johannesbrodwall.infrastructure.Repository;
import com.johannesbrodwall.infrastructure.db.Database;
import com.johannesbrodwall.infrastructure.db.Query;
import com.johannesbrodwall.projectweek.ProjectweekDatabase;

import java.sql.SQLException;
import java.util.Collection;
import java.util.SortedMap;
import java.util.TreeMap;

import lombok.SneakyThrows;

public class IssuesRepository implements Repository<Issue> {

    private Database database = new ProjectweekDatabase();

    @Override
    @SneakyThrows
    public void deleteAll() {
        database.executeOperation("DELETE FROM Issues");
    }

    @Override
    @SneakyThrows
    public Collection<Issue> findAll() {
        SortedMap<Integer, Issue> result = new TreeMap<>();
        database.query("SELECT * FROM Issues ORDER BY key", (rs) -> {
            Issue issue = new Issue(rs.getString("key"), rs.getString("project_key"));
            issue.setId(rs.getInt("id"));
            result.put(rs.getInt("id"), issue);
        });

        database.query("SELECT * FROM Issue_Status", (rs) -> {
            Issue issue = result.get(rs.getInt("issue_id"));
            if (issue != null) {
                issue.addStatus(rs.getTimestamp("created_at").toInstant(),
                       rs.getString("status"));
            }
        });

        database.query("SELECT * FROM Issue_Worklogs", (rs) -> {
            Issue issue = result.get(rs.getInt("issue_id"));
            if (issue != null) {
                issue.addWorklog(rs.getTimestamp("work_started_at").toInstant(),
                        rs.getString("worker"), rs.getInt("seconds_worked"));
            }
        });

        return result.values();
    }

    @Override
    @SneakyThrows
    public void insertOrUpdate(Issue issue) {
        issue.setId(database.queryForSingle("SELECT id FROM Issues WHERE key = ?",
                (rs) -> rs.getInt("id"),
                issue.getKey()));
        if (issue.getId() == null) {
            database.executeOperation("INSERT INTO Issues (key, project_key) VALUES (?, ?)",
                    issue.getKey(), issue.getProjectKey());
            issue.setId(database.queryForPrimaryInt("SELECT last_value FROM Issues_Id_Seq"));
        } else {
            database.executeOperation("UPDATE Issues SET key = ?, project_key = ? WHERE id = ?",
                    issue.getKey(), issue.getProjectKey(), issue.getId());

            database.executeOperation("DELETE FROM Issue_Status WHERE issue_id = ?", issue.getId());
            database.executeOperation("DELETE FROM Issue_Worklogs WHERE issue_id = ?", issue.getId());
        }
        insertIssueStatus(issue);
        insertIssueWorklogs(issue);
    }

    private void insertIssueWorklogs(Issue issue) throws SQLException {
        for (Worklog worklog : issue.getWorklogs()) {
            database.executeOperation(
                    "INSERT INTO Issue_Worklogs (issue_id, work_started_at, worker, seconds_worked) values (?, ?, ?, ?)",
                    issue.getId(), worklog.getWorkStarted(), worklog.getAuthor(), worklog.getSecondsWorked());
        }
    }

    private void insertIssueStatus(Issue issue) throws SQLException {
        for (IssueStatus status : issue.getStatusChanges()) {
            database.executeOperation(
                    "INSERT INTO Issue_Status (issue_id, created_at, status) VALUES (?, ?, ?)",
                    issue.getId(), status.getCreatedAt(), status.getStatus());
        }
    }

    @SneakyThrows
    public Issue findSingle(Query query) {
        Issue issue = database.queryForSingle("SELECT * FROM Issues WHERE " + query.getWhereClause("key"),
            (rs) -> {
                Issue result = new Issue(rs.getString("key"), rs.getString("project_key"));
                result.setId(rs.getInt("id"));
                return result;
            }, query.getParameters());

        database.query("SELECT * FROM Issue_Status WHERE Issue_id = ?", (rs) -> {
            issue.addStatus(rs.getTimestamp("created_at").toInstant(),
                    rs.getString("status"));
         }, issue.getId());

         database.query("SELECT * FROM Issue_Worklogs WHERE Issue_id = ?", (rs) -> {
             issue.addWorklog(rs.getTimestamp("work_started_at").toInstant(),
                     rs.getString("worker"), rs.getInt("seconds_worked"));
         }, issue.getId());

        return issue;
    }
}

package com.johannesbrodwall.projectweek.issues;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import com.johannesbrodwall.infrastructure.db.Database;
import com.johannesbrodwall.infrastructure.db.Query;
import com.johannesbrodwall.infrastructure.db.TestDatabase;
import com.johannesbrodwall.projectweek.ProjectweekAppConfig;

import java.io.IOException;
import java.util.Collection;

public class IssuesLoaderTest {

    private IssuesRepository repository = new IssuesRepository();
    private Database database = new TestDatabase();
    private JiraIssuesLoader loader = new JiraIssuesLoader("test");

    @Test
    public void shouldLoadAllProjectsFromJira() throws IOException {
        database.executeInTransaction(() -> repository.deleteAll());

        String project = ProjectweekAppConfig.getProjects().get(0);
        database.executeInTransaction(() -> loader.load(project));

        Collection<Issue> issues = database.executeInTransaction(() -> repository.findAll());
        assertThat(issues)
            .isNotEmpty()
            .extracting("projectKey").containsOnly(project);

        assertThat(issues.iterator().next().getStatusChanges()).extracting("status")
            .isNotEmpty()
            .doesNotContain(new Object[] { null });

        Worklog someWorklog = issues.iterator().next().getWorklogs().iterator().next();
        assertThat(someWorklog.getHours()).isPositive();
        assertThat(someWorklog.getAuthor()).isNotEmpty();
    }

    @Test
    public void shouldOnlyLoadProjectOnce() throws IOException {
        database.executeInTransaction(() -> repository.deleteAll());

        String project = ProjectweekAppConfig.getProjects().get(0);
        database.executeInTransaction(() -> loader.load(project));
        Collection<Issue> issues = database.executeInTransaction(() -> repository.findAll());
        Issue firstIssue = issues.iterator().next();

        database.executeInTransaction(() -> loader.load(project));
        Issue issueSecond = database.executeInTransaction(() ->
            repository.findSingle(new Query("key", firstIssue.getKey())));
        assertThat(issueSecond.getId()).isEqualTo(firstIssue.getId());

        assertThat(firstIssue.getWorklogs()).isEqualTo(issueSecond.getWorklogs());
    }

}

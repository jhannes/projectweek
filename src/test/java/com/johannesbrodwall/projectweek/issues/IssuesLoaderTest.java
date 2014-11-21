package com.johannesbrodwall.projectweek.issues;

import static com.johannesbrodwall.projectweek.ProjectweekAssertions.assertThat;

import org.junit.Before;
import org.junit.Test;

import com.johannesbrodwall.infrastructure.db.Database;
import com.johannesbrodwall.infrastructure.db.Query;
import com.johannesbrodwall.projectweek.ProjectweekAppConfig;
import com.johannesbrodwall.projectweek.ProjectweekDatabase;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.List;

public class IssuesLoaderTest {

    private ProjectweekAppConfig config = new ProjectweekAppConfig("projectweek-test.properties");

    private IssuesRepository repository = new IssuesRepository();
    private Database database = new ProjectweekDatabase(config.getDataSource());
    private JiraIssuesLoader loader = new JiraIssuesLoader("test", config);

    @Before
    public void resetDatabase() {
        config.resetDatabase();
    }

    @Test
    public void shouldLoadAllProjectsFromJira() throws IOException {
        database.executeInTransaction(() -> repository.deleteAll());

        String project = getProjects().get(0);
        database.executeInTransaction(() -> loader.load(project));

        Collection<Issue> issues = database.executeInTransaction(() -> repository.findAll());
        assertThat(issues)
            .isNotEmpty()
            .extracting("projectKey").containsOnly(project);

        Issue issue = issues.iterator().next();

        assertThat(issue.getUpdated())
            .isAfter(LocalDate.of(1999, 1, 1));

        assertThat(issue.getStatusChanges()).extracting("status")
            .isNotEmpty()
            .doesNotContain(new Object[] { null });

        Worklog someWorklog = issue.getWorklogs().iterator().next();
        assertThat(someWorklog.getHours()).isPositive();
        assertThat(someWorklog.getAuthor()).isNotEmpty();
    }

    @Test
    public void shouldOnlyLoadIssueOnce() throws IOException {
        database.executeInTransaction(() -> repository.deleteAll());

        String project = getProjects().get(0);
        database.executeInTransaction(() -> loader.load(project));
        Collection<Issue> issues = database.executeInTransaction(() -> repository.findAll());
        Issue firstIssue = issues.iterator().next();

        database.executeInTransaction(() -> loader.load(project));
        Issue issueSecond = database.executeInTransaction(() ->
            repository.findSingle(new Query("key", firstIssue.getKey())));
        assertThat(issueSecond.getId()).isEqualTo(firstIssue.getId());

        assertThat(firstIssue.getWorklogs()).isEqualTo(issueSecond.getWorklogs());
    }

    @Test
    public void shouldStartAtLastLoadPoint() {
        database.executeInTransaction(() -> {
            assertThat(loader.getJql("TEST")).isEqualTo("project=\"TEST\"+order+by+updated+asc");
        });

        database.executeInTransaction(() -> repository.deleteAll());
        Issue issue = new Issue("TEST-11", "TEST");
        issue.setUpdated(ZonedDateTime.of(2011, 10, 1, 22, 31, 11, 0, ZoneId.of("CET")).toInstant());
        database.executeInTransaction(() -> repository.insertOrUpdate(issue));

        database.executeInTransaction(() -> {
            assertThat(loader.getJql("TEST"))
            .isEqualTo("project=\"TEST\"+and+updated+>+\"2011-10-01+22:31\"+order+by+updated+asc");
        });
    }

    private List<String> getProjects() {
        return config.getRequiredPropertyList("projects");
    }

}

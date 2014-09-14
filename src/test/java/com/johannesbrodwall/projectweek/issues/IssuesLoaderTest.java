package com.johannesbrodwall.projectweek.issues;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import com.johannesbrodwall.infrastructure.db.Database;
import com.johannesbrodwall.infrastructure.db.TestDatabase;
import com.johannesbrodwall.projectweek.ProjectweekAppConfig;

import java.io.IOException;
import java.util.Collection;

public class IssuesLoaderTest {

    private IssuesRepository repository = new IssuesRepository();
    private Database database = new TestDatabase();

    @Test
    public void shouldLoadAllProjectsFromJira() throws IOException {
        database.executeInTransaction(() -> repository.deleteAll());

        JiraIssuesLoader loader = new JiraIssuesLoader("test");
        String project = ProjectweekAppConfig.getProjects().get(0);
        database.executeInTransaction(() -> loader.load(project));

        Collection<Issue> issues = database.executeInTransaction(() -> repository.findAll());
        assertThat(issues)
            .isNotEmpty()
            .extracting("projectKey").containsOnly(project);

        assertThat(issues.iterator().next().getStatusChanges()).extracting("status")
            .isNotEmpty()
            .doesNotContain(new Object[] { null });
    }

}

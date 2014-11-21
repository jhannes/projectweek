package com.johannesbrodwall.projectweek.issues;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;

import org.junit.Before;
import org.junit.Test;

import com.johannesbrodwall.infrastructure.db.Database;
import com.johannesbrodwall.projectweek.ProjectweekAppConfig;

public class IssuesRepositoryTest {

    private ProjectweekAppConfig config = new ProjectweekAppConfig("project-test.properties");

    private Database database = new Database(config.getDataSource());

    @Before
    public void resetDatabase() {
        config.resetDatabase();
    }


    @Test
    public void shouldFindLatestUpdatedTime() {
        IssuesRepository repository = new IssuesRepository();


        Instant instant = Instant.now();
        database.executeInTransaction(() -> {
            repository.insertOrUpdate(createIssue("A-2", instant.minusSeconds(3600)));
            repository.insertOrUpdate(createIssue("A-3", instant.minusSeconds(7200)));
            repository.insertOrUpdate(createIssue("A-1", instant));
        });

        assertThat(database.executeInTransaction(() -> repository.getLastModified("A")))
            .isEqualTo(instant);

        assertThat(database.executeInTransaction(() -> repository.getLastModified("B")))
            .isNull();
    }


    private Issue createIssue(String key, Instant updated) {
        Issue issue = new Issue(key, "A");
        issue.setUpdated(updated);
        return issue;
    }

}

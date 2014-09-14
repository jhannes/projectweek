package com.johannesbrodwall.projectweek.projects;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import com.johannesbrodwall.infrastructure.db.Database;
import com.johannesbrodwall.infrastructure.db.TestDatabase;
import com.johannesbrodwall.projectweek.ProjectweekAppConfig;

import java.io.IOException;

public class JiraProjectLoaderTest {

    private ProjectRepository projectRepository = new ProjectRepository();
    private Database database = new TestDatabase();

    @Test
    public void shouldLoadAllProjectsFromJira() throws IOException {
        database.executeInTransaction(() -> projectRepository.deleteAll());

        JiraProjectLoader loader = new JiraProjectLoader("test", projectRepository);
        database.executeInTransaction(() -> loader.load());

        assertThat(database.executeInTransaction(() -> projectRepository.findAll()))
            .isNotEmpty()
            .extracting("key").containsAll(ProjectweekAppConfig.getProjects());
    }
}

package com.johannesbrodwall.projectweek.projects;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import com.johannesbrodwall.infrastructure.db.Database;
import com.johannesbrodwall.infrastructure.db.TestDatabase;
import com.johannesbrodwall.projectweek.ProjectweekAppConfig;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class JiraProjectLoaderTest {

    private ProjectRepository projectRepository = new ProjectRepository();
    private Database database = new TestDatabase();
    private JiraProjectLoader loader = new JiraProjectLoader("test", projectRepository);

    @Test
    public void shouldLoadAllProjectsFromJira() throws IOException {
        database.executeInTransaction(() -> projectRepository.deleteAll());

        database.executeInTransaction(() -> loader.load());

        assertThat(database.executeInTransaction(() -> projectRepository.findAll()))
            .isNotEmpty()
            .extracting("key").containsAll(ProjectweekAppConfig.getProjects());
    }

    @Test
    public void shouldUpdateProjects() {
        database.executeInTransaction(() -> projectRepository.deleteAll());

        database.executeInTransaction(() -> loader.load());
        database.executeInTransaction(() -> loader.load());

        Map<String, List<Project>> byKey = database.executeInTransaction(() -> projectRepository.findAll())
            .stream().collect(Collectors.groupingBy((p) -> p.getKey()));

        String firstKey = byKey.keySet().iterator().next();
        assertThat(byKey.get(firstKey)).hasSize(1);
    }
}

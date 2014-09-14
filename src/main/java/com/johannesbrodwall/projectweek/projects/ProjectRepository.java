package com.johannesbrodwall.projectweek.projects;

import com.johannesbrodwall.infrastructure.Repository;
import com.johannesbrodwall.infrastructure.db.Database;

import java.util.Collection;

import lombok.SneakyThrows;

public class ProjectRepository implements Repository<Project> {

    @SneakyThrows
    @Override
    public void deleteAll() {
        Database.executeOperation("delete from projects");
    }

    @SneakyThrows
    @Override
    public Collection<Project> findAll() {
        return Database.executeQuery("select * from projects", (rs) -> {
            Project project = new Project(rs.getString("key"), rs.getString("displayName"));
            project.setId(rs.getInt("id"));
            return project;
        });
    }

    @SneakyThrows
    @Override
    public void insertOrUpdate(Project project) {
        Database.executeOperation("insert into projects (key, displayName) values (?, ?)",
                project.getKey(), project.getName());
        project.setId(Database.queryForPrimaryInt("select last_value from projects_id_seq"));
    }
}

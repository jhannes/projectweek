package com.johannesbrodwall.projectweek.projects;

import com.johannesbrodwall.infrastructure.Repository;
import com.johannesbrodwall.infrastructure.db.Database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

import lombok.SneakyThrows;

public class ProjectRepository implements Repository<Project> {

    @SneakyThrows
    @Override
    public void deleteAll() {
        Database.executeOperation("delete from projects");
    }

    private Project mapToEntity(ResultSet rs) throws SQLException {
        Project project = new Project(rs.getString("key"), rs.getString("displayName"));
        project.setId(rs.getInt("id"));
        return project;
    }

    @SneakyThrows
    @Override
    public Collection<Project> findAll() {
        return Database.queryForList("select * from projects", this::mapToEntity);
    }

    @SneakyThrows
    @Override
    public void insertOrUpdate(Project project) {
        List<Integer> result = Database.queryForList("select id from projects where key = ?",
                (rs) -> rs.getInt("id"),
                project.getKey());
        if (result.isEmpty()) {
            Database.executeOperation("insert into projects (key, displayName) values (?, ?)",
                    project.getKey(), project.getName());
            project.setId(Database.queryForPrimaryInt("select last_value from projects_id_seq"));
        } else {
            int key = result.get(0);
            Database.executeOperation("update projects set key = ?, displayName = ? where id = ?",
                    project.getKey(), project.getName(), key);
            project.setId(key);
        }

    }
}

package com.johannesbrodwall.projectweek.projects;

import com.johannesbrodwall.infrastructure.Repository;
import com.johannesbrodwall.infrastructure.db.Database;
import com.johannesbrodwall.projectweek.ProjectweekDatabase;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

public class ProjectRepository implements Repository<Project> {

    private Database database = new ProjectweekDatabase();

    @Override
    public void deleteAll() {
        database.executeOperation("delete from projects");
    }

    private Project mapToEntity(ResultSet rs) throws SQLException {
        Project project = new Project(rs.getString("key"), rs.getString("displayName"));
        project.setId(rs.getInt("id"));
        return project;
    }

    @Override
    public Collection<Project> findAll() {
        return database.queryForList("select * from projects", this::mapToEntity);
    }

    @Override
    public void insertOrUpdate(Project project) {
        List<Integer> result = database.queryForList("select id from projects where key = ?",
                (rs) -> rs.getInt("id"),
                project.getKey());
        if (result.isEmpty()) {
            database.executeOperation("insert into projects (key, displayName) values (?, ?)",
                    project.getKey(), project.getName());
            project.setId(database.queryForPrimaryInt("select last_value from projects_id_seq"));
        } else {
            int key = result.get(0);
            database.executeOperation("update projects set key = ?, displayName = ? where id = ?",
                    project.getKey(), project.getName(), key);
            project.setId(key);
        }

    }
}

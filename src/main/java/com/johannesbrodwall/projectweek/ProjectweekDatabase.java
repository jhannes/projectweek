package com.johannesbrodwall.projectweek;

import javax.sql.DataSource;

import com.johannesbrodwall.infrastructure.db.Database;

public class ProjectweekDatabase extends Database {

    public ProjectweekDatabase() {
        this(ProjectweekAppConfig.instance().getDataSource());
    }

    public ProjectweekDatabase(DataSource dataSource) {
        super(dataSource);
    }

}

package com.johannesbrodwall.infrastructure.db;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface ResultSetOperation {

    void execute(ResultSet rs) throws SQLException;

}

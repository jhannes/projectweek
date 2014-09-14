package com.johannesbrodwall.infrastructure.db;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface ResultSetTransformer<T> {

    T execute(ResultSet rs) throws SQLException;

}

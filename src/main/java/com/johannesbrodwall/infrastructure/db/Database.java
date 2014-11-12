package com.johannesbrodwall.infrastructure.db;


import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import lombok.SneakyThrows;

public class Database {

    private static ThreadLocal<Connection> currentConnection = new ThreadLocal<Connection>();

    private DataSource dataSource;

    public Database(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private static Connection getConnection() {
        if (currentConnection.get() == null) {
            throw new IllegalStateException("Start database transaction before database commands");
        }
        return currentConnection.get();
    }

    public <T extends Exception> void executeInTransaction(Action<T> action) throws T {
        executeInTransaction(() -> { action.execute(); return null; });
    }

    @SneakyThrows
    public <T, U extends Exception> T executeInTransaction(Retriever<T, U> object) throws U {
        try (Transaction tx = beginTransaction()) {
            T result = object.execute();
            tx.setCommit();
            return result;
        }
    }

    private Transaction beginTransaction() throws SQLException {
        currentConnection.set(getDataSource().getConnection());
        currentConnection.get().setAutoCommit(false);
        return new Transaction() {

            private boolean commit;

            @Override
            public void close() throws IOException {
                try {
                    if (commit) {
                        currentConnection.get().commit();
                    } else {
                        currentConnection.get().rollback();
                    }
                    currentConnection.get().close();
                    currentConnection.set(null);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void setCommit() {
                this.commit = true;
            }
        };
    }

    private DataSource getDataSource() {
        return dataSource;
    }

    @SneakyThrows
    public int executeOperation(String query, Object... parameters) {
        try (PreparedStatement stmt = Database.getConnection().prepareStatement(query)) {
            setParameters(stmt, parameters);
            return stmt.executeUpdate();
        }
    }

    @SneakyThrows
    public int queryForPrimaryInt(String query) {
        try (Statement stmt = Database.getConnection().createStatement()) {
            try ( ResultSet rs = stmt.executeQuery(query) ) {
                if (rs.next()) return rs.getInt(1);
                throw new IllegalArgumentException("No results: " + query);
            }
        }
    }

    @SneakyThrows
    public <T> List<T> queryForList(String query, ResultSetTransformer<T> transformer, Object... parameters) {
        try (PreparedStatement stmt = Database.getConnection().prepareStatement(query)) {
            setParameters(stmt, parameters);
            try ( ResultSet rs = stmt.executeQuery() ) {
                return transformResultSet(transformer, rs);
            }
        }
    }

    @SneakyThrows
    public void query(String query, ResultSetOperation operation, Object... parameters) {
        try (PreparedStatement stmt = Database.getConnection().prepareStatement(query)) {
            setParameters(stmt, parameters);
            try ( ResultSet rs = stmt.executeQuery() ) {
                while (rs.next()) {
                    operation.execute(rs);
                }
            }
        }
    }

    @SneakyThrows
    public <T> T queryForSingle(String query, ResultSetTransformer<T> transformer, Object... parameters) {
        try (PreparedStatement stmt = Database.getConnection().prepareStatement(query)) {
            setParameters(stmt, parameters);
            try ( ResultSet rs = stmt.executeQuery() ) {
                if (!rs.next()) return null;

                T result = transformer.execute(rs);
                if (rs.next()) {
                    throw new IllegalStateException("More than one result " + query);
                }
                return result;
            }
        }
    }

    private void setParameters(PreparedStatement stmt, Object... parameters)
            throws SQLException {
        for (int i = 0; i < parameters.length; i++) {
            if (parameters[i] instanceof Instant) {
                Instant instant = (Instant)parameters[i];
                stmt.setTimestamp(i+1, new Timestamp(instant.toEpochMilli()));
            } else {
                stmt.setObject(i+1, parameters[i]);
            }
        }
    }

    private <T> List<T> transformResultSet(ResultSetTransformer<T> transformer, ResultSet rs) throws SQLException {
        List<T> result = new ArrayList<T>();
        while (rs.next()) {
            result.add(transformer.execute(rs));
        }
        return result;
    }

    public Instant getInstant(ResultSet rs, String fieldName) throws SQLException {
        Timestamp timestamp = rs.getTimestamp(fieldName);
        return timestamp != null ? timestamp.toInstant() : null;
    }

}

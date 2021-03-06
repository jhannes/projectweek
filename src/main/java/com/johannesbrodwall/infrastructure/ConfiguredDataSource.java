package com.johannesbrodwall.infrastructure;

import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.sql.DataSource;

import com.zaxxer.hikari.HikariDataSource;

class ConfiguredDataSource implements DataSource {

    private HikariDataSource dataSource = new HikariDataSource();
    private AppConfiguration config;
    private final String propertyPrefix;
    private String defaultUsername;
    private String defaultUrl;
    private String defaultDriver;

    ConfiguredDataSource(AppConfiguration config, String propertyPrefix) {
        this.config = config;
        this.propertyPrefix = propertyPrefix;
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return getDataSource().getConnection(username, password);
    }

    @Override
    public Connection getConnection() throws SQLException {
        return getDataSource().getConnection();
    }

    private DataSource getDataSource() {
        updateProperties();
        return dataSource;
    }

    private void updateProperties() {
        String databaseUrl = System.getenv("DATABASE_URL");
        if (databaseUrl != null) {
            URI dbUri = getDatabaseUri(databaseUrl);
            dataSource.setUsername(dbUri.getUserInfo().split(":")[0]);
            dataSource.setPassword(dbUri.getUserInfo().split(":")[1]);
            dataSource.setJdbcUrl(getJdbcUrlFromDbUrl(dbUri));
            dataSource.setDriverClassName(getJdbcDriverFromDbUrl(dbUri));
        } else {
            dataSource.setUsername(config.getProperty(propertyPrefix + ".db.username", defaultUsername));
            dataSource.setPassword(config.getProperty(propertyPrefix + ".db.password", dataSource.getUsername()));
            dataSource.setJdbcUrl(config.getProperty(propertyPrefix + ".db.url", defaultUrl));
            dataSource.setDriverClassName(config.getProperty(propertyPrefix + ".db.driverClassName",
                    defaultDriver));
        }
    }

    private URI getDatabaseUri(String databaseUrl) {
        try {
            return new URI(databaseUrl);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Invalid database URL " + databaseUrl);
        }
    }

    String getJdbcUrlFromDbUrl(URI dbUri) {
        Map<String, String> formats = new HashMap<>();
        formats.put("postgres", "jdbc:postgresql://%2$s:%3$d/%4$s");
        formats.put("oracle", "jdbc:%1$s:thin:@%2$s:%3$d:%4$s");

        String format = formats.get(dbUri.getScheme());
        if (format == null && dbUri.getPort() == -1) {
            format = "jdbc:%1$s://%2$s/%4$s";
        } else if (format == null) {
            format = "jdbc:%1$s://%2$s:%3$d/%4$s";
        }

        return String.format(format,
                dbUri.getScheme(), dbUri.getHost(), dbUri.getPort(), dbUri.getPath().substring(1));
    }

    String getJdbcDriverFromDbUrl(URI dbUri) {
        Map<String, String> drivers = new HashMap<>();
        drivers.put("postgres", "org.postgresql.Driver");
        drivers.put("oracle", "oracle.jdbc.OracleDriver");
        drivers.put("mysql", "com.mysql.jdbc.Driver");
        return drivers.getOrDefault(dbUri.getScheme(), "java.sql.Driver");
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return getDataSource().unwrap(iface);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return getDataSource().isWrapperFor(iface);
    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
        getDataSource().setLoginTimeout(seconds);
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {
        getDataSource().setLogWriter(out);
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return getDataSource().getParentLogger();
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return getDataSource().getLoginTimeout();
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return getDataSource().getLogWriter();
    }

    public void setDefaultUsername(String defaultUsername) {
        this.defaultUsername = defaultUsername;
    }

    public void setDefaultUrl(String defaultUrl) {
        this.defaultUrl = defaultUrl;
    }

    public void setDefaultDriver(String defaultDriver) {
        this.defaultDriver = defaultDriver;
    }

    static DataSource getOracleDataSource(AppConfiguration config, String propertyPrefix) {
        ConfiguredDataSource dataSource = new ConfiguredDataSource(config, propertyPrefix);
        dataSource.setDefaultUsername(propertyPrefix);
        dataSource.setDefaultUrl("jdbc:oracle:thin:@localhost:1521:XE");
        dataSource.setDefaultDriver("oracle.jdbc.driver.OracleDriver");
        return dataSource;
    }

    static DataSource getPostgresDataSource(AppConfiguration config, String propertyPrefix) {
        ConfiguredDataSource dataSource = new ConfiguredDataSource(config, propertyPrefix);
        dataSource.setDefaultUsername(propertyPrefix);
        dataSource.setDefaultUrl("jdbc:postgresql://localhost:5432/" + propertyPrefix);
        dataSource.setDefaultDriver("org.postgresql.Driver");
        return dataSource;
    }
}
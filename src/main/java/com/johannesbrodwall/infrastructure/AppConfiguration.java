package com.johannesbrodwall.infrastructure;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import javax.sql.DataSource;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AppConfiguration {

    private long nextCheckTime = 0;
    private long lastLoadTime = 0;
    private Properties properties = new Properties();
    private Properties defaultProperties = new Properties();
    private final File configFile;
    protected final String propertyPrefix;
    private DataSource dataSource;

    public AppConfiguration(String filename, String propertyPrefix) {
        this.configFile = new File(filename);
        this.propertyPrefix = propertyPrefix;
        dataSource = new ConfiguredDataSource(this, propertyPrefix);
    }

    public String getProperty(String propertyName, String defaultValue) {
        String result = getProperty(propertyName);
        if (result == null) {
            log.trace("Missing property {} in {}", propertyName, properties.keySet());
            return defaultValue;
        }
        return result;
    }

    public String getRequiredProperty(String propertyName) {
        String result = getProperty(propertyName);
        if (result == null) {
            throw new RuntimeException("Missing property " + propertyName);
        }
        return result;
    }

    private String getProperty(String propertyName) {
        if (System.getenv(propertyName.replace('.', '_')) != null) {
            return System.getenv(propertyName.replace('.', '_'));
        }

        ensureConfigurationIsFresh();
        return properties.getProperty(propertyName, defaultProperties.getProperty(propertyName));
    }

    private synchronized void ensureConfigurationIsFresh() {
        if (System.currentTimeMillis() < nextCheckTime) return;
        nextCheckTime = System.currentTimeMillis() + 10000;
        log.trace("Rechecking {}", configFile);

        if (!configFile.exists()) {
            log.error("Missing configuration file {}", configFile);
        }

        if (lastLoadTime >= configFile.lastModified()) return;
        lastLoadTime = configFile.lastModified();
        log.debug("Reloading {}", configFile);

        try (FileInputStream inputStream = new FileInputStream(configFile)) {
            properties.clear();
            properties.load(inputStream);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load " + configFile, e);
        }
    }

    @SneakyThrows
    public DataSource getDataSource() {
        return dataSource;
    }

    public int getHttpPort(int defaultPort) {
        if (System.getenv("PORT") != null) {
            return Integer.parseInt(System.getenv("PORT"));
        }
        return getIntProperty(propertyPrefix + ".http.port", defaultPort);
    }

    public boolean getFlag(String property, boolean defaultValue) {
        return Boolean.parseBoolean(getProperty(property, String.valueOf(defaultValue)));
    }

    public int getIntProperty(String propertyName, int defaultValue) {
        return Integer.parseInt(getProperty(propertyName, String.valueOf(defaultValue)));
    }

}
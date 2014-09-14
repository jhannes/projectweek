package com.johannesbrodwall.infrastructure;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AppConfiguration {

    private long nextCheckTime = 0;
    private long lastLoadTime = 0;
    private Properties properties = new Properties();
    private Properties defaultProperties = new Properties();
    private final File configFile;

    public AppConfiguration(String filename) {
        this.configFile = new File(filename);
    }

    public void setDefault(String key, String value) {
        defaultProperties.setProperty(key, value);
    }

    public String getProperty(String propertyName, String defaultValue) {
        String result = getProperty(propertyName);
        if (result == null) {
            log.debug("Missing property {} in {}", propertyName, properties.keySet());
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

        if (lastLoadTime >= configFile.lastModified()) return;
        lastLoadTime = configFile.lastModified();
        log.debug("Reloading {}", configFile);

        try (FileInputStream inputStream = new FileInputStream(configFile)) {
            properties.clear();
            properties.load(inputStream);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


}
package com.currencyApp.config;

import java.io.InputStream;
import java.util.Properties;

public class Config {
    private static final Properties properties = new Properties();

    static {
        try (InputStream input = Config.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                // Enhancing the exception message with more details
                throw new RuntimeException("config.properties not found in the classpath. Ensure the file is located in src/main/resources.");
            }
            properties.load(input);
        } catch (Exception e) {
            // Provide more clarity for debugging
            throw new RuntimeException("Failed to load config.properties. Check for file existence and readability.", e);
        }
    }

    public static String get(String key) {
        return properties.getProperty(key);
    }
}
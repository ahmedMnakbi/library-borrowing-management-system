package com.library.util;

import com.library.exception.DatabaseException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;

public final class DatabaseConnection {
    private static final String PROPERTIES_FILE = "db.properties";
    private static final Properties PROPERTIES = loadProperties();

    private DatabaseConnection() {
    }

    public static Connection getConnection() throws SQLException {
        ensureDriverLoaded();
        return DriverManager.getConnection(
                PROPERTIES.getProperty("db.url"),
                PROPERTIES.getProperty("db.user"),
                PROPERTIES.getProperty("db.password")
        );
    }

    public static Properties getProperties() {
        Properties copy = new Properties();
        copy.putAll(PROPERTIES);
        return copy;
    }

    private static Properties loadProperties() {
        try (InputStream inputStream = openPropertiesStream()) {
            if (inputStream == null) {
                throw new DatabaseException("File " + PROPERTIES_FILE + " introuvable dans les ressources ou a la racine du projet.", null);
            }
            Properties properties = new Properties();
            properties.load(new java.io.InputStreamReader(inputStream, StandardCharsets.UTF_8));
            Properties normalized = normalizeProperties(properties);
            if (normalized.getProperty("db.url") == null || normalized.getProperty("db.url").isBlank()) {
                throw new DatabaseException("La propriete db.url est manquante dans db.properties.", null);
            }
            return normalized;
        } catch (IOException exception) {
            throw new DatabaseException("Impossible de charger la configuration de base de donnees.", exception);
        }
    }

    private static InputStream openPropertiesStream() throws IOException {
        InputStream classpathStream = DatabaseConnection.class.getClassLoader().getResourceAsStream(PROPERTIES_FILE);
        if (classpathStream != null) {
            return classpathStream;
        }

        Path projectFile = Path.of("src", "main", "resources", PROPERTIES_FILE);
        if (Files.exists(projectFile)) {
            return Files.newInputStream(projectFile);
        }
        return null;
    }

    private static void ensureDriverLoaded() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException mysqlException) {
            try {
                Class.forName("org.mariadb.jdbc.Driver");
            } catch (ClassNotFoundException mariaDbException) {
                throw new DatabaseException("Aucun driver JDBC MySQL/MariaDB n'est disponible dans le classpath.", mariaDbException);
            }
        }
    }

    private static Properties normalizeProperties(Properties source) {
        Properties normalized = new Properties();
        for (Map.Entry<Object, Object> entry : source.entrySet()) {
            String key = stripBom(String.valueOf(entry.getKey())).trim();
            String value = stripBom(String.valueOf(entry.getValue())).trim();
            normalized.setProperty(key, value);
        }
        return normalized;
    }

    private static String stripBom(String value) {
        if (value != null && !value.isEmpty() && value.charAt(0) == '\uFEFF') {
            return value.substring(1);
        }
        return value;
    }
}

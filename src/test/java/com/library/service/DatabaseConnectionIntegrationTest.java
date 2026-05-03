package com.library.service;

import com.library.util.DatabaseConnection;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import java.sql.Connection;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class DatabaseConnectionIntegrationTest {
    @Test
    void shouldOpenJdbcConnectionWhenDatabaseIsAvailable() throws Exception {
        try {
            Connection connection = DatabaseConnection.getConnection();
            assertNotNull(connection);
            assertFalse(connection.isClosed());
            connection.close();
        } catch (Exception exception) {
            Assumptions.abort("Base locale indisponible ou non configuree: " + exception.getMessage());
        }
    }
}

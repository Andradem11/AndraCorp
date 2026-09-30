package storageimpltest;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.junit.jupiter.api.Test;

import storageimpl.DatabaseManager;

/*
 * Tests the SQLite database connection
 * and verifies that all AndraCorp tables
 * are created successfully.
 */
public class DatabaseTest {

    @Test
    public void testDatabaseConnection()
            throws SQLException {

//      Creates the database and tables.
        DatabaseManager.initializeDatabase();

//      Opens a connection to the SQLite database.
        try (Connection connection =
                     DatabaseManager.getConnection()) {

//          Checks that the database connection was created.
            assertNotNull(connection);

//          List of tables required by AndraCorp.
            String[] tables = {
                    "users",
                    "projects",
                    "workers",
                    "work_hours",
                    "expenses",
                    "change_orders",
                    "materials",
                    "equipment",
                    "schedule_tasks",
                    "daily_reports",
                    "photos",
                    "payments",
                    "audit_logs"
            };

//          Checks that every required table exists.
            for (String table : tables) {

                try (ResultSet result =
                             connection
                                     .getMetaData()
                                     .getTables(
                                             null,
                                             null,
                                             table,
                                             null)) {

                    assertTrue(
                            result.next(),
                            table
                                    + " table was not created.");
                }
            }
        }
    }
}
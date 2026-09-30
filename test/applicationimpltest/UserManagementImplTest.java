package applicationimpltest;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import applicationimpl.UserManagementImpl;
import storageimpl.DatabaseManager;

/*
 * Tests user management using
 * the SQLite database.
 */
public class UserManagementImplTest {

    private UserManagementImpl userManager;

//  Creates the database and user manager before each test.
    @BeforeEach
    public void setUp() {

        DatabaseManager.initializeDatabase();

        userManager =
                new UserManagementImpl();
    }

//  Tests creating and retrieving a user.
    @Test
    public void testCreateAndGetUser() {

        String userId =
                userManager.createUser(
                        "Marco Andrade",
                        "marco@test.com",
                        "contractor");

        assertNotNull(userId);

        String user =
                userManager.getUser(userId);

        assertNotNull(user);
        assertTrue(
                user.contains("Marco Andrade"));
        assertTrue(
                user.contains("marco@test.com"));
        assertTrue(
                user.contains("contractor"));
    }

//  Tests updating user information.
    @Test
    public void testUpdateUser() {

        String userId =
                userManager.createUser(
                        "Test User",
                        "old@test.com",
                        "client");

        boolean updated =
                userManager.updateUser(
                        userId,
                        "Updated User",
                        "new@test.com");

        assertTrue(updated);

        String user =
                userManager.getUser(userId);

        assertNotNull(user);
        assertTrue(
                user.contains("Updated User"));
        assertTrue(
                user.contains("new@test.com"));
    }

//  Tests deleting a user.
    @Test
    public void testDeleteUser() {

        String userId =
                userManager.createUser(
                        "Delete User",
                        "delete@test.com",
                        "client");

        boolean deleted =
                userManager.deleteUser(userId);

        assertTrue(deleted);

        assertNull(
                userManager.getUser(userId));
    }
}

package applicationimpl;

import applicationapi.UserManagementAPI;
import storageimpl.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

/*
 * Implementation of user management.
 * Uses SQLite to store and manage contractor
 * and client accounts.
 */
public class UserManagementImpl
        implements UserManagementAPI {

//  Creates a new user account.
    @Override
    public String createUser(
            String name,
            String email,
            String role) {

        String userId =
                UUID.randomUUID().toString();

        String sql = """
                INSERT INTO users (
                    user_id,
                    name,
                    email,
                    role
                )
                VALUES (?, ?, ?, ?);
                """;

        try (Connection connection =
                     DatabaseManager.getConnection();

             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

//          Stores the user information in SQLite.
            statement.setString(
                    1,
                    userId);

            statement.setString(
                    2,
                    name);

            statement.setString(
                    3,
                    email);

            statement.setString(
                    4,
                    role);

            statement.executeUpdate();

            return userId;

        } catch (SQLException e) {

            System.out.println(
                    "Could not create user: "
                            + e.getMessage());

            return null;
        }
    }

//  Retrieves user information.
    @Override
    public String getUser(String userId) {

        String sql = """
                SELECT *
                FROM users
                WHERE user_id = ?;
                """;

        try (Connection connection =
                     DatabaseManager.getConnection();

             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(
                    1,
                    userId);

            try (ResultSet result =
                         statement.executeQuery()) {

                if (!result.next()) {
                    return null;
                }

                return "Name: "
                        + result.getString("name")
                        + "\nEmail: "
                        + result.getString("email")
                        + "\nRole: "
                        + result.getString("role");
            }

        } catch (SQLException e) {

            System.out.println(
                    "Could not retrieve user: "
                            + e.getMessage());

            return null;
        }
    }

//  Updates basic user information.
    @Override
    public boolean updateUser(
            String userId,
            String name,
            String email) {

        String sql = """
                UPDATE users
                SET name = ?,
                    email = ?
                WHERE user_id = ?;
                """;

        try (Connection connection =
                     DatabaseManager.getConnection();

             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(
                    1,
                    name);

            statement.setString(
                    2,
                    email);

            statement.setString(
                    3,
                    userId);

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {

            System.out.println(
                    "Could not update user: "
                            + e.getMessage());

            return false;
        }
    }

//  Removes a user account from SQLite.
    @Override
    public boolean deleteUser(String userId) {

        String sql = """
                DELETE FROM users
                WHERE user_id = ?;
                """;

        try (Connection connection =
                     DatabaseManager.getConnection();

             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(
                    1,
                    userId);

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {

            System.out.println(
                    "Could not delete user: "
                            + e.getMessage());

            return false;
        }
    }
}
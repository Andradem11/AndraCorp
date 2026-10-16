
package applicationimpl;

import applicationapi.UserManagementAPI;
import storageimpl.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/*
 * Implementation of user management.
 * Uses SQLite to store and manage contractor
 * and client accounts.
 */
public class UserManagementImpl implements UserManagementAPI {

//  Creates a new user account.
    @Override
    public String createUser(
            String name,
            String email,
            String phoneNumber,
            String role) {

        if (name == null || name.isBlank()
                || email == null || email.isBlank()
                || role == null || role.isBlank()) {
            return null;
        }

        String userId = UUID.randomUUID().toString();

        String sql = """
                INSERT INTO users (
                    user_id,
                    name,
                    email,
                    phone_number,
                    role
                )
                VALUES (?, ?, ?, ?, ?);
                """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

//          Stores the user information in SQLite.
            statement.setString(1, userId);
            statement.setString(2, name.trim());
            statement.setString(3, email.trim());
            statement.setString(4, phoneNumber);
            statement.setString(5, role.trim().toUpperCase());

            statement.executeUpdate();

            return userId;

        } catch (SQLException e) {
            System.out.println(
                    "Could not create user: " + e.getMessage());
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

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, userId);

            try (ResultSet result = statement.executeQuery()) {

                if (!result.next()) {
                    return null;
                }

                return "Name: "
                        + result.getString("name")
                        + "\nEmail: "
                        + result.getString("email")
                        + "\nPhone Number: "
                        + result.getString("phone_number")
                        + "\nRole: "
                        + result.getString("role");
            }

        } catch (SQLException e) {
            System.out.println(
                    "Could not retrieve user: " + e.getMessage());
            return null;
        }
    }

//  Retrieves all registered clients from SQLite.
    @Override
    public List<Map<String, String>> getAllClients() {
    	

        List<Map<String, String>> clients = new ArrayList<>();

        String sql = """
                SELECT user_id, name, email, phone_number
                FROM users
                WHERE UPPER(role) = 'CLIENT'
                ORDER BY name;
                """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

//          Adds each client to the list.
            while (result.next()) {
                Map<String, String> client = new java.util.HashMap<>();

                client.put("clientId", result.getString("user_id"));
                client.put("name", result.getString("name"));
                client.put("email", result.getString("email"));
                client.put("phoneNumber", result.getString("phone_number"));

                clients.add(client);
            }

        } catch (SQLException e) {
            System.out.println(
                    "Could not retrieve clients: " + e.getMessage());
        }

        return clients;
    }
    

    // Finds an existing client using their name and phone number.
    @Override
    public Map<String, String> findClientByNameAndPhone(
            String name,
            String phoneNumber) {

        if (name == null || name.isBlank()
                || phoneNumber == null || phoneNumber.isBlank()) {
            return null;
        }

        // Ignore spaces, parentheses, and dashes in phone numbers.
        String enteredPhone = phoneNumber.replaceAll("[^0-9]", "");

        if (enteredPhone.isBlank()) {
            return null;
        }

        String sql = """
                SELECT user_id, name, phone_number
                FROM users
                WHERE UPPER(role) = 'CLIENT'
                  AND LOWER(TRIM(name)) = LOWER(?);
                """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, name.trim());

            try (ResultSet result = statement.executeQuery()) {

                Map<String, String> matchedClient = null;

                while (result.next()) {

                    String storedPhone = result.getString("phone_number");

                    if (storedPhone == null) {
                        continue;
                    }

                    String normalizedPhone =
                            storedPhone.replaceAll("[^0-9]", "");

                    if (!enteredPhone.equals(normalizedPhone)) {
                        continue;
                    }

                    // Reject ambiguous matches rather than choosing
                    // the wrong client account.
                    if (matchedClient != null) {
                        return null;
                    }

                    matchedClient = Map.of(
                            "clientId", result.getString("user_id"),
                            "name", result.getString("name")
                    );
                }

                return matchedClient;
            }

        } catch (SQLException e) {
            System.out.println(
                    "Could not find client: " + e.getMessage());
            return null;
        }
    }

//  Updates basic user information.
    @Override
    public boolean updateUser(
            String userId,
            String name,
            String email,
            String phoneNumber) {

        if (userId == null || userId.isBlank()
                || name == null || name.isBlank()
                || email == null || email.isBlank()) {
            return false;
        }

        String sql = """
                UPDATE users
                SET name = ?,
                    email = ?,
                    phone_number = ?
                WHERE user_id = ?;
                """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, name.trim());
            statement.setString(2, email.trim());
            statement.setString(3, phoneNumber);
            statement.setString(4, userId);

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println(
                    "Could not update user: " + e.getMessage());
            return false;
        }
    }

//  Removes a user account from SQLite.
    @Override
    public boolean deleteUser(String userId) {

        if (userId == null || userId.isBlank()) {
            return false;
        }

        String sql = """
                DELETE FROM users
                WHERE user_id = ?;
                """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, userId);

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println(
                    "Could not delete user: " + e.getMessage());
            return false;
        }
    }
    

//  Checks whether a user exists with the specified role.
    @Override
    public boolean userHasRole(String userId, String role) {

        if (userId == null || userId.isBlank()
                || role == null || role.isBlank()) {
            return false;
        }

        String sql = """
                SELECT 1
                FROM users
                WHERE user_id = ?
                AND UPPER(role) = UPPER(?);
                """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, userId);
            statement.setString(2, role.trim());

            try (ResultSet result = statement.executeQuery()) {
                return result.next();
            }

        } catch (SQLException e) {
            System.out.println(
                    "Could not verify user role: " + e.getMessage());
            return false;
        }
    }
    

    // Checks whether a client has project or change order history.
    @Override
    public boolean clientHasHistory(String clientId) {

        if (clientId == null || clientId.isBlank()) {
            return false;
        }

        String sql = """
                SELECT
                    (SELECT COUNT(*) FROM projects
                     WHERE client_id = ?) AS project_count,
                    (SELECT COUNT(*) FROM change_orders
                     WHERE client_id = ?) AS order_count;
                """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, clientId);
            statement.setString(2, clientId);

            try (ResultSet result = statement.executeQuery()) {

                if (result.next()) {
                    return result.getInt("project_count") > 0
                            || result.getInt("order_count") > 0;
                }
            }

        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Could not verify client history.", e);
        }

        return false;
    }

}

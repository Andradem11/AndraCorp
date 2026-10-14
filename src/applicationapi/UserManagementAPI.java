
package applicationapi;

import java.util.List;
import java.util.Map;

/*
 * Handles basic contractor and client
 * account operations.
 */
public interface UserManagementAPI {
    // Creates a new user account.
    String createUser(
            String name,
            String email,
            String phoneNumber,
            String role);

    // Retrieves user information.
    String getUser(String userId);

    // Retrieves all registered clients.
    List<Map<String, String>> getAllClients();

    // Updates basic user information.
    boolean updateUser(
            String userId,
            String name,
            String email,
            String phoneNumber);

    // Removes a user from the system.
    boolean deleteUser(String userId);
}

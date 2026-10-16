
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
    
    // Finds a client using their full name and phone number.
    Map<String, String> findClientByNameAndPhone(
            String name,
            String phoneNumber);

    // Updates basic user information.
    boolean updateUser(
            String userId,
            String name,
            String email,
            String phoneNumber);

    // Removes a user from the system.
    boolean deleteUser(String userId);
    
    // Checks whether a user exists with the specified role.
    boolean userHasRole(String userId, String role);
    
    // Checks whether a client has project or change order history.
    boolean clientHasHistory(String clientId);
}

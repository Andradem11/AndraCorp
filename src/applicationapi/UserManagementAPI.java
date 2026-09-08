package applicationapi;
/*
 * Handles basic contractor and client
 * account operations.
 */
public interface UserManagementAPI {
//	Creates a new user account.
	String createUser(
            String name,
            String email,
            String role);
	
//	Retrieves user information.
	String getUser(String userId);
	
//	Updates basic user information.
	boolean updateUser(
            String userId,
            String name,
            String email);
//	Removes a user from the system.
	boolean deleteUser(String userId);
}

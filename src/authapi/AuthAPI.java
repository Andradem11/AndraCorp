package authapi;
/*
 * This interface is responsible for:
 * - User authentication
 * - User authorization
 * - Roles
 * - Permissions
 */
public interface AuthAPI {
//	Checks whether the user's login information is valid.
	boolean authenticate(String email, String password);
	
//	Checks whether a user has permission to perform
	boolean authorize(String userId, String permission);
	
//	roles: contractor or client.
	String getUserRole(String userId);
}

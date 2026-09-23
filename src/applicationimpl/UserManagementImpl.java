package applicationimpl;

import applicationapi.UserManagementAPI;
import model.User;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/*
 * Prototype implementation of user management.
 * Handles basic contractor and client accounts.
 */

public class UserManagementImpl
        implements UserManagementAPI {

//  Stores users using their IDs.
    private Map<String, User> users =
            new HashMap<>();

//  Creates a new user account.
    @Override
    public String createUser(
            String name,
            String email,
            String role) {

        String userId =
                UUID.randomUUID().toString();

        User user = new User(
                userId,
                name,
                email,
                role);

        users.put(userId, user);

        return userId;
    }

//  Retrieves user information.
    @Override
    public String getUser(String userId) {

        User user = users.get(userId);

        if (user == null) {
            return null;
        }

        return "Name: " + user.getName()
                + "\nEmail: " + user.getEmail()
                + "\nRole: " + user.getRole();
    }

//  Updates basic user information.
    @Override
    public boolean updateUser(
            String userId,
            String name,
            String email) {

        User user = users.get(userId);

        if (user == null) {
            return false;
        }

        user.setName(name);
        user.setEmail(email);

        return true;
    }

//  Removes a user account.
    @Override
    public boolean deleteUser(String userId) {

        return users.remove(userId) != null;
    }

}
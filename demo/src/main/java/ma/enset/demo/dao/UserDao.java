package ma.enset.demo.dao;

import ma.enset.demo.entities.User;

public interface UserDao {

    /**
     * Create a new user in the database
     * @param user The user to create
     * @return true if successful, false otherwise
     */
    boolean create(User user);

    /**
     * Find a user by their ID
     * @param id The user ID
     * @return The User object if found, null otherwise
     */
    User findById(int id);

    /**
     * Find a user by their email
     * @param email The user's email
     * @return The User object if found, null otherwise
     */
    User findByEmail(String email);
    /**
     * Authenticate a user with email and password
     * @param email The user's email
     * @param password The user's password (hashed)
     * @return The User object if authentication successful, null otherwise
     */
    User authenticate(String email, String password);
}
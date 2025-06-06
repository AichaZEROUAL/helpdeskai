package ma.enset.demo.utils;

import ma.enset.demo.entities.User;

/**
 * Singleton class for managing user session information across the application
 */
public class SessionManager {

    private static SessionManager instance;
    private User currentUser;

    // Private constructor to enforce singleton pattern
    private SessionManager() {
    }

    /**
     * Get the singleton instance of SessionManager
     * @return The SessionManager instance
     */
    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    /**
     * Set the current logged-in user
     * @param user The currently logged-in user
     */
    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    /**
     * Get the current logged-in user
     * @return The currently logged-in user, or null if no user is logged in
     */
    public User getCurrentUser() {
        return currentUser;
    }

    /**
     * Clear the current user session
     */
    public void clearSession() {
        this.currentUser = null;
    }

    /**
     * Check if a user is currently logged in
     * @return true if a user is logged in, false otherwise
     */
    public boolean isLoggedIn() {
        return currentUser != null;
    }
}
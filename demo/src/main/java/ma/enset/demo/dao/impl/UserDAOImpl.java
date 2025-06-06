package ma.enset.demo.dao.impl;

import ma.enset.demo.dao.UserDao;
import ma.enset.demo.entities.User;
import ma.enset.demo.utils.DBConnection;

import java.sql.*;

/**
 * Implementation of UserDAO interface
 */
public class UserDAOImpl implements UserDao {

    private final DBConnection dbConnection;

    /**
     * Constructor that initializes the database connection
     */
    public UserDAOImpl() {
        this.dbConnection = new DBConnection();
    }

    @Override
    public boolean create(User user) {
        Connection connection = null;
        PreparedStatement statement = null;
        boolean success = false;

        try {
            connection = dbConnection.getConnection();
            String query = "INSERT INTO users (full_name, email, password) VALUES (?, ?, ?)";
            statement = connection.prepareStatement(query);
            statement.setString(1, user.getFullName());
            statement.setString(2, user.getEmail());
            statement.setString(3, user.getPassword());

            int rowsAffected = statement.executeUpdate();
            success = rowsAffected > 0;

        } catch (SQLException e) {
            System.err.println("Error creating user: " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeResources(null, statement, connection);
        }

        return success;
    }

    @Override
    public User findById(int id) {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        User user = null;

        try {
            connection = dbConnection.getConnection();
            String query = "SELECT * FROM users WHERE id = ?";
            statement = connection.prepareStatement(query);
            statement.setInt(1, id);

            resultSet = statement.executeQuery();

            if (resultSet.next()) {
                user = extractUserFromResultSet(resultSet);
            }

        } catch (SQLException e) {
            System.err.println("Error finding user by ID: " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeResources(resultSet, statement, connection);
        }

        return user;
    }

    @Override
    public User findByEmail(String email) {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        User user = null;

        try {
            connection = dbConnection.getConnection();
            String query = "SELECT * FROM users WHERE email = ?";
            statement = connection.prepareStatement(query);
            statement.setString(1, email);

            resultSet = statement.executeQuery();

            if (resultSet.next()) {
                user = extractUserFromResultSet(resultSet);
            }

        } catch (SQLException e) {
            System.err.println("Error finding user by email: " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeResources(resultSet, statement, connection);
        }

        return user;
    }

    @Override
    public User authenticate(String email, String hashedPassword) {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        User user = null;

        try {
            connection = dbConnection.getConnection();
            String query = "SELECT * FROM users WHERE email = ? AND password = ?";
            statement = connection.prepareStatement(query);
            statement.setString(1, email);
            statement.setString(2, hashedPassword);

            resultSet = statement.executeQuery();

            if (resultSet.next()) {
                user = extractUserFromResultSet(resultSet);
            }

        } catch (SQLException e) {
            System.err.println("Error authenticating user: " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeResources(resultSet, statement, connection);
        }

        return user;
    }

    /**
     * Helper method to extract a User object from a ResultSet
     */
    private User extractUserFromResultSet(ResultSet resultSet) throws SQLException {
        User user = new User();
        user.setId(resultSet.getInt("id"));
        user.setFullName(resultSet.getString("full_name"));
        user.setEmail(resultSet.getString("email"));
        user.setPassword(resultSet.getString("password"));
        return user;
    }

    /**
     * Helper method to close database resources
     */
    private void closeResources(ResultSet resultSet, Statement statement, Connection connection) {
        try {
            if (resultSet != null) {
                resultSet.close();
            }
            if (statement != null) {
                statement.close();
            }
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            System.err.println("Error closing resources: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
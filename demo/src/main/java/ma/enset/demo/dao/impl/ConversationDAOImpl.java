package ma.enset.demo.dao.impl;

import ma.enset.demo.dao.ConversationDao;
import ma.enset.demo.entities.Conversation;
import ma.enset.demo.utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ConversationDAOImpl implements ConversationDao {
    private final DBConnection dbConnection;

    public ConversationDAOImpl() {
        this.dbConnection = new DBConnection();
    }

    @Override
    public boolean create(Conversation conversation) {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet generatedKeys = null;
        boolean success = false;

        try {
            connection = dbConnection.getConnection();
            String query = "INSERT INTO conversation (user_id, title) VALUES (?, ?)";
            statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            statement.setInt(1, conversation.getUserId());
            statement.setString(2, conversation.getTitle());

            int rowsAffected = statement.executeUpdate();
            if (rowsAffected > 0) {
                generatedKeys = statement.getGeneratedKeys();
                if (generatedKeys.next()) {
                    conversation.setId(generatedKeys.getInt(1));
                    success = true;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error creating conversation: " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeResources(generatedKeys, statement, connection);
        }

        return success;
    }

    @Override
    public Conversation findById(int id) {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        Conversation conversation = null;

        try {
            connection = dbConnection.getConnection();
            String query = "SELECT * FROM conversation WHERE id = ?";
            statement = connection.prepareStatement(query);
            statement.setInt(1, id);

            resultSet = statement.executeQuery();
            if (resultSet.next()) {
                conversation = extractConversationFromResultSet(resultSet);
            }
        } catch (SQLException e) {
            System.err.println("Error finding conversation by ID: " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeResources(resultSet, statement, connection);
        }

        return conversation;
    }

    @Override
    public List<Conversation> findByUserId(int userId) {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        List<Conversation> conversations = new ArrayList<>();

        try {
            connection = dbConnection.getConnection();
            String query = "SELECT * FROM conversation WHERE user_id = ? ORDER BY created_at DESC";
            statement = connection.prepareStatement(query);
            statement.setInt(1, userId);

            resultSet = statement.executeQuery();
            while (resultSet.next()) {
                conversations.add(extractConversationFromResultSet(resultSet));
            }
        } catch (SQLException e) {
            System.err.println("Error finding conversations by user ID: " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeResources(resultSet, statement, connection);
        }

        return conversations;
    }

    @Override
    public boolean updateTitle(Conversation conversation) {
        Connection connection = null;
        PreparedStatement statement = null;
        boolean success = false;

        try {
            connection = dbConnection.getConnection();
            String query = "UPDATE conversation SET title = ? WHERE id = ?";
            statement = connection.prepareStatement(query);
            statement.setString(1, conversation.getTitle());
            statement.setInt(2, conversation.getId());

            int rowsAffected = statement.executeUpdate();
            success = rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error updating conversation title: " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeResources(null, statement, connection);
        }

        return success;
    }

    private Conversation extractConversationFromResultSet(ResultSet resultSet) throws SQLException {
        Conversation conversation = new Conversation();
        conversation.setId(resultSet.getInt("id"));
        conversation.setUserId(resultSet.getInt("user_id"));
        conversation.setTitle(resultSet.getString("title"));

        // Convert SQL timestamp to LocalDateTime
        Timestamp timestamp = resultSet.getTimestamp("created_at");
        if (timestamp != null) {
            conversation.setCreatedAt(timestamp.toLocalDateTime());
        }

        return conversation;
    }

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

    @Override
    public boolean delete(int id) {
        try {
            Connection conn = dbConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement("DELETE FROM conversation WHERE id = ?");
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

}
package ma.enset.demo.dao.impl;

import ma.enset.demo.dao.MessageDao;
import ma.enset.demo.entities.Message;
import ma.enset.demo.utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MessageDAOImpl implements MessageDao {
    private final DBConnection dbConnection;

    public MessageDAOImpl() {
        this.dbConnection = new DBConnection();
    }

    @Override
    public boolean create(Message message) {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet generatedKeys = null;
        boolean success = false;

        try {
            connection = dbConnection.getConnection();
            String query = "INSERT INTO message (conversation_id, sender, content) VALUES (?, ?, ?)";
            statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            statement.setInt(1, message.getConversationId());
            statement.setString(2, message.getSender().name().toLowerCase());
            statement.setString(3, message.getContent());

            int rowsAffected = statement.executeUpdate();
            if (rowsAffected > 0) {
                generatedKeys = statement.getGeneratedKeys();
                if (generatedKeys.next()) {
                    message.setId(generatedKeys.getInt(1));
                    success = true;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error creating message: " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeResources(generatedKeys, statement, connection);
        }

        return success;
    }

    @Override
    public List<Message> findByConversationId(int conversationId) {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        List<Message> messages = new ArrayList<>();

        try {
            connection = dbConnection.getConnection();
            String query = "SELECT * FROM message WHERE conversation_id = ? ORDER BY timestamp ASC";
            statement = connection.prepareStatement(query);
            statement.setInt(1, conversationId);

            resultSet = statement.executeQuery();
            while (resultSet.next()) {
                messages.add(extractMessageFromResultSet(resultSet));
            }
        } catch (SQLException e) {
            System.err.println("Error finding messages by conversation ID: " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeResources(resultSet, statement, connection);
        }

        return messages;
    }

    private Message extractMessageFromResultSet(ResultSet resultSet) throws SQLException {
        Message message = new Message();
        message.setId(resultSet.getInt("id"));
        message.setConversationId(resultSet.getInt("conversation_id"));

        // Parse sender enum from string
        String senderStr = resultSet.getString("sender");
        message.setSender(Message.Sender.valueOf(senderStr.toUpperCase()));

        message.setContent(resultSet.getString("content"));

        // Convert SQL timestamp to LocalDateTime
        Timestamp timestamp = resultSet.getTimestamp("timestamp");
        if (timestamp != null) {
            message.setTimestamp(timestamp.toLocalDateTime());
        }

        // Handle possible NULL feedback
        if (resultSet.getObject("feedback") != null) {
            message.setFeedback(resultSet.getBoolean("feedback"));
        } else {
            message.setFeedback(null);
        }

        return message;
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
    public boolean updateFeedback(int messageId, Integer feedback) {
        String sql = "UPDATE message SET feedback = ? WHERE id = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            if (feedback == null) {
                stmt.setNull(1, java.sql.Types.TINYINT);
            } else {
                stmt.setInt(1, feedback);
            }
            stmt.setInt(2, messageId);

            int rows = stmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }


}
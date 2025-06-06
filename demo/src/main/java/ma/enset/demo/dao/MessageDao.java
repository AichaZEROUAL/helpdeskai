package ma.enset.demo.dao;

import ma.enset.demo.entities.Message;

import java.util.List;

public interface MessageDao {
    /**
     * Create a new message in the database
     * @param message The message to create
     * @return true if successful, false otherwise
     */
    boolean create(Message message);

    /**
     * Find all messages for a specific conversation
     * @param conversationId The conversation ID
     * @return List of messages in the conversation
     */
    List<Message> findByConversationId(int conversationId);
    public boolean updateFeedback(int messageId, Integer feedback);

}
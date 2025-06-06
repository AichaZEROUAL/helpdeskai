package ma.enset.demo.dao;

import ma.enset.demo.entities.Conversation;

import java.util.List;

public interface ConversationDao {
    /**
     * Create a new conversation in the database
     * @param conversation The conversation to create
     * @return true if successful, false otherwise
     */
    boolean create(Conversation conversation);

    /**
     * Find a conversation by its ID
     * @param id The conversation ID
     * @return The Conversation object if found, null otherwise
     */
    Conversation findById(int id);

    /**
     * Find all conversations for a specific user
     * @param userId The user ID
     * @return List of conversations belonging to the user
     */
    List<Conversation> findByUserId(int userId);

    /**
     * Update a conversation's title
     * @param conversation The conversation with updated title
     * @return true if successful, false otherwise
     */
    boolean updateTitle(Conversation conversation);
    public boolean delete(int id);

}
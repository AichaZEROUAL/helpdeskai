package ma.enset.demo.entities;

import java.time.LocalDateTime;

public class Message {
    public enum Sender {
        USER,
        CHATBOT
    }

    private int id;
    private int conversationId;
    private Sender sender;
    private String content;
    private LocalDateTime timestamp;
    private Boolean feedback; // null = no feedback, true = positive, false = negative

    // Default constructor
    public Message() {
    }

    // Constructor without id and feedback (for creating new messages)
    public Message(int conversationId, Sender sender, String content) {
        this.conversationId = conversationId;
        this.sender = sender;
        this.content = content;
        this.timestamp = LocalDateTime.now();
        this.feedback = null;
    }

    // Constructor with all fields
    public Message(int id, int conversationId, Sender sender, String content, LocalDateTime timestamp, Boolean feedback) {
        this.id = id;
        this.conversationId = conversationId;
        this.sender = sender;
        this.content = content;
        this.timestamp = timestamp;
        this.feedback = feedback;
    }

    // Getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getConversationId() {
        return conversationId;
    }

    public void setConversationId(int conversationId) {
        this.conversationId = conversationId;
    }

    public Sender getSender() {
        return sender;
    }

    public void setSender(Sender sender) {
        this.sender = sender;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public Boolean getFeedback() {
        return feedback;
    }

    public void setFeedback(Boolean feedback) {
        this.feedback = feedback;
    }

    @Override
    public String toString() {
        return "Message{" +
                "id=" + id +
                ", conversationId=" + conversationId +
                ", sender=" + sender +
                ", content='" + content + '\'' +
                ", timestamp=" + timestamp +
                ", feedback=" + feedback +
                '}';
    }
}
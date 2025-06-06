package ma.enset.demo.entities;

import java.time.LocalDateTime;

public class Conversation {
    private int id;
    private int userId;
    private String title;
    private LocalDateTime createdAt;

    // Default constructor
    public Conversation() {
    }

    // Constructor without id (for creating new conversations)
    public Conversation(int userId, String title) {
        this.userId = userId;
        this.title = title;
        this.createdAt = LocalDateTime.now();
    }

    // Constructor with all fields
    public Conversation(int id, int userId, String title, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.createdAt = createdAt;
    }

    // Getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return title;
    }
}
package com.example.privateping;

public class ChatMessage {
    private String message;
    private String user;
    private long timestamp;

    public ChatMessage() {
    }

    public ChatMessage(String message, String user, long timestamp) {
        this.message = message;
        this.user = user;
        this.timestamp = timestamp;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
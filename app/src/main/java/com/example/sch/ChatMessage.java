package com.example.sch;

public class ChatMessage {
    public enum MessageType {
        TEXT,
        IMAGE
    }

    private MessageType messageType;
    private String content;
    private long timestamp;
    private String sender;

    public ChatMessage(MessageType messageType, String content, long timestamp, String sender) {
        this.messageType = messageType;
        this.content = content;
        this.timestamp = timestamp;
        this.sender = sender;
    }
    public ChatMessage() {
        // Default constructor (empty)
    }
    public MessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }





    // Constructor, getters, and setters
}


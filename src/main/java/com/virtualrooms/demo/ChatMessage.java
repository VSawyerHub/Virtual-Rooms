package com.virtualrooms.demo;

import java.time.LocalDateTime;

public class ChatMessage {
    private String sender;
    private String content;
    private LocalDateTime sentAt;

    public ChatMessage() {
        this.sentAt = LocalDateTime.now();
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }
}
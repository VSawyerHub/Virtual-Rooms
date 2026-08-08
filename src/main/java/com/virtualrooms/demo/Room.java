package com.virtualrooms.demo;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
public class Room {

    @Id
    private String id;

    private LocalDateTime createdAt;

    private LocalDateTime lastActivityAt;

    private String tag;

    private String description;
    
    private String code;
    
    private String title;

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (lastActivityAt == null) {
            lastActivityAt = createdAt;
        }
        if (tag == null) {
            tag = "";
        }
        if (description == null) {
            description = "";
        }
        if (code == null) {
            code = generateCode();
        }
        if (title == null) {
            title = generateTitle();
        }
    }

    private String generateCode() {
        String chars = "ABCDEFGHJKMNPQRSTUVWXYZ23456789"; // sem 0/O e 1/I/L, pra não confundir
        StringBuilder sb = new StringBuilder();
        java.util.Random random = new java.util.Random();
        for (int i = 0; i < 6; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    private String generateTitle() {
        return "Sala " + id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getLastActivityAt() {
        return lastActivityAt;
    }

    public void setLastActivityAt(LocalDateTime lastActivityAt) {
        this.lastActivityAt = lastActivityAt;
    }

    public void touchActivity() {
        this.lastActivityAt = LocalDateTime.now();
    }

    public String getTag() {return tag;}

    public void setTag(String tag) {this.tag = tag;}

    public String getDescription() {return description;}

    public void setDescription(String description) {this.description = description;}

    public String getCode() {return code;}

    public void setCode(String code) {this.code = code;}

    public String getTitle() {return title;}

    public void setTitle(String title) {this.title = title;}
}


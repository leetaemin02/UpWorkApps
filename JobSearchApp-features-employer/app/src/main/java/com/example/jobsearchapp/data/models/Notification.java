package com.example.jobsearchapp.data.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;
import java.io.Serializable;

@Entity(tableName = "notifications")
public class Notification implements Serializable {
    @PrimaryKey
    @NonNull
    private String notificationId;
    private String userId;
    private String title;
    private String message;
    private String type; // "application_status"/"new_job"/"system"
    private boolean isRead;
    private long createdAt;

    public Notification() {
        this.notificationId = java.util.UUID.randomUUID().toString();
        this.createdAt = System.currentTimeMillis();
        this.isRead = false;
    }

    // Getters and Setters
    @NonNull
    public String getNotificationId() { return notificationId; }
    public void setNotificationId(@NonNull String notificationId) { this.notificationId = notificationId; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}

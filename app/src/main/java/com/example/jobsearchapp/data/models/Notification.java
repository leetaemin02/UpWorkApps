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
    private String recipientId;
    private String title;
    private String message;
    private String type; // "application_status"/"new_job"/"system"
    private boolean isRead;
    private long timestamp;
    private String jobId;
    private String applicationId;

    public Notification() {
        this.notificationId = java.util.UUID.randomUUID().toString();
        this.timestamp = System.currentTimeMillis();
        this.isRead = false;
    }

    public Notification(String recipientId, String title, String message, String type, String jobId, String applicationId) {
        this();
        this.recipientId = recipientId;
        this.title = title;
        this.message = message;
        this.type = type;
        this.jobId = jobId;
        this.applicationId = applicationId;
    }

    // Getters and Setters
    @NonNull
    public String getNotificationId() { return notificationId; }
    public void setNotificationId(@NonNull String notificationId) { this.notificationId = notificationId; }
    public String getRecipientId() { return recipientId; }
    public void setRecipientId(String recipientId) { this.recipientId = recipientId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public String getJobId() { return jobId; }
    public void setJobId(String jobId) { this.jobId = jobId; }

    public String getApplicationId() { return applicationId; }
    public void setApplicationId(String applicationId) { this.applicationId = applicationId; }
}

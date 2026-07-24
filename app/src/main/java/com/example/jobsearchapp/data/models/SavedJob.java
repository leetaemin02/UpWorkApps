package com.example.jobsearchapp.data.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import java.io.Serializable;

@Entity(tableName = "savedJobs")
public class SavedJob implements Serializable {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String userId;
    private String jobId;
    private long savedAt;

    public SavedJob() {
        this.savedAt = System.currentTimeMillis();
    }

    public SavedJob(String userId, String jobId) {
        this();
        this.userId = userId;
        this.jobId = jobId;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getJobId() { return jobId; }
    public void setJobId(String jobId) { this.jobId = jobId; }
    public long getSavedAt() { return savedAt; }
    public void setSavedAt(long savedAt) { this.savedAt = savedAt; }
}

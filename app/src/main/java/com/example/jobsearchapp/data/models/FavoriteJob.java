package com.example.jobsearchapp.data.models;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(tableName = "favorite_jobs",
        foreignKeys = {
            @ForeignKey(entity = Job.class, parentColumns = "id", childColumns = "jobId", onDelete = ForeignKey.CASCADE),
            @ForeignKey(entity = User.class, parentColumns = "id", childColumns = "userId", onDelete = ForeignKey.CASCADE)
        })
public class FavoriteJob {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String userId;
    private String jobId;

    public FavoriteJob(String userId, String jobId) {
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
}
package com.example.jobsearchapp.data.models;

import java.io.Serializable;

public class JobMatch implements Serializable {
    private String jobId;
    private int score;
    private String reason;
    private long calculatedAt;

    // Denormalization for UI
    private Job job; 

    public JobMatch() {}

    public String getJobId() { return jobId; }
    public void setJobId(String jobId) { this.jobId = jobId; }
    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public long getCalculatedAt() { return calculatedAt; }
    public void setCalculatedAt(long calculatedAt) { this.calculatedAt = calculatedAt; }
    public Job getJob() { return job; }
    public void setJob(Job job) { this.job = job; }
}

package com.example.jobsearchapp.data.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;
import java.io.Serializable;

@Entity(tableName = "applications")
public class Application implements Serializable {
    @PrimaryKey
    @NonNull
    private String id; // applicationId
    private String jobId;
    private String candidateId;
    private String companyId;
    private String cvUrl;
    private String coverLetter;
    private long appliedAt;
    private String status; // "pending"/"reviewed"/"accepted"/"rejected"
    
    // Denormalization
    private String jobTitle;
    private String candidateName;

    public Application() {
        this.id = java.util.UUID.randomUUID().toString();
        this.appliedAt = System.currentTimeMillis();
        this.status = "pending";
    }

    public Application(String jobId, String candidateId, String companyId) {
        this();
        this.jobId = jobId;
        this.candidateId = candidateId;
        this.companyId = companyId;
    }

    // Getters and Setters
    @NonNull
    public String getId() { return id; }
    public void setId(@NonNull String id) { this.id = id; }
    public String getJobId() { return jobId; }
    public void setJobId(String jobId) { this.jobId = jobId; }
    public String getCandidateId() { return candidateId; }
    public void setCandidateId(String candidateId) { this.candidateId = candidateId; }
    public String getCompanyId() { return companyId; }
    public void setCompanyId(String companyId) { this.companyId = companyId; }
    public String getCvUrl() { return cvUrl; }
    public void setCvUrl(String cvUrl) { this.cvUrl = cvUrl; }
    public String getCoverLetter() { return coverLetter; }
    public void setCoverLetter(String coverLetter) { this.coverLetter = coverLetter; }
    public long getAppliedAt() { return appliedAt; }
    public void setAppliedAt(long appliedAt) { this.appliedAt = appliedAt; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getJobTitle() { return jobTitle; }
    public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }
    public String getCandidateName() { return candidateName; }
    public void setCandidateName(String candidateName) { this.candidateName = candidateName; }
}

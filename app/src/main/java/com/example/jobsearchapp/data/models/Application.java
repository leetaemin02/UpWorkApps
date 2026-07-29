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
    private String employerId;
    private String cvUrl;
    private String coverLetter;
    private long appliedAt;
    private String status; // "pending"/"reviewed"/"accepted"/"rejected"
    
    // Denormalization
    private String jobTitle;
    private String candidateName;
    private String companyName;
    private String location;
    private String candidateAvatarUrl;
    
    // AI Score and Reason
    private Integer aiScore;
    private String aiReason;

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
        this.employerId = companyId; // Mặc định dùng companyId nếu không truyền employerId
    }

    public Application(String jobId, String candidateId, String companyId, String employerId) {
        this();
        this.jobId = jobId;
        this.candidateId = candidateId;
        this.companyId = companyId;
        this.employerId = employerId;
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
    public String getEmployerId() { return employerId; }
    public void setEmployerId(String employerId) { this.employerId = employerId; }
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
    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getCandidateAvatarUrl() { return candidateAvatarUrl; }
    public void setCandidateAvatarUrl(String candidateAvatarUrl) { this.candidateAvatarUrl = candidateAvatarUrl; }

    public Integer getAiScore() { return aiScore; }
    public void setAiScore(Integer aiScore) { this.aiScore = aiScore; }

    public String getAiReason() { return aiReason; }
    public void setAiReason(String aiReason) { this.aiReason = aiReason; }
}

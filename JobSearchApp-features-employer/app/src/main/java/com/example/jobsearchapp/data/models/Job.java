package com.example.jobsearchapp.data.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;
import java.io.Serializable;

@Entity(tableName = "jobs")
public class Job implements Serializable {
    @PrimaryKey
    @NonNull
    private String id; // jobId
    private String companyId;
    private String employerId; // Thêm trường này để xác định chủ sở hữu bài đăng
    private String title;
    private String description;
    private String requirements;
    private String benefits;
    private long salaryMin;
    private long salaryMax;
    private String location;
    private String jobType; // "Full-time"/"Part-time"/"Remote"/"Internship"
    private String category;
    private String experienceRequired;
    private String deadline;
    private long postedAt;
    private String status; // "active"/"closed"/"pending"
    private int views;
    private int quantity;
    
    // Denormalization fields for faster display
    private String companyName;
    private String logoUrl;

    public Job() {
        this.id = java.util.UUID.randomUUID().toString();
        this.postedAt = System.currentTimeMillis();
        this.status = "active";
        this.views = 0;
    }

    // Getters and Setters
    @NonNull
    public String getId() { return id; }
    public void setId(@NonNull String id) { this.id = id; }
    public String getCompanyId() { return companyId; }
    public void setCompanyId(String companyId) { this.companyId = companyId; }
    public String getEmployerId() { return employerId; }
    public void setEmployerId(String employerId) { this.employerId = employerId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getRequirements() { return requirements; }
    public void setRequirements(String requirements) { this.requirements = requirements; }
    public String getBenefits() { return benefits; }
    public void setBenefits(String benefits) { this.benefits = benefits; }
    public long getSalaryMin() { return salaryMin; }
    public void setSalaryMin(long salaryMin) { this.salaryMin = salaryMin; }
    public long getSalaryMax() { return salaryMax; }
    public void setSalaryMax(long salaryMax) { this.salaryMax = salaryMax; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getJobType() { return jobType; }
    public void setJobType(String jobType) { this.jobType = jobType; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getExperienceRequired() { return experienceRequired; }
    public void setExperienceRequired(String experienceRequired) { this.experienceRequired = experienceRequired; }
    public String getDeadline() { return deadline; }
    public void setDeadline(String deadline) { this.deadline = deadline; }
    public long getPostedAt() { return postedAt; }
    public void setPostedAt(long postedAt) { this.postedAt = postedAt; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public int getViews() { return views; }
    public void setViews(int views) { this.views = views; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }
    public String getLogoUrl() { return logoUrl; }
    public void setLogoUrl(String logoUrl) { this.logoUrl = logoUrl; }
}

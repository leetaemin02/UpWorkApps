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
    private long deadline; // Giữ kiểu long cho Room
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
    public String getEmployerId() { 
        return (employerId != null && !employerId.isEmpty()) ? employerId : companyId; 
    }
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
    
    public long getDeadline() { return deadline; }

    // Setter chuẩn cho Room và code Java
    public void setDeadline(long deadline) { this.deadline = deadline; }
    
    // Setter linh hoạt cho Firestore (handles String, Long, Timestamp)
    // Đổi tên để tránh xung đột "multiple setter overloads" trong Firebase
    public void setDeadlineFromObject(Object deadlineObj) {
        if (deadlineObj == null) return;
        if (deadlineObj instanceof Long) {
            this.deadline = (Long) deadlineObj;
        } else if (deadlineObj instanceof com.google.firebase.Timestamp) {
            this.deadline = ((com.google.firebase.Timestamp) deadlineObj).toDate().getTime();
        } else if (deadlineObj instanceof String) {
            try {
                // Thử parse các định dạng ngày phổ biến
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault());
                this.deadline = sdf.parse((String) deadlineObj).getTime();
            } catch (Exception e) {
                try {
                    java.text.SimpleDateFormat sdf2 = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
                    this.deadline = sdf2.parse((String) deadlineObj).getTime();
                } catch (Exception e2) {
                    this.deadline = 0;
                }
            }
        }
    }
    public long getPostedAt() { return postedAt; }
    
    public void setPostedAt(long postedAt) { this.postedAt = postedAt; }
    
    // Phương thức xử lý linh hoạt cho Firebase
    public void setPostedAtFromObject(Object postedAt) {
        if (postedAt == null) return;
        if (postedAt instanceof Long) {
            this.postedAt = (Long) postedAt;
        } else if (postedAt instanceof com.google.firebase.Timestamp) {
            this.postedAt = ((com.google.firebase.Timestamp) postedAt).toDate().getTime();
        } else if (postedAt instanceof String) {
            try {
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault());
                this.postedAt = sdf.parse((String) postedAt).getTime();
            } catch (Exception e) {
                this.postedAt = System.currentTimeMillis();
            }
        } else if (postedAt instanceof Double) {
            this.postedAt = ((Double) postedAt).longValue();
        }
    }
    
    public void setCreatedAt(long createdAt) { this.postedAt = createdAt; }
    public long getCreatedAt() { return postedAt; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public int getViews() { return views; }
    public void setViews(int views) { this.views = views; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public String getCompanyName() { 
        return (companyName != null) ? companyName : "Công ty ẩn danh"; 
    }
    public void setCompanyName(String companyName) { this.companyName = companyName; }
    public String getLogoUrl() { return logoUrl; }
    public void setLogoUrl(String logoUrl) { this.logoUrl = logoUrl; }
}

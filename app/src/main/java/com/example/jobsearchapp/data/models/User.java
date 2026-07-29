package com.example.jobsearchapp.data.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;
import java.io.Serializable;

@Entity(tableName = "users")
public class User implements Serializable {
    @PrimaryKey
    @NonNull
    private String id; // userId
    private String email;
    private String fullName;
    private String phone;
    private String avatarUrl;
    private String role; // "candidate" / "employer" / "admin"
    private long createdAt;
    private String status; // "active" / "banned"
    private String companyName;
    private String profession;
    private String location;
    private String cvPath;
    private String skills;
    private String password;
    private String companyWebsite;
    private String companyDescription;

    public User() {
        this.id = java.util.UUID.randomUUID().toString();
        this.createdAt = System.currentTimeMillis();
        this.status = "active";
    }

    public User(String id, String email, String fullName, String role) {
        this.id = id;
        this.email = email;
        this.fullName = fullName;
        this.role = role;
        this.createdAt = System.currentTimeMillis();
        this.status = "active";
    }

    // Getters and Setters
    @NonNull
    public String getId() { return id; }
    public void setId(@NonNull String id) { this.id = id; }
    
    // Alias cho Firestore
    public void setUserId(String userId) { this.id = userId; }
    public String getUserId() { return id; }
    public void setUid(String uid) { this.id = uid; }
    public String getUid() { return id; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public long getCreatedAt() { return createdAt; }
    
    // Room needs this exact match
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    
    // Firestore flexible mapping helper
    public void setCreatedAtFromObject(Object createdAt) {
        if (createdAt instanceof com.google.firebase.Timestamp) {
            this.createdAt = ((com.google.firebase.Timestamp) createdAt).toDate().getTime();
        } else if (createdAt instanceof Long) {
            this.createdAt = (Long) createdAt;
        } else if (createdAt instanceof Double) {
            this.createdAt = ((Double) createdAt).longValue();
        }
    }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }
    public String getProfession() { return profession; }
    public void setProfession(String profession) { this.profession = profession; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getCvPath() { return cvPath; }
    public void setCvPath(String cvPath) { this.cvPath = cvPath; }
    public String getSkills() { return skills; }
    public void setSkills(String skills) { this.skills = skills; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getCompanyWebsite() { return companyWebsite; }
    public void setCompanyWebsite(String companyWebsite) { this.companyWebsite = companyWebsite; }
    public String getCompanyDescription() { return companyDescription; }
    public void setCompanyDescription(String companyDescription) { this.companyDescription = companyDescription; }
}

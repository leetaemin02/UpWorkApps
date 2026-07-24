package com.example.jobsearchapp.data.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;
import java.io.Serializable;

@Entity(tableName = "candidates")
public class Candidate implements Serializable {
    @PrimaryKey
    @NonNull
    private String userId;
    private long dateOfBirth;
    private String gender;
    private String address;
    private String education;
    private String experience;
    private String skills; // Stored as comma separated string for Room
    private String cvUrl;
    private String desiredPosition;
    private long desiredSalary;

    public Candidate() {}

    public Candidate(@NonNull String userId) {
        this.userId = userId;
    }

    // Getters and Setters
    @NonNull
    public String getUserId() { return userId; }
    public void setUserId(@NonNull String userId) { this.userId = userId; }
    public long getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(long dateOfBirth) { this.dateOfBirth = dateOfBirth; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getEducation() { return education; }
    public void setEducation(String education) { this.education = education; }
    public String getExperience() { return experience; }
    public void setExperience(String experience) { this.experience = experience; }
    public String getSkills() { return skills; }
    public void setSkills(String skills) { this.skills = skills; }
    public String getCvUrl() { return cvUrl; }
    public void setCvUrl(String cvUrl) { this.cvUrl = cvUrl; }
    public String getDesiredPosition() { return desiredPosition; }
    public void setDesiredPosition(String desiredPosition) { this.desiredPosition = desiredPosition; }
    public long getDesiredSalary() { return desiredSalary; }
    public void setDesiredSalary(long desiredSalary) { this.desiredSalary = desiredSalary; }
}

package com.example.jobsearchapp.data.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;
import java.io.Serializable;

@Entity(tableName = "reviews")
public class Review implements Serializable {
    @PrimaryKey
    @NonNull
    private String reviewId;
    private String companyId;
    private String userId;
    private float rating;
    private String comment;
    private long createdAt;

    public Review() {
        this.reviewId = java.util.UUID.randomUUID().toString();
        this.createdAt = System.currentTimeMillis();
    }

    // Getters and Setters
    @NonNull
    public String getReviewId() { return reviewId; }
    public void setReviewId(@NonNull String reviewId) { this.reviewId = reviewId; }
    public String getCompanyId() { return companyId; }
    public void setCompanyId(String companyId) { this.companyId = companyId; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public float getRating() { return rating; }
    public void setRating(float rating) { this.rating = rating; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}

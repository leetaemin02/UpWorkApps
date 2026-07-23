package com.example.jobsearchapp.data.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;
import java.io.Serializable;

@Entity(tableName = "categories")
public class Category implements Serializable {
    @PrimaryKey
    @NonNull
    private String categoryId;
    private String categoryName;
    private int iconRes;

    public Category() {
        this.categoryId = java.util.UUID.randomUUID().toString();
    }

    public Category(String name, int iconRes) {
        this();
        this.categoryName = name;
        this.iconRes = iconRes;
    }

    // Getters and Setters
    @NonNull
    public String getCategoryId() { return categoryId; }
    public void setCategoryId(@NonNull String categoryId) { this.categoryId = categoryId; }
    
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    
    public String getName() { return categoryName; } // Alias for adapter
    
    public int getIconRes() { return iconRes; }
    public void setIconRes(int iconRes) { this.iconRes = iconRes; }
}

package com.example.jobsearchapp.data.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;
import java.io.Serializable;

@Entity(tableName = "companies")
public class Company implements Serializable {
    @PrimaryKey
    @NonNull
    private String companyId;
    private String userId;
    private String companyName;
    private String logoUrl;
    private String description;
    private String address;
    private String website;
    private String taxCode;
    private String industry;
    private String companySize;
    private boolean verified;

    public Company() {
        this.companyId = java.util.UUID.randomUUID().toString();
        this.verified = false;
    }

    public Company(@NonNull String userId, String companyName) {
        this();
        this.userId = userId;
        this.companyName = companyName;
    }

    // Getters and Setters
    @NonNull
    public String getCompanyId() { return companyId; }
    public void setCompanyId(@NonNull String companyId) { this.companyId = companyId; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }
    public String getLogoUrl() { return logoUrl; }
    public void setLogoUrl(String logoUrl) { this.logoUrl = logoUrl; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }
    public String getTaxCode() { return taxCode; }
    public void setTaxCode(String taxCode) { this.taxCode = taxCode; }
    public String getIndustry() { return industry; }
    public void setIndustry(String industry) { this.industry = industry; }
    public String getCompanySize() { return companySize; }
    public void setCompanySize(String companySize) { this.companySize = companySize; }
    public boolean isVerified() { return verified; }
    public void setVerified(boolean verified) { this.verified = verified; }
}

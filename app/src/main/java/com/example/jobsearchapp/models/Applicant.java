package com.example.jobsearchapp.models;

import java.io.Serializable;

public class Applicant implements Serializable {
    private String id;
    private String name;
    private String email;
    private String phone;
    private String cvUrl;

    public Applicant() {}

    public Applicant(String id, String name, String email, String phone, String cvUrl) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.cvUrl = cvUrl;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getCvUrl() { return cvUrl; }
    public void setCvUrl(String cvUrl) { this.cvUrl = cvUrl; }
}
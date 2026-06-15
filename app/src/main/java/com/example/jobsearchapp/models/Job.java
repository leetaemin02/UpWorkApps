package com.example.jobsearchapp.models;

import java.io.Serializable;

public class Job implements Serializable {
    private String id;
    private String title;
    private String salary;
    private String location;
    private String description;

    public Job() {}

    public Job(String id, String title, String salary, String location, String description) {
        this.id = id;
        this.title = title;
        this.salary = salary;
        this.location = location;
        this.description = description;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getSalary() { return salary; }
    public void setSalary(String salary) { this.salary = salary; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
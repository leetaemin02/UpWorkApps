package com.example.jobsearchapp.data.models;

import androidx.room.Embedded;
import androidx.room.Relation;

public class ApplicationWithJob {
    @Embedded
    public Application application;

    @Relation(
        parentColumn = "jobId",
        entityColumn = "id"
    )
    public Job job;
}
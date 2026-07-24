package com.example.jobsearchapp.data.local;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import com.example.jobsearchapp.data.models.Job;
import java.util.List;

@Dao
public interface JobDao {
    @Insert
    void insertJob(Job job);

    @Update
    void updateJob(Job job);

    @Delete
    void deleteJob(Job job);

    @Query("SELECT * FROM jobs ORDER BY postedAt DESC")
    List<Job> getAllJobs();

    @Query("SELECT * FROM jobs WHERE title LIKE '%' || :query || '%' OR companyName LIKE '%' || :query || '%'")
    List<Job> searchJobs(String query);

    @Query("SELECT * FROM jobs WHERE companyId = :companyId")
    List<Job> getJobsByCompany(String companyId);

    @Query("SELECT * FROM jobs WHERE id = :jobId LIMIT 1")
    Job getJobById(String jobId);
}

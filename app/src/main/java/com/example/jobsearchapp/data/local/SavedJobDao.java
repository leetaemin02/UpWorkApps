package com.example.jobsearchapp.data.local;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import com.example.jobsearchapp.data.models.SavedJob;
import com.example.jobsearchapp.data.models.Job;
import java.util.List;

@Dao
public interface SavedJobDao {
    @Insert
    void addToSaved(SavedJob savedJob);

    @Delete
    void removeFromSaved(SavedJob savedJob);

    @Query("SELECT jobs.* FROM jobs INNER JOIN savedJobs ON jobs.id = savedJobs.jobId WHERE savedJobs.userId = :userId")
    List<Job> getSavedJobs(String userId);

    @Query("SELECT * FROM savedJobs WHERE userId = :userId AND jobId = :jobId LIMIT 1")
    SavedJob getSaved(String userId, String jobId);
}

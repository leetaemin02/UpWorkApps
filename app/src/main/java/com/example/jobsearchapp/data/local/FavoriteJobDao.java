package com.example.jobsearchapp.data.local;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import com.example.jobsearchapp.data.models.FavoriteJob;
import com.example.jobsearchapp.data.models.Job;
import java.util.List;

@Dao
public interface FavoriteJobDao {
    @Insert
    void addToFavorite(FavoriteJob favoriteJob);

    @Delete
    void removeFromFavorite(FavoriteJob favoriteJob);

    @Query("SELECT jobs.* FROM jobs INNER JOIN favorite_jobs ON jobs.id = favorite_jobs.jobId WHERE favorite_jobs.userId = :userId")
    List<Job> getFavoriteJobs(String userId);

    @Query("SELECT * FROM favorite_jobs WHERE userId = :userId AND jobId = :jobId LIMIT 1")
    FavoriteJob getFavorite(String userId, String jobId);
}
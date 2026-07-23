package com.example.jobsearchapp.data.local;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import com.example.jobsearchapp.data.models.Candidate;

@Dao
public interface CandidateDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertCandidate(Candidate candidate);

    @Update
    void updateCandidate(Candidate candidate);

    @Query("SELECT * FROM candidates WHERE userId = :userId LIMIT 1")
    Candidate getCandidateByUserId(String userId);
}

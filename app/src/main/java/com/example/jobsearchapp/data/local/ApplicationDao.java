package com.example.jobsearchapp.data.local;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;
import com.example.jobsearchapp.data.models.Application;
import com.example.jobsearchapp.data.models.ApplicationWithJob;
import java.util.List;

@Dao
public interface ApplicationDao {
    @Insert
    void applyJob(Application application);

    @Update
    void updateStatus(Application application);

    @Transaction
    @Query("SELECT * FROM applications WHERE candidateId = :candidateId ORDER BY appliedAt DESC")
    List<ApplicationWithJob> getMyApplicationsWithJob(String candidateId);

    @Transaction
    @Query("SELECT * FROM applications WHERE candidateId = :candidateId AND status = :status ORDER BY appliedAt DESC")
    List<ApplicationWithJob> getMyApplicationsByStatus(String candidateId, String status);

    @Query("SELECT * FROM applications WHERE jobId = :jobId ORDER BY appliedAt DESC")
    List<Application> getApplicantsForJob(String jobId);

    @Query("SELECT * FROM applications WHERE companyId = :companyId")
    List<Application> getApplicationsByCompany(String companyId);

    @Query("UPDATE applications SET status = :status WHERE id = :applicationId")
    void updateStatusById(String applicationId, String status);
}

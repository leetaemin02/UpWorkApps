package com.example.jobsearchapp.data.local;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import com.example.jobsearchapp.data.models.Company;

@Dao
public interface CompanyDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertCompany(Company company);

    @Update
    void updateCompany(Company company);

    @Query("SELECT * FROM companies WHERE companyId = :companyId LIMIT 1")
    Company getCompanyById(String companyId);

    @Query("SELECT * FROM companies WHERE userId = :userId LIMIT 1")
    Company getCompanyByUserId(String userId);
}

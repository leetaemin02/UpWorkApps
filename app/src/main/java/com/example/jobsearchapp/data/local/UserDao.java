package com.example.jobsearchapp.data.local;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import com.example.jobsearchapp.data.models.User;

@Dao
public interface UserDao {
    @Insert
    void register(User user);

    @Query("SELECT * FROM users WHERE email = :email AND password = :password LIMIT 1")
    User login(String email, String password);

    @Update
    void updateProfile(User user);

    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    User getUserById(String userId);
}
package com.example.geolearn.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.geolearn.database.entities.User;

import java.util.List;

@Dao
public interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertUser(User user);

    @Query("SELECT * FROM users WHERE uid = :userId LIMIT 1")
    User getUserById(String userId);

    @Query("SELECT * FROM users")
    List<User> getAllUsers();
}

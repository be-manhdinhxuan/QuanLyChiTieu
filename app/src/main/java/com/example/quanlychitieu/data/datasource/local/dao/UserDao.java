package com.example.quanlychitieu.data.datasource.local.dao;

import androidx.room.*;
import com.example.quanlychitieu.data.datasource.local.entity.UserEntity;

import java.util.List;

@Dao
public interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertUser(UserEntity user);

    @Query("SELECT * FROM users")
    List<UserEntity> getAllUsers();

    @Query("SELECT * FROM users WHERE id = :userId")
    UserEntity getUserById(String userId);

    @Query("SELECT * FROM users WHERE isCurrentUser = 1 LIMIT 1")
    UserEntity getCurrentUser();

    @Update
    void updateUser(UserEntity user);

    @Delete
    void deleteUser(UserEntity user);
}

package com.example.quanlychitieu.data.datasource.local.impl;

import android.util.Log;
import com.example.quanlychitieu.data.datasource.local.dao.UserDao;
import com.example.quanlychitieu.data.datasource.local.datasource.LocalUserDataSource;
import com.example.quanlychitieu.data.datasource.local.entity.UserEntity;
import com.example.quanlychitieu.data.mapper.UserMapper;
import com.example.quanlychitieu.domain.model.user.User;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

@Singleton
public class LocalUserDataSourceImpl implements LocalUserDataSource {
    private static final String TAG = "LocalUserDataSourceImpl";
    private final UserDao userDao;
    private final UserMapper mapper;

    @Inject
    public LocalUserDataSourceImpl(UserDao userDao, UserMapper mapper) {
        this.userDao = userDao;
        this.mapper = mapper;
    }

    @Override
    public User getUser(String userId) {
        UserEntity entity = userDao.getUserById(userId);
        if (entity == null) {
            return null;
        }
        return mapper.toDomain(entity);
    }

    @Override
    public User getCurrentUser() {
        UserEntity entity = userDao.getCurrentUser();
        if (entity == null) {
            return null;
        }
        return mapper.toDomain(entity);
    }

    @Override
    public long saveUser(User user) {
        if (user == null || user.getId() == null || user.getId().isEmpty()) {
            Log.e(TAG, "Cannot save user: user or user ID is null/empty");
            return -1;
        }

        try {
            UserEntity entity = mapper.toEntity(user, false);
            return userDao.insertUser(entity);
        } catch (Exception e) {
            Log.e(TAG, "Error saving user: " + e.getMessage());
            return -1;
        }
    }

    @Override
    public void updateUser(User user) {
        if (user == null || user.getId() == null || user.getId().isEmpty()) {
            Log.e(TAG, "Cannot update user: user or user ID is null/empty");
            return;
        }

        try {
            UserEntity entity = mapper.toEntity(user, false);
            userDao.updateUser(entity);
        } catch (Exception e) {
            Log.e(TAG, "Error updating user: " + e.getMessage());
        }
    }

    @Override
    public void deleteUser(String userId) {
        if (userId == null || userId.isEmpty()) {
            Log.e(TAG, "Cannot delete user: userId is null/empty");
            return;
        }

        UserEntity user = userDao.getUserById(userId);
        if (user != null) {
            userDao.deleteUser(user);
        }
    }

    @Override
    public void setCurrentUser(String userId) {
        if (userId == null || userId.isEmpty()) {
            Log.e(TAG, "Cannot set current user: userId is null/empty");
            return;
        }

        List<UserEntity> allUsers = userDao.getAllUsers();
        for (UserEntity user : allUsers) {
            if (user.isCurrentUser()) {
                user.setCurrentUser(false);
                userDao.updateUser(user);
            }
        }

        UserEntity user = userDao.getUserById(userId);
        if (user != null) {
            user.setCurrentUser(true);
            userDao.updateUser(user);
        }
    }

    @Override
    public void updateUserMoney(String userId, int amount) {
        if (userId == null || userId.isEmpty()) {
            Log.e(TAG, "Cannot update user money: userId is null/empty");
            return;
        }

        UserEntity user = userDao.getUserById(userId);
        if (user != null) {
            user.setMoney(amount);
            userDao.updateUser(user);
        }
    }
}
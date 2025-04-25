package com.example.quanlychitieu.data.datasource.local.impl;

import android.annotation.SuppressLint;

import com.example.quanlychitieu.data.datasource.local.dao.UserDao;
import com.example.quanlychitieu.data.datasource.local.datasource.LocalUserDataSource;
import com.example.quanlychitieu.data.datasource.local.entity.UserEntity;
import com.example.quanlychitieu.data.mapper.UserMapper;
import com.example.quanlychitieu.domain.model.user.User;

import java.util.List;

/**
 * Implementation của LocalUserDataSource sử dụng UserDao
 */
public class LocalUserDataSourceImpl implements LocalUserDataSource {
  private final UserDao userDao;
  private final UserMapper mapper;

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
    UserEntity entity = mapper.toEntity(user, false);
    if (userDao.getUserById(user.getId()) != null) {
      userDao.updateUser(entity);
      return Long.parseLong(user.getId());
    } else {
      return userDao.insertUser(entity);
    }
  }

  @Override
  public void updateUser(User user) {
    UserEntity entity = mapper.toEntity(user, false);
    userDao.updateUser(entity);
  }

  @Override
  public void deleteUser(String userId) {
    UserEntity user = userDao.getUserById(userId);
    if (user != null) {
      userDao.deleteUser(user);
    }
  }

  @Override
  public void setCurrentUser(String userId) {
    // Đặt tất cả các user hiện tại về false
    List<UserEntity> allUsers = userDao.getAllUsers();
    for (UserEntity user : allUsers) {
      if (user.isCurrentUser()) {
        user.setCurrentUser(false);
        userDao.updateUser(user);
      }
    }

    // Đặt user được chỉ định thành currentUser
    UserEntity user = userDao.getUserById(userId);
    if (user != null) {
      user.setCurrentUser(true);
      userDao.updateUser(user);
    }
  }

  @Override
  public void updateUserMoney(String userId, int amount) {
    UserEntity user = userDao.getUserById(userId);
    if (user != null) {
      user.setMoney(amount);
      userDao.updateUser(user);
    }
  }
}

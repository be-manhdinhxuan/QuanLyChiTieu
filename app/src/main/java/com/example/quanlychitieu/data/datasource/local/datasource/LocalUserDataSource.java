package com.example.quanlychitieu.data.datasource.local.datasource;

import com.example.quanlychitieu.domain.model.user.User;

public interface LocalUserDataSource {
  User getUser(String userId);

  User getCurrentUser();

  long saveUser(User user);

  void updateUser(User user);

  void deleteUser(String userId);

  void setCurrentUser(String userId);

  void updateUserMoney(String userId, int amount);
}

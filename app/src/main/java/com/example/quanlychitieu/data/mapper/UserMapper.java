package com.example.quanlychitieu.data.mapper;

import com.example.quanlychitieu.data.datasource.local.entity.UserEntity;
import com.example.quanlychitieu.domain.model.user.User;

import java.util.ArrayList;
import java.util.List;

/**
 * Mapper class to convert between User domain model and UserEntity
 */
public class UserMapper {

  /**
   * Convert UserEntity to User domain model
   *
   * @param entity UserEntity to convert
   * @return User domain model
   */
  public User toDomain(UserEntity entity) {
    if (entity == null) {
      return null;
    }

    User user = new User(
        entity.getName(),
        entity.getMoney(),
        entity.getBirthday(),
        entity.isGender(),
        entity.getAvatar());

    // ID có thể được lưu trữ riêng (không nằm trong constructor)
    // nếu User model có setter để thiết lập ID
    // user.setId(entity.getId());

    return user;
  }

  /**
   * Convert User domain model to UserEntity
   *
   * @param domain        User domain model to convert
   * @param isCurrentUser Whether this user is the current user
   * @return UserEntity
   */
  public UserEntity toEntity(User domain, boolean isCurrentUser) {
    if (domain == null) {
      return null;
    }

    UserEntity entity = new UserEntity();
    // entity.setId(domain.getId()); // Tùy vào model User có getId() hay không
    entity.setName(domain.getName());
    entity.setBirthday(domain.getBirthday());
    entity.setAvatar(domain.getAvatar());
    entity.setMoney(domain.getMoney());
    entity.setGender(domain.isGender());
    entity.setEmail(domain.getEmail());
    entity.setCurrentUser(isCurrentUser);
    entity.setCreatedAt(System.currentTimeMillis());
    entity.setUpdatedAt(System.currentTimeMillis());

    return entity;
  }

  /**
   * Convert User domain model to UserEntity
   *
   * @param domain User domain model to convert
   * @return UserEntity (not marked as current user)
   */
  public UserEntity toEntity(User domain) {
    return toEntity(domain, false);
  }

  /**
   * Convert a list of UserEntity to a list of User domain models
   *
   * @param entities List of UserEntity to convert
   * @return List of User domain models
   */
  public List<User> toDomainList(List<UserEntity> entities) {
    if (entities == null) {
      return null;
    }

    List<User> users = new ArrayList<>(entities.size());
    for (UserEntity entity : entities) {
      users.add(toDomain(entity));
    }

    return users;
  }

  /**
   * Convert a list of User domain models to a list of UserEntity
   *
   * @param domains List of User domain models to convert
   * @return List of UserEntity
   */
  public List<UserEntity> toEntityList(List<User> domains) {
    if (domains == null) {
      return null;
    }

    List<UserEntity> entities = new ArrayList<>(domains.size());
    for (User domain : domains) {
      entities.add(toEntity(domain));
    }

    return entities;
  }
}

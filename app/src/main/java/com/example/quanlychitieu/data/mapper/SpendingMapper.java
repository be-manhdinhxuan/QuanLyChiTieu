package com.example.quanlychitieu.data.mapper;

import com.example.quanlychitieu.data.datasource.local.entity.SpendingEntity;
import com.example.quanlychitieu.domain.model.spending.Spending;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Mapper class to convert between Spending domain model and SpendingEntity
 */
public class SpendingMapper {

  private final Gson gson;

  public SpendingMapper() {
    this.gson = new Gson();
  }

  /**
   * Convert SpendingEntity to Spending domain model
   *
   * @param entity SpendingEntity to convert
   * @return Spending domain model
   */
  public Spending toDomain(SpendingEntity entity) {
    if (entity == null) {
      return null;
    }

    // Convert JSON string of friends to List<String>
    List<String> friends = null;
    if (entity.getFriendsJson() != null) {
      Type listType = new TypeToken<ArrayList<String>>() {}.getType();
      friends = gson.fromJson(entity.getFriendsJson(), listType);
    }

    // Sử dụng constructor phù hợp
    Spending spending = new Spending(
        entity.getMoney(),
        entity.getType(),
        entity.getTypeName(),
        entity.getDateTime(),
        entity.getNote(),
        entity.getImage(),
        entity.getLocation(),
        friends
    );

    // Set các thuộc tính bổ sung
    spending.setId(entity.getId());
    spending.setUserId(entity.getUserId());
    spending.setCategoryId(entity.getCategoryId());

    return spending;
  }

  /**
   * Convert Spending domain model to SpendingEntity
   *
   * @param domain Spending domain model to convert
   * @return SpendingEntity
   */
  public SpendingEntity toEntity(Spending domain) {
    if (domain == null) {
      return null;
    }

    SpendingEntity entity = new SpendingEntity();
    entity.setId(domain.getId());
    entity.setUserId(domain.getUserId());
    entity.setMoney(domain.getMoney());
    entity.setDateTime(domain.getDateTime());
    entity.setType(domain.getType());
    entity.setTypeName(domain.getTypeName());
    entity.setNote(domain.getNote());
    entity.setLocation(domain.getLocation());
    entity.setImage(domain.getImage());
    entity.setCategoryId(domain.getCategoryId());

    // Convert List<String> friends to JSON string
    if (domain.getFriends() != null) {
      entity.setFriendsJson(gson.toJson(domain.getFriends()));
    } else {
      entity.setFriendsJson(null);
    }

    return entity;
  }

  /**
   * Convert a list of SpendingEntity to a list of Spending domain models
   *
   * @param entities List of SpendingEntity to convert
   * @return List of Spending domain models
   */
  public List<Spending> toDomainList(List<SpendingEntity> entities) {
    if (entities == null) {
      return null;
    }

    List<Spending> spendings = new ArrayList<>(entities.size());
    for (SpendingEntity entity : entities) {
      spendings.add(toDomain(entity));
    }

    return spendings;
  }

  /**
   * Convert a list of Spending domain models to a list of SpendingEntity
   *
   * @param domains List of Spending domain models to convert
   * @return List of SpendingEntity
   */
  public List<SpendingEntity> toEntityList(List<Spending> domains) {
    if (domains == null) {
      return null;
    }

    List<SpendingEntity> entities = new ArrayList<>(domains.size());
    for (Spending domain : domains) {
      entities.add(toEntity(domain));
    }

    return entities;
  }
}

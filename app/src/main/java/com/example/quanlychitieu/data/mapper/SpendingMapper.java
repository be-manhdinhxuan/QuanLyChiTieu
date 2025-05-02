package com.example.quanlychitieu.data.mapper;

import android.util.Log;
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

  // Constructor initializes Gson instance
  public SpendingMapper() {
    this.gson = new Gson();
  }

  /**
   * Convert SpendingEntity (from local database) to Spending domain model
   * @param entity The SpendingEntity object
   * @return The corresponding Spending domain model object, or null if entity is null
   */
  public Spending toDomain(SpendingEntity entity) {
    if (entity == null) {
      return null;
    }

    Spending spending = new Spending();

    // Map basic fields
    spending.setUserId(entity.getUserId());
    spending.setMoney(entity.getMoney());
    spending.setType(entity.getType());
    spending.setTypeName(entity.getTypeName());
    spending.setDateTime(entity.getDateTime());
    spending.setNote(entity.getNote());
    spending.setLocation(entity.getLocation());
    spending.setImage(entity.getImage());
    spending.setCreatedAt(entity.getCreatedAt());
    spending.setUpdatedAt(entity.getUpdatedAt());
    // spending.setMonthKey(entity.getMonthKey()); // Assuming SpendingEntity also has monthKey

    // Convert friends JSON string back to List<String>
    if (entity.getFriendsJson() != null) {
      try {
        Type listType = new TypeToken<ArrayList<String>>() {}.getType();
        List<String> friends = gson.fromJson(entity.getFriendsJson(), listType);
        spending.setFriends(friends);
      } catch (Exception e) {
        // Handle potential Gson parsing errors
        System.err.println("Error parsing friendsJson: " + e.getMessage());
        spending.setFriends(new ArrayList<>()); // Set empty list on error
      }
    } else {
      spending.setFriends(null); // Or new ArrayList<>() depending on desired behavior
    }


    return spending;
  }

  /**
   * Convert Spending domain model to SpendingEntity (for local database)
   * @param domain The Spending domain model object
   * @return The corresponding SpendingEntity object, or null if domain is null
   */
  public SpendingEntity toEntity(Spending domain) {
    if (domain == null) return null;

    SpendingEntity entity = new SpendingEntity();

    // Map basic fields
    entity.setId(domain.getId());
    entity.setUserId(domain.getUserId());
    entity.setMoney(domain.getMoney());
    entity.setType(domain.getType());
    entity.setTypeName(domain.getTypeName());
    entity.setDateTime(domain.getDateTime());
    entity.setNote(domain.getNote());
    entity.setLocation(domain.getLocation());
    entity.setImage(domain.getImage());
    
    // Xử lý an toàn cho createdAt và updatedAt
    entity.setCreatedAt(domain.getCreatedAt() != null ? domain.getCreatedAt() : System.currentTimeMillis());
    entity.setUpdatedAt(domain.getUpdatedAt() != null ? domain.getUpdatedAt() : System.currentTimeMillis());

    // Convert List<String> friends to JSON string
    if (domain.getFriends() != null) {
      try {
        String friendsJson = gson.toJson(domain.getFriends());
        entity.setFriendsJson(friendsJson);
      } catch (Exception e) {
        Log.e("SpendingMapper", "Error serializing friends list", e);
        entity.setFriendsJson(null);
      }
    } else {
      entity.setFriendsJson(null);
    }

    return entity;
  }

  /**
   * Convert a list of SpendingEntity to a list of Spending domain models
   * @param entities List of SpendingEntity objects
   * @return List of corresponding Spending domain model objects, or null if entities list is null
   */
  public List<Spending> toDomainList(List<SpendingEntity> entities) {
    if (entities == null) {
      return null; // Or return new ArrayList<>() depending on desired behavior
    }

    List<Spending> spendings = new ArrayList<>(entities.size());
    for (SpendingEntity entity : entities) {
      // Add null check for individual items if necessary
      Spending domain = toDomain(entity);
      if (domain != null) {
        spendings.add(domain);
      }
    }

    return spendings;
  }

  /**
   * Convert a list of Spending domain models to a list of SpendingEntity
   * @param domains List of Spending domain model objects
   * @return List of corresponding SpendingEntity objects, or null if domains list is null
   */
  public List<SpendingEntity> toEntityList(List<Spending> domains) {
    if (domains == null) {
      return null; // Or return new ArrayList<>()
    }

    List<SpendingEntity> entities = new ArrayList<>(domains.size());
    for (Spending domain : domains) {
      // Add null check for individual items if necessary
      SpendingEntity entity = toEntity(domain);
      if (entity != null) {
        entities.add(entity);
      }
    }

    return entities;
  }
}

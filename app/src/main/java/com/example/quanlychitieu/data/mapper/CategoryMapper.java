package com.example.quanlychitieu.data.mapper;

import com.example.quanlychitieu.data.datasource.local.entity.CategoryEntity;
import com.example.quanlychitieu.domain.model.category.Category;

import java.util.ArrayList;
import java.util.List;

/**
 * Mapper class to convert between Category domain model and CategoryEntity
 */
public class CategoryMapper {

  /**
   * Convert CategoryEntity to Category domain model
   *
   * @param entity CategoryEntity to convert
   * @return Category domain model
   */
  public Category toDomain(CategoryEntity entity) {
    if (entity == null) {
      return null;
    }

    return new Category(
        entity.getId(),
        entity.getName(),
        entity.getIcon(),
        entity.getColor(),
        entity.getType(),
        entity.isDefault());
  }

  /**
   * Convert Category domain model to CategoryEntity
   *
   * @param domain Category domain model to convert
   * @return CategoryEntity
   */
  public CategoryEntity toEntity(Category domain) {
    if (domain == null) {
      return null;
    }

    CategoryEntity entity = new CategoryEntity();
    entity.setId(domain.getId());
    entity.setName(domain.getName());
    entity.setIcon(domain.getIcon());
    entity.setColor(domain.getColor());
    entity.setType(domain.getType());
    entity.setDefault(domain.isDefault());

    return entity;
  }

  /**
   * Convert a list of CategoryEntity to a list of Category domain models
   *
   * @param entities List of CategoryEntity to convert
   * @return List of Category domain models
   */
  public List<Category> toDomainList(List<CategoryEntity> entities) {
    if (entities == null) {
      return null;
    }

    List<Category> categories = new ArrayList<>(entities.size());
    for (CategoryEntity entity : entities) {
      categories.add(toDomain(entity));
    }

    return categories;
  }

  /**
   * Convert a list of Category domain models to a list of CategoryEntity
   *
   * @param domains List of Category domain models to convert
   * @return List of CategoryEntity
   */
  public List<CategoryEntity> toEntityList(List<Category> domains) {
    if (domains == null) {
      return null;
    }

    List<CategoryEntity> entities = new ArrayList<>(domains.size());
    for (Category domain : domains) {
      entities.add(toEntity(domain));
    }

    return entities;
  }
}

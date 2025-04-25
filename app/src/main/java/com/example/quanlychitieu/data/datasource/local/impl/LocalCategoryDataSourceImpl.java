package com.example.quanlychitieu.data.datasource.local.impl;

import com.example.quanlychitieu.data.datasource.local.dao.CategoryDao;
import com.example.quanlychitieu.data.datasource.local.datasource.LocalCategoryDataSource;
import com.example.quanlychitieu.data.datasource.local.entity.CategoryEntity;
import com.example.quanlychitieu.data.mapper.CategoryMapper;
import com.example.quanlychitieu.domain.model.category.Category;

import java.util.ArrayList;
import java.util.List;

public class LocalCategoryDataSourceImpl implements LocalCategoryDataSource {
  private final CategoryDao categoryDao;
  private final CategoryMapper mapper;

  public LocalCategoryDataSourceImpl(CategoryDao categoryDao, CategoryMapper mapper) {
    this.categoryDao = categoryDao;
    this.mapper = mapper;
  }

  @Override
  public Category getCategory(int id) {
    CategoryEntity entity = categoryDao.getCategoryById(id);
    return entity != null ? mapper.toDomain(entity) : null;
  }

  @Override
  public List<Category> getAllCategories() {
    List<CategoryEntity> entities = categoryDao.getAllCategories();
    List<Category> categories = new ArrayList<>();
    for (CategoryEntity entity : entities) {
      categories.add(mapper.toDomain(entity));
    }
    return categories;
  }

  @Override
  public long saveCategory(Category category) {
    CategoryEntity entity = mapper.toEntity(category);
    return categoryDao.insertCategory(entity);
  }

  @Override
  public void saveCategories(List<Category> categories) {
    List<CategoryEntity> entities = new ArrayList<>();
    for (Category category : categories) {
      entities.add(mapper.toEntity(category));
    }
    categoryDao.insertAllCategories(entities);
  }

  @Override
  public void updateCategory(Category category) {
    CategoryEntity entity = mapper.toEntity(category);
    categoryDao.updateCategory(entity);
  }

  @Override
  public void deleteCategory(int id) {
    CategoryEntity entity = categoryDao.getCategoryById(id);
    if (entity != null) {
      categoryDao.deleteCategory(entity);
    }
  }
}

package com.example.quanlychitieu.data.datasource.local.datasource;

import com.example.quanlychitieu.domain.model.category.Category;

import java.util.List;

/**
 * Interface for accessing category data from local storage
 */
public interface LocalCategoryDataSource {
  /**
   * Get a category by its ID
   *
   * @param id Category ID
   * @return Category or null if not found
   */
  Category getCategory(int id);

  /**
   * Get all categories
   *
   * @return List of all categories
   */
  List<Category> getAllCategories();

  /**
   * Save a new category
   *
   * @param category Category to save
   * @return ID of the saved category
   */
  long saveCategory(Category category);

  /**
   * Save multiple categories
   *
   * @param categories List of categories to save
   */
  void saveCategories(List<Category> categories);

  /**
   * Update an existing category
   *
   * @param category Category with updated data
   */
  void updateCategory(Category category);

  /**
   * Delete a category
   *
   * @param id ID of the category to delete
   */
  void deleteCategory(int id);
}

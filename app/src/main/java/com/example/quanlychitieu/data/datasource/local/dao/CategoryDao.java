package com.example.quanlychitieu.data.datasource.local.dao;
import androidx.room.*;
import com.example.quanlychitieu.data.datasource.local.entity.CategoryEntity;

import java.util.List;

@Dao
public interface CategoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertCategory(CategoryEntity category);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAllCategories(List<CategoryEntity> categories);

    @Query("SELECT * FROM categories WHERE id = :id")
    CategoryEntity getCategoryById(int id);

    @Query("SELECT * FROM categories ORDER BY name ASC")
    List<CategoryEntity> getAllCategories();

    @Update
    void updateCategory(CategoryEntity category);

    @Delete
    void deleteCategory(CategoryEntity category);
}

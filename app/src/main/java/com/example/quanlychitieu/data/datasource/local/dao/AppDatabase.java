package com.example.quanlychitieu.data.datasource.local.dao;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.example.quanlychitieu.data.datasource.local.converter.DateConverter;
import com.example.quanlychitieu.data.datasource.local.entity.CategoryEntity;
import com.example.quanlychitieu.data.datasource.local.entity.MonthlyDataEntity;
import com.example.quanlychitieu.data.datasource.local.entity.SpendingEntity;
import com.example.quanlychitieu.data.datasource.local.entity.UserEntity;

/**
 * Room Database class for local storage
 */
@Database(entities = { UserEntity.class, SpendingEntity.class, CategoryEntity.class,
        MonthlyDataEntity.class }, version = 1)
@TypeConverters({ DateConverter.class })
public abstract class AppDatabase extends RoomDatabase {
    public abstract UserDao userDao();

    public abstract SpendingDao spendingDao();

    public abstract CategoryDao categoryDao();

    public abstract MonthlyDataDao monthlyDataDao();

    // Singleton instance
    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "quanlychitieu.db")
                            .fallbackToDestructiveMigration() // Xóa và tạo lại DB khi version thay đổi
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}

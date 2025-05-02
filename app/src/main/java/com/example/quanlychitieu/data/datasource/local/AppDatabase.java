package com.example.quanlychitieu.data.datasource.local;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.example.quanlychitieu.data.datasource.local.converter.DateConverter;
import com.example.quanlychitieu.data.datasource.local.entity.UserEntity;
import com.example.quanlychitieu.data.datasource.local.entity.SpendingEntity;
import com.example.quanlychitieu.data.datasource.local.entity.MonthlyDataEntity;
import com.example.quanlychitieu.data.datasource.local.dao.UserDao;
import com.example.quanlychitieu.data.datasource.local.dao.SpendingDao;
import com.example.quanlychitieu.data.datasource.local.dao.MonthlyDataDao;

@Database(
    entities = {
        UserEntity.class,
        SpendingEntity.class,
        MonthlyDataEntity.class
    },
    version = 4,
    exportSchema = false
)
@TypeConverters({ DateConverter.class })
public abstract class AppDatabase extends RoomDatabase {
    private static final String DATABASE_NAME = "quanlychitieu.db";
    
    public abstract UserDao userDao();
    public abstract SpendingDao spendingDao();
    public abstract MonthlyDataDao monthlyDataDao();

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            DATABASE_NAME)
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
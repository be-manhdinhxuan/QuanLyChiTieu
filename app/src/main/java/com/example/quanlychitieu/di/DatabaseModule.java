package com.example.quanlychitieu.di;

import android.content.Context;
import androidx.room.Room;
import com.example.quanlychitieu.data.datasource.local.AppDatabase;
import com.example.quanlychitieu.data.datasource.local.dao.UserDao;
import com.example.quanlychitieu.data.datasource.local.dao.SpendingDao;
import javax.inject.Singleton;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public class DatabaseModule {

    @Provides
    @Singleton
    public AppDatabase provideAppDatabase(@ApplicationContext Context context) {
        return Room.databaseBuilder(
                context,
                AppDatabase.class,
                "quanlychitieu.db")
                .fallbackToDestructiveMigration()
                .build();
    }

    @Provides
    @Singleton
    public UserDao provideUserDao(AppDatabase database) {
        return database.userDao();
    }

    @Provides
    @Singleton
    public SpendingDao provideSpendingDao(AppDatabase database) {
        return database.spendingDao();
    }
}

package com.example.quanlychitieu.data.datasource.local.dao;

import androidx.room.*;
import com.example.quanlychitieu.data.datasource.local.entity.MonthlyDataEntity;

@Dao
public interface MonthlyDataDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertMonthlyData(MonthlyDataEntity monthlyData);

    @Query("SELECT * FROM monthly_data WHERE userId = :userId AND monthKey = :monthKey")
    MonthlyDataEntity getMonthlyData(String userId, String monthKey);

    @Update
    void updateMonthlyData(MonthlyDataEntity monthlyData);

    @Delete
    void deleteMonthlyData(MonthlyDataEntity monthlyData);
}

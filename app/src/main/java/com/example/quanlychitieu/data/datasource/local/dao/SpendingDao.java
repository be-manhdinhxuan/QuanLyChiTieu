package com.example.quanlychitieu.data.datasource.local.dao;

import androidx.room.*;
import com.example.quanlychitieu.data.datasource.local.entity.SpendingEntity;

import java.util.Date;
import java.util.List;

@Dao
public interface SpendingDao {
    @Query("SELECT * FROM spendings WHERE userId = :userId")
    List<SpendingEntity> getAllSpendingsByUser(String userId);

    @Query("SELECT * FROM spendings WHERE userId = :userId AND dateTime BETWEEN :startDate AND :endDate")
    List<SpendingEntity> getSpendingsByDate(String userId, Date startDate, Date endDate);

    @Query("SELECT * FROM spendings WHERE userId = :userId AND id = :spendingId")
    SpendingEntity getSpendingById(String userId, String spendingId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertSpending(SpendingEntity spending);

    @Update
    void updateSpending(SpendingEntity spending);

    @Query("DELETE FROM spendings WHERE userId = :userId AND id = :spendingId")
    void deleteSpending(String userId, String spendingId);

    @Query("DELETE FROM spendings WHERE userId = :userId")
    void deleteAllSpendingsByUser(String userId);

    @Query("SELECT * FROM spendings WHERE userId = :userId AND type = :type")
    List<SpendingEntity> getSpendingsByType(String userId, int type);
}

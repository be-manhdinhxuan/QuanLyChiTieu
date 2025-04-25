package com.example.quanlychitieu.data.datasource.local.impl;

import com.example.quanlychitieu.data.datasource.local.dao.SpendingDao;
import com.example.quanlychitieu.data.datasource.local.datasource.LocalSpendingDataSource;
import com.example.quanlychitieu.data.datasource.local.entity.SpendingEntity;
import com.example.quanlychitieu.data.mapper.SpendingMapper;
import com.example.quanlychitieu.domain.model.spending.Spending;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Implementation of LocalSpendingDataSource using Room database
 */
public class LocalSpendingDataSourceImpl implements LocalSpendingDataSource {
  private final SpendingDao spendingDao;
  private final SpendingMapper mapper;

  public LocalSpendingDataSourceImpl(SpendingDao spendingDao, SpendingMapper mapper) {
    this.spendingDao = spendingDao;
    this.mapper = mapper;
  }

  @Override
  public Spending getSpending(String id) {
    // Tách chuỗi id thành userId và spendingId
    String[] parts = id.split("/");
    if (parts.length != 2) {
      return null; // hoặc throw exception tùy vào yêu cầu của bạn
    }
    String userId = parts[0];
    String spendingId = parts[1];
    
    SpendingEntity entity = spendingDao.getSpendingById(userId, spendingId);
    return entity != null ? mapper.toDomain(entity) : null;
  }

  @Override
  public List<Spending> getAllSpendings(String userId) {
    List<SpendingEntity> entities = spendingDao.getAllSpendingsByUser(userId);
    return mapEntitiesToDomainList(entities);
  }

  @Override
  public List<Spending> getSpendingsByDateRange(String userId, Date startDate, Date endDate) {
    List<SpendingEntity> entities = spendingDao.getSpendingsByDate(userId, startDate, endDate);
    return mapEntitiesToDomainList(entities);
  }

  @Override
  public List<Spending> getSpendingsByMonth(String userId, Date date) {
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(date);
    
    // Set to first day of month
    calendar.set(Calendar.DAY_OF_MONTH, 1);
    calendar.set(Calendar.HOUR_OF_DAY, 0);
    calendar.set(Calendar.MINUTE, 0);
    calendar.set(Calendar.SECOND, 0);
    calendar.set(Calendar.MILLISECOND, 0);
    Date startDate = calendar.getTime();

    // Set to last day of month
    calendar.add(Calendar.MONTH, 1);
    calendar.add(Calendar.MILLISECOND, -1);
    Date endDate = calendar.getTime();

    return getSpendingsByDateRange(userId, startDate, endDate);
  }

  @Override
  public List<Spending> getSpendingsByType(String userId, int type) {
    List<SpendingEntity> entities = spendingDao.getSpendingsByCategory(userId, String.valueOf(type));
    return mapEntitiesToDomainList(entities);
  }

  @Override
  public List<Spending> getUnsyncedSpendings() {
    // Since there's no direct DAO method for unsynced spendings,
    // we'll need to implement this in the DAO first
    return new ArrayList<>();
  }

  @Override
  public long saveSpending(Spending spending) {
    SpendingEntity entity = mapper.toEntity(spending);
    return spendingDao.insertSpending(entity);
  }

  @Override
  public void saveSpendings(List<Spending> spendings) {
    for (Spending spending : spendings) {
      saveSpending(spending);
    }
  }

  @Override
  public void updateSpending(Spending spending) {
    SpendingEntity entity = mapper.toEntity(spending);
    spendingDao.updateSpending(entity);
  }

  @Override
  public void deleteSpending(String id) {
    String[] parts = id.split("/");
    String userId = parts[0];
    String spendingId = parts[1];
    spendingDao.deleteSpending(userId, spendingId);
  }

  @Override
  public void markAsSynced(String id) {
    // This functionality needs to be added to the DAO
    // For now, we'll leave it as a no-op
  }

  /**
   * Helper method to convert list of entities to domain models
   */
  private List<Spending> mapEntitiesToDomainList(List<SpendingEntity> entities) {
    List<Spending> result = new ArrayList<>();
    if (entities != null) {
      for (SpendingEntity entity : entities) {
        result.add(mapper.toDomain(entity));
      }
    }
    return result;
  }
}

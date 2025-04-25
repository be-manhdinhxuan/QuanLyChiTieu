package com.example.quanlychitieu.data.datasource.local.datasource;

import com.example.quanlychitieu.domain.model.spending.Spending;

import java.util.Date;
import java.util.List;

public interface LocalSpendingDataSource {
  Spending getSpending(String id);

  List<Spending> getAllSpendings(String userId);

  List<Spending> getSpendingsByMonth(String userId, Date date);

  List<Spending> getSpendingsByDateRange(String userId, Date startDate, Date endDate);

  List<Spending> getSpendingsByType(String userId, int type);

  List<Spending> getUnsyncedSpendings();

  long saveSpending(Spending spending);

  void saveSpendings(List<Spending> spendings);

  void updateSpending(Spending spending);

  void deleteSpending(String id);

  void markAsSynced(String id);
}

package com.example.quanlychitieu.data.repository.impl;

import android.util.Log;

import com.example.quanlychitieu.data.datasource.firebase.auth.FirebaseAuthDataSource;
import com.example.quanlychitieu.data.datasource.firebase.firestore.FirestoreSpendingDataSource;
import com.example.quanlychitieu.data.datasource.local.datasource.LocalSpendingDataSource;
import com.example.quanlychitieu.data.mapper.SpendingMapper;
import com.example.quanlychitieu.domain.model.spending.Spending;
import com.example.quanlychitieu.domain.repository.SpendingRepository;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseUser;

import java.util.Date;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Implementation of SpendingRepository that combines remote (Firebase) and
 * local
 * (Room) datasources
 */
public class SpendingRepositoryImpl implements SpendingRepository {
  private static final String TAG = "SpendingRepositoryImpl";

  private final FirestoreSpendingDataSource remoteDataSource;
  private final LocalSpendingDataSource localDataSource;
  private final FirebaseAuthDataSource authDataSource;
  private final SpendingMapper mapper;
  private final Executor executor;

  public SpendingRepositoryImpl(
      FirestoreSpendingDataSource remoteDataSource,
      LocalSpendingDataSource localDataSource,
      FirebaseAuthDataSource authDataSource,
      SpendingMapper mapper) {
    this.remoteDataSource = remoteDataSource;
    this.localDataSource = localDataSource;
    this.authDataSource = authDataSource;
    this.mapper = mapper;
    this.executor = Executors.newSingleThreadExecutor();
  }

  @Override
  public Task<List<Spending>> getAllSpendings() {
    // Kiểm tra người dùng hiện tại
    FirebaseUser firebaseUser = authDataSource.getCurrentUser();
    if (firebaseUser == null) {
      return Tasks.forException(new Exception("No user is signed in"));
    }

    final String userId = firebaseUser.getUid();

    // Kiểm tra cache trong local database trước
    List<Spending> localSpendings = localDataSource.getAllSpendings(userId);

    if (localSpendings != null && !localSpendings.isEmpty()) {
      // Nếu có dữ liệu trong cache, trả về ngay
      return Tasks.forResult(localSpendings);
    }

    // Nếu không có trong cache, lấy từ Firestore
    return remoteDataSource.getAllSpendings()
        .continueWith(task -> {
          if (task.isSuccessful() && task.getResult() != null) {
            List<Spending> spendings = task.getResult();
            cacheSpendings(userId, spendings);
            return spendings;
          } else {
            Log.e(TAG, "Error getting spendings", task.getException());
            throw task.getException();
          }
        });
  }

  @Override
  public Task<List<Spending>> getSpendingsByDate(Date startDate, Date endDate) {
    // Kiểm tra người dùng hiện tại
    FirebaseUser firebaseUser = authDataSource.getCurrentUser();
    if (firebaseUser == null) {
      return Tasks.forException(new Exception("No user is signed in"));
    }

    final String userId = firebaseUser.getUid();

    // Kiểm tra cache trong local database trước
    List<Spending> localSpendings = localDataSource.getSpendingsByDateRange(userId, startDate, endDate);

    if (localSpendings != null && !localSpendings.isEmpty()) {
      // Nếu có dữ liệu trong cache, trả về ngay
      return Tasks.forResult(localSpendings);
    }

    // Nếu không có trong cache, lấy từ Firestore
    return remoteDataSource.getSpendingsByDateRange(startDate, endDate)
        .continueWith(task -> {
          if (task.isSuccessful() && task.getResult() != null) {
            List<Spending> spendings = task.getResult();
            cacheSpendings(userId, spendings);
            return spendings;
          } else {
            Log.e(TAG, "Error getting spendings by date", task.getException());
            throw task.getException();
          }
        });
  }

  @Override
  public Task<List<Spending>> getSpendingsByCategory(String categoryId) {
    // Kiểm tra người dùng hiện tại
    FirebaseUser firebaseUser = authDataSource.getCurrentUser();
    if (firebaseUser == null) {
      return Tasks.forException(new Exception("No user is signed in"));
    }

    final String userId = firebaseUser.getUid();

    // Kiểm tra cache trong local database trước
    List<Spending> localSpendings = localDataSource.getSpendingsByType(userId, Integer.parseInt(categoryId));

    if (localSpendings != null && !localSpendings.isEmpty()) {
      // Nếu có dữ liệu trong cache, trả về ngay
      return Tasks.forResult(localSpendings);
    }

    // Nếu không có trong cache, lấy từ Firestore
    return remoteDataSource.getSpendingsByType(Integer.parseInt(categoryId))
        .continueWith(task -> {
          if (task.isSuccessful() && task.getResult() != null) {
            List<Spending> spendings = task.getResult();
            cacheSpendings(userId, spendings);
            return spendings;
          } else {
            Log.e(TAG, "Error getting spendings by category", task.getException());
            throw task.getException();
          }
        });
  }

  @Override
  public Task<Spending> getSpending(String spendingId) {
    // Kiểm tra người dùng hiện tại
    FirebaseUser firebaseUser = authDataSource.getCurrentUser();
    if (firebaseUser == null) {
      return Tasks.forException(new Exception("No user is signed in"));
    }

    final String userId = firebaseUser.getUid();
    // Tạo compositeId bằng cách kết hợp userId và spendingId
    String compositeId = userId + "/" + spendingId;

    // Kiểm tra cache trong local database trước
    Spending localSpending = localDataSource.getSpending(compositeId);

    if (localSpending != null) {
      // Nếu có dữ liệu trong cache, trả về ngay
      return Tasks.forResult(localSpending);
    }

    // Nếu không có trong cache, lấy từ Firestore
    return remoteDataSource.getSpending(spendingId)
        .continueWith(task -> {
          if (task.isSuccessful() && task.getResult() != null) {
            // Lưu kết quả vào cache
            Spending spending = task.getResult();
            cacheSpending(userId, spending);
            return spending;
          } else {
            Log.e(TAG, "Error getting spending", task.getException());
            throw task.getException();
          }
        });
  }

  @Override
  public Task<String> addSpending(Spending spending) {
    // Kiểm tra người dùng hiện tại
    FirebaseUser firebaseUser = authDataSource.getCurrentUser();
    if (firebaseUser == null) {
      return Tasks.forException(new Exception("No user is signed in"));
    }

    final String userId = firebaseUser.getUid();

    // Thêm thông tin userId vào spending
    spending.setUserId(userId);

    // Lưu vào Firestore
    return remoteDataSource.addSpending(spending)
        .continueWith(task -> {
          if (task.isSuccessful() && task.getResult() != null) {
            // Lấy ID mới được tạo
            String spendingId = task.getResult();
            spending.setId(spendingId);

            // Lưu vào local database
            cacheSpending(userId, spending);
            return spendingId;
          } else {
            Log.e(TAG, "Error adding spending", task.getException());
            throw task.getException();
          }
        });
  }

  @Override
  public Task<Void> updateSpending(Spending spending) {
    // Kiểm tra người dùng hiện tại
    FirebaseUser firebaseUser = authDataSource.getCurrentUser();
    if (firebaseUser == null) {
      return Tasks.forException(new Exception("No user is signed in"));
    }

    final String userId = firebaseUser.getUid();

    // Thêm thông tin userId vào spending
    spending.setUserId(userId);

    // Cập nhật trên Firestore
    return remoteDataSource.updateSpending(spending)
        .continueWith(task -> {
          if (task.isSuccessful()) {
            // Cập nhật trong local database
            cacheSpending(userId, spending);
          } else {
            Log.e(TAG, "Error updating spending", task.getException());
          }
          return null;
        });
  }

  @Override
  public Task<Void> deleteSpending(String spendingId) {
    // Kiểm tra người dùng hiện tại
    FirebaseUser firebaseUser = authDataSource.getCurrentUser();
    if (firebaseUser == null) {
      return Tasks.forException(new Exception("No user is signed in"));
    }

    final String userId = firebaseUser.getUid();
    // Tạo compositeId cho local database
    String compositeId = userId + "/" + spendingId;

    // Xóa từ Firestore
    return remoteDataSource.deleteSpending(spendingId)
        .continueWith(task -> {
          if (task.isSuccessful()) {
            // Xóa khỏi local database
            localDataSource.deleteSpending(compositeId);
          } else {
            Log.e(TAG, "Error deleting spending", task.getException());
          }
          return null;
        });
  }

  @Override
  public Task<Void> syncSpendings() {
    // Kiểm tra người dùng hiện tại
    FirebaseUser firebaseUser = authDataSource.getCurrentUser();
    if (firebaseUser == null) {
      return Tasks.forException(new Exception("No user is signed in"));
    }

    final String userId = firebaseUser.getUid();

    // Lấy tất cả dữ liệu từ Firestore
    return remoteDataSource.getAllSpendings()
        .continueWith(task -> {
          if (task.isSuccessful() && task.getResult() != null) {
            // Xóa tất cả dữ liệu cũ trong cache và thay thế bằng dữ liệu mới
            List<Spending> spendings = task.getResult();
            executor.execute(() -> {
              // Xóa tất cả dữ liệu cũ trong cache
              // Lưu ý: Cần thêm phương thức deleteAllSpendings vào LocalSpendingDataSource
              // localDataSource.deleteAllSpendings(userId);
              cacheSpendings(userId, spendings);
            });
          } else {
            Log.e(TAG, "Error syncing spendings", task.getException());
          }
          return null;
        });
  }

  @Override
  public Task<Integer> getTotalSpending(Date startDate, Date endDate) {
    // Kiểm tra người dùng hiện tại
    FirebaseUser firebaseUser = authDataSource.getCurrentUser();
    if (firebaseUser == null) {
      return Tasks.forException(new Exception("No user is signed in"));
    }

    final String userId = firebaseUser.getUid();

    // Lấy tổng chi tiêu từ Firestore
    return remoteDataSource.getTotalSpending(userId, startDate, endDate)
        .continueWith(task -> {
          if (task.isSuccessful() && task.getResult() != null) {
            return task.getResult();
          } else {
            Log.e(TAG, "Error getting total spending", task.getException());
            throw task.getException();
          }
        });
  }

  @Override
  public Task<List<Object[]>> getSpendingGroupByCategory(Date startDate, Date endDate) {
    // Kiểm tra người dùng hiện tại
    FirebaseUser firebaseUser = authDataSource.getCurrentUser();
    if (firebaseUser == null) {
      return Tasks.forException(new Exception("No user is signed in"));
    }

    final String userId = firebaseUser.getUid();

    // Lấy dữ liệu thống kê từ Firestore
    return remoteDataSource.getSpendingGroupByCategory(userId, startDate, endDate)
        .continueWith(task -> {
          if (task.isSuccessful() && task.getResult() != null) {
            return task.getResult();
          } else {
            Log.e(TAG, "Error getting spending by category", task.getException());
            throw task.getException();
          }
        });
  }

  /**
   * Lưu danh sách spending vào local database (cache)
   */
  private void cacheSpendings(String userId, List<Spending> spendings) {
    executor.execute(() -> {
      for (Spending spending : spendings) {
        // Đảm bảo userId được gán
        spending.setUserId(userId);
        // Tạo compositeId cho spending
        if (spending.getId() != null) {
          String compositeId = userId + "/" + spending.getId();
          spending.setId(compositeId);
        }
        localDataSource.saveSpending(spending);
      }
    });
  }

  /**
   * Lưu một spending vào local database (cache)
   */
  private void cacheSpending(String userId, Spending spending) {
    executor.execute(() -> {
      // Đảm bảo userId được gán
      spending.setUserId(userId);
      // Tạo compositeId cho spending nếu cần
      if (spending.getId() != null) {
        String compositeId = userId + "/" + spending.getId();
        spending.setId(compositeId);
      }
      localDataSource.saveSpending(spending);
    });
  }
}

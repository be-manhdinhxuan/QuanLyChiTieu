package com.example.quanlychitieu.data.repository.adapter;

import android.net.Uri;
import android.content.Context;

import com.example.quanlychitieu.data.datasource.firebase.auth.FirebaseAuthConfig;
import com.example.quanlychitieu.data.datasource.firebase.auth.FirebaseAuthDataSource;
import com.example.quanlychitieu.data.datasource.firebase.firestore.impl.FirestoreSpendingDataSourceImpl;
import com.example.quanlychitieu.data.datasource.local.dao.AppDatabase;
import com.example.quanlychitieu.data.datasource.local.impl.LocalSpendingDataSourceImpl;
import com.example.quanlychitieu.data.mapper.SpendingMapper;
import com.example.quanlychitieu.data.repository.impl.SpendingRepositoryImpl;
import com.example.quanlychitieu.domain.model.spending.Spending;
import com.example.quanlychitieu.domain.repository.SpendingRepository;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Adapter để giữ khả năng tương thích với code cũ sử dụng SpendingRepository
 * trực tiếp
 */
public class LegacySpendingRepositoryAdapter {
  private final SpendingRepository repository;
  private final Context context;

  public LegacySpendingRepositoryAdapter(Context context, FirebaseFirestore firestore) {
    this.context = context;

    // Khởi tạo SpendingRepositoryImpl
    FirestoreSpendingDataSourceImpl remoteDataSource = new FirestoreSpendingDataSourceImpl(context);
    LocalSpendingDataSourceImpl localDataSource = new LocalSpendingDataSourceImpl(
        AppDatabase.getInstance(context).spendingDao(),
        new SpendingMapper());

    FirebaseAuthDataSource authDataSource = FirebaseAuthConfig.getAuthDataSource();
    SpendingMapper mapper = new SpendingMapper();

    this.repository = new SpendingRepositoryImpl(
        remoteDataSource,
        localDataSource,
        authDataSource,
        mapper);
  }

  // Triển khai các phương thức cũ của SpendingRepository nhưng gọi đến
  // implementation mới

  public Task<Void> updateSpending(Spending spending, Uri newImageUri, boolean isImageRemoved) {
    // Trong adapter này chúng ta bỏ qua tham số hình ảnh vì SpendingRepositoryImpl
    // không xử lý hình ảnh
    // Trong trường hợp cần thiết, bạn có thể lưu hình ảnh riêng
    return repository.updateSpending(spending);
  }

  public Task<Void> addSpending(Spending spending, Uri imageUri) {
    return repository.addSpending(spending)
        .continueWithTask(task -> {
          if (task.isSuccessful()) {
            String spendingId = task.getResult();
            spending.setId(spendingId);
            return Tasks.forResult(null);
          } else {
            throw task.getException();
          }
        });
  }

  public Task<Void> deleteSpending(Spending spending) {
    return repository.deleteSpending(spending.getId());
  }

  // Các phương thức khác từ SpendingRepository cũ
}

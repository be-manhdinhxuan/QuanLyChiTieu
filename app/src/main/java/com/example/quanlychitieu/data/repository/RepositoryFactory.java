package com.example.quanlychitieu.data.repository;

import android.content.Context;

import com.example.quanlychitieu.data.datasource.firebase.auth.FirebaseAuthConfig;
import com.example.quanlychitieu.data.datasource.firebase.auth.FirebaseAuthDataSource;
import com.example.quanlychitieu.data.datasource.firebase.firestore.FirestoreUserDataSource;
import com.example.quanlychitieu.data.datasource.firebase.firestore.impl.FirestoreUserDataSourceImpl;
import com.example.quanlychitieu.data.datasource.local.AppDatabase;
import com.example.quanlychitieu.data.datasource.local.datasource.LocalUserDataSource;
import com.example.quanlychitieu.data.datasource.local.impl.LocalUserDataSourceImpl;
import com.example.quanlychitieu.data.mapper.UserMapper;
import com.example.quanlychitieu.data.repository.impl.UserRepositoryImpl;
import com.example.quanlychitieu.domain.repository.UserRepository;
import com.google.firebase.firestore.FirebaseFirestore; // Import FirebaseFirestore

/**
 * Factory class để tạo và cung cấp các instance của Repository
 * Lưu ý: Cách dùng Factory như này không được khuyến khích khi sử dụng Hilt/Dagger.
 * Hilt/Dagger được thiết kế để tự động quản lý việc khởi tạo và inject dependency.
 * Đoạn code này chỉ để sửa lỗi hiện tại, bạn nên cân nhắc chuyển sang inject Repository trực tiếp bằng Hilt.
 */
public class RepositoryFactory {
  // Singleton instances
  private static UserRepository userRepository;
  private static LocalUserDataSource localUserDataSource;

  /**
   * Lấy instance của UserRepository
   *
   * @param context Context để truy cập vào database và khởi tạo Firestore DS
   * @return UserRepository instance
   */
  public static synchronized UserRepository getUserRepository(Context context) {
    if (userRepository == null) {
      // Khởi tạo các dependency
      Context appContext = context.getApplicationContext(); // Nên dùng Application Context

      // *** SỬA LỖI Ở ĐÂY ***
      // Lấy instance FirebaseFirestore
      FirebaseFirestore firestoreInstance = FirebaseFirestore.getInstance();

      // Khởi tạo FirestoreUserDataSource với cả context và firestore instance
      FirestoreUserDataSource firestoreUserDataSource = new FirestoreUserDataSourceImpl(
              appContext,
              firestoreInstance
      );
      // *** KẾT THÚC SỬA LỖI ***

      // Lấy LocalUserDataSource
      LocalUserDataSource localUserDataSource = getLocalUserDataSource(appContext);

      // Lấy FirebaseAuthDataSource từ config
      FirebaseAuthDataSource authDataSource = FirebaseAuthConfig.getAuthDataSource();

      // Khởi tạo mapper
      UserMapper userMapper = new UserMapper();

      // Tạo và lưu trữ instance của UserRepository
      userRepository = new UserRepositoryImpl(
              firestoreUserDataSource,
              localUserDataSource, // Có thể null nếu không dùng cache
              authDataSource,
              userMapper);
    }

    return userRepository;
  }

  /**
   * Lấy instance của LocalUserDataSource
   *
   * @param context Context để truy cập vào database
   * @return LocalUserDataSource instance
   */
  private static synchronized LocalUserDataSource getLocalUserDataSource(Context context) {
    // Chỉ khởi tạo nếu chưa có và có dùng Room
    if (localUserDataSource == null) {
      try {
        // Tạo adapter/implementation của LocalUserDataSource sử dụng UserDao
        localUserDataSource = new LocalUserDataSourceImpl(
                AppDatabase.getInstance(context).userDao(), // Lấy UserDao từ Room DB
                new UserMapper()); // Tạo UserMapper mới hoặc inject nếu cần
      } catch (Exception e) {
        // Xử lý lỗi nếu không thể khởi tạo Room DB hoặc Dao
        System.err.println("Error initializing LocalUserDataSource: " + e.getMessage());
        // Có thể trả về null hoặc một dummy implementation tùy logic
        return null;
      }
    }
    return localUserDataSource;
  }

  /**
   * Reset tất cả các repository instances
   * (Hữu ích khi logout hoặc testing)
   */
  public static void resetAll() {
    userRepository = null;
    localUserDataSource = null;
    // Reset các repository khác nếu có
  }
}
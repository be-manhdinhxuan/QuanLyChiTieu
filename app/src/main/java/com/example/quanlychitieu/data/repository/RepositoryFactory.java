package com.example.quanlychitieu.data.repository;

import android.content.Context;

import com.example.quanlychitieu.data.datasource.firebase.auth.FirebaseAuthConfig;
import com.example.quanlychitieu.data.datasource.firebase.auth.FirebaseAuthDataSource;
import com.example.quanlychitieu.data.datasource.firebase.firestore.FirestoreUserDataSource;
import com.example.quanlychitieu.data.datasource.firebase.firestore.impl.FirestoreUserDataSourceImpl;
import com.example.quanlychitieu.data.datasource.local.dao.AppDatabase;
import com.example.quanlychitieu.data.datasource.local.datasource.LocalUserDataSource;
import com.example.quanlychitieu.data.datasource.local.impl.LocalUserDataSourceImpl;
import com.example.quanlychitieu.data.mapper.UserMapper;
import com.example.quanlychitieu.data.repository.impl.UserRepositoryImpl;
import com.example.quanlychitieu.domain.repository.UserRepository;

/**
 * Factory class để tạo và cung cấp các instance của Repository
 */
public class RepositoryFactory {
  // Singleton instances
  private static UserRepository userRepository;
  private static LocalUserDataSource localUserDataSource;

  /**
   * Lấy instance của UserRepository
   *
   * @param context Context để truy cập vào database
   * @return UserRepository instance
   */
  public static synchronized UserRepository getUserRepository(Context context) {
    if (userRepository == null) {
      // Khởi tạo các dependency
      FirestoreUserDataSource firestoreUserDataSource = new FirestoreUserDataSourceImpl(context);

      // Lấy LocalUserDataSource
      LocalUserDataSource localUserDataSource = getLocalUserDataSource(context);

      // Lấy FirebaseAuthDataSource từ config
      FirebaseAuthDataSource authDataSource = FirebaseAuthConfig.getAuthDataSource();

      // Khởi tạo mapper
      UserMapper userMapper = new UserMapper();

      // Tạo và lưu trữ instance của UserRepository
      userRepository = new UserRepositoryImpl(
          firestoreUserDataSource,
          localUserDataSource,
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
    if (localUserDataSource == null) {
      // Tạo adapter/implementation của LocalUserDataSource sử dụng UserDao
      localUserDataSource = new LocalUserDataSourceImpl(
          AppDatabase.getInstance(context).userDao(),
          new UserMapper());
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

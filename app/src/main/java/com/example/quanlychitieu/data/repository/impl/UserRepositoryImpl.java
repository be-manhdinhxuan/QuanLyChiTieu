package com.example.quanlychitieu.data.repository.impl;

import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.quanlychitieu.data.datasource.firebase.auth.FirebaseAuthDataSource;
import com.example.quanlychitieu.data.datasource.firebase.firestore.FirestoreUserDataSource;
import com.example.quanlychitieu.data.datasource.local.datasource.LocalUserDataSource;
import com.example.quanlychitieu.data.mapper.UserMapper;
import com.example.quanlychitieu.data.datasource.local.entity.UserEntity;
import com.example.quanlychitieu.domain.model.user.User;
import com.example.quanlychitieu.domain.repository.UserRepository;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Implementation of UserRepository that combines remote (Firebase) and local
 * (Room) datasources
 */
public class UserRepositoryImpl implements UserRepository {
  private static final String TAG = "UserRepositoryImpl";

  private final FirestoreUserDataSource remoteDataSource;
  private final LocalUserDataSource localDataSource;
  private final FirebaseAuthDataSource authDataSource;
  private final UserMapper mapper;
  private final Executor executor;

  public UserRepositoryImpl(
      FirestoreUserDataSource remoteDataSource,
      LocalUserDataSource localDataSource,
      FirebaseAuthDataSource authDataSource,
      UserMapper mapper) {
    this.remoteDataSource = remoteDataSource;
    this.localDataSource = localDataSource;
    this.authDataSource = authDataSource;
    this.mapper = mapper;
    this.executor = Executors.newSingleThreadExecutor();
  }

  @Override
  public Task<User> getUser(String userId) {
    // Kiểm tra cache trong local database trước
    User localUser = localDataSource.getUser(userId);

    if (localUser != null) {
      // Nếu có dữ liệu trong cache, trả về ngay
      return Tasks.forResult(localUser);
    }

    // Nếu không có trong cache, lấy từ Firestore
    return remoteDataSource.getUser(userId)
        .continueWith(task -> {
          if (task.isSuccessful() && task.getResult() != null) {
            // Lưu kết quả vào cache
            User user = task.getResult();
            cacheUser(user, false);
            return user;
          } else {
            Log.e(TAG, "Error getting user", task.getException());
            throw task.getException();
          }
        });
  }

  @Override
  public Task<User> getCurrentUser() {
    // Kiểm tra trong local database trước
    User localCurrentUser = localDataSource.getCurrentUser();

    if (localCurrentUser != null) {
      // Có trong cache, trả về ngay
      return Tasks.forResult(localCurrentUser);
    }

    // Lấy user ID từ Firebase Auth
    FirebaseUser firebaseUser = authDataSource.getCurrentUser();
    if (firebaseUser == null) {
      return Tasks.forException(new Exception("No user is signed in"));
    }

    // Lấy thông tin user từ Firestore
    return remoteDataSource.getUser(firebaseUser.getUid())
        .continueWith(task -> {
          if (task.isSuccessful() && task.getResult() != null) {
            // Lưu vào cache và đánh dấu là current user
            User user = task.getResult();
            cacheUser(user, true);
            return user;
          } else {
            Log.e(TAG, "Error getting current user", task.getException());
            throw task.getException();
          }
        });
  }

  @Override
  public Task<Void> createUser(User user) {
    FirebaseUser firebaseUser = authDataSource.getCurrentUser();
    if (firebaseUser == null) {
      return Tasks.forException(new Exception("No user is signed in"));
    }

    // Đảm bảo ID và email được set
    user.setId(firebaseUser.getUid());
    if (user.getEmail() == null || user.getEmail().isEmpty()) {
      user.setEmail(firebaseUser.getEmail());
    }

    // Lưu vào Firestore sử dụng set
    return FirebaseFirestore.getInstance()
        .collection("users")
        .document(user.getId())
        .set(user)
        .addOnSuccessListener(aVoid -> {
          // Lưu vào local database
          cacheUser(user, true);
        });
  }

  @Override
  public Task<Void> updateUser(User user, Uri imageUri) {
    if (user == null) {
      return Tasks.forException(new IllegalArgumentException("User cannot be null"));
    }

    FirebaseUser firebaseUser = authDataSource.getCurrentUser();
    if (firebaseUser == null) {
      return Tasks.forException(new Exception("No user is signed in"));
    }

    // Đảm bảo user ID được set
    user.setId(firebaseUser.getUid());
    
    Map<String, Object> updates = new HashMap<>();
    updates.put("id", user.getId()); // Thêm ID vào updates
    updates.put("name", user.getName());
    updates.put("money", user.getMoney());
    updates.put("birthday", user.getBirthday());
    updates.put("gender", user.isGender());
    updates.put("avatar", user.getAvatar());
    updates.put("email", user.getEmail());
    
    return FirebaseFirestore.getInstance()
        .collection("users")
        .document(user.getId()) // Sử dụng ID làm document ID
        .set(updates) // Sử dụng set thay vì update
        .addOnSuccessListener(aVoid -> {
          // Cập nhật cache local
          cacheUser(user, true);
        });
  }

  private Task<Void> updateUserInfo(String userId, User user) {
    // Cập nhật thông tin trên Firestore
    return remoteDataSource.updateUser(userId, user)
        .continueWith(task -> {
          if (task.isSuccessful()) {
            // Cập nhật trong local database
            cacheUser(user, isCurrentUser(userId));
          }
          return null;
        });
  }

  @Override
  public Task<Void> deleteUser() {
    FirebaseUser firebaseUser = authDataSource.getCurrentUser();
    if (firebaseUser == null) {
      return Tasks.forException(new Exception("No user is signed in"));
    }

    final String userId = firebaseUser.getUid();

    // Xóa dữ liệu user từ Firestore
    return remoteDataSource.deleteUser(userId)
        .continueWithTask(task -> {
          if (task.isSuccessful()) {
            // Xóa khỏi local database
            localDataSource.deleteUser(userId);

            // Xóa tài khoản Firebase Auth
            return firebaseUser.delete();
          } else {
            throw task.getException();
          }
        });
  }

  @Override
  public Task<Void> updateUserMoney(int money) {
    FirebaseUser firebaseUser = authDataSource.getCurrentUser();
    if (firebaseUser == null) {
      return Tasks.forException(new Exception("No user is signed in"));
    }

    final String userId = firebaseUser.getUid();

    // Cập nhật tiền trên Firestore
    return remoteDataSource.updateUserMoney(userId, money)
        .continueWith(task -> {
          if (task.isSuccessful()) {
            // Cập nhật trong local database
            executor.execute(() -> {
              User user = localDataSource.getUser(userId);
              if (user != null) {
                user.setMoney(money);
                localDataSource.updateUser(user);
              }
            });
          }
          return null;
        });
  }

  @Override
  public Task<Boolean> isEmailExists(String email) {
    // Chỉ kiểm tra trên Firestore vì đây là thông tin quan trọng cần chính xác
    return Tasks.call(executor, () -> {
      try {
        // Thay thế phương thức không tồn tại bằng phương thức trong FirebaseAuth
        FirebaseAuth auth = FirebaseAuth.getInstance();
        boolean exists = !auth.fetchSignInMethodsForEmail(email).getResult().getSignInMethods().isEmpty();
        return exists;
      } catch (Exception e) {
        Log.e(TAG, "Error checking email existence", e);
        return false;
      }
    });
  }

  /**
   * Lưu thông tin user vào local database (cache)
   */
  private void cacheUser(User user, boolean isCurrentUser) {
    if (user == null || user.getId() == null) {
      Log.e(TAG, "Cannot cache user: user or user ID is null");
      return;
    }

    executor.execute(() -> {
      try {
        // Kiểm tra xem user đã tồn tại chưa
        User existingUser = localDataSource.getUser(user.getId());
        
        if (existingUser != null) {
          // Update existing user
          localDataSource.updateUser(user);
        } else {
          // Insert new user
          localDataSource.saveUser(user);
        }

        if (isCurrentUser) {
          localDataSource.setCurrentUser(user.getId());
        }
      } catch (Exception e) {
        Log.e(TAG, "Error caching user", e);
      }
    });
  }

  /**
   * Kiểm tra xem userId có phải là current user không
   */
  private boolean isCurrentUser(String userId) {
    FirebaseUser currentUser = authDataSource.getCurrentUser();
    return currentUser != null && currentUser.getUid().equals(userId);
  }
}

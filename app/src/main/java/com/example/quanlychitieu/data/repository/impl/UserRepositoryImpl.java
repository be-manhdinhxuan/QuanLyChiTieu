package com.example.quanlychitieu.data.repository.impl;

import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.quanlychitieu.data.datasource.firebase.auth.FirebaseAuthDataSource;
import com.example.quanlychitieu.data.datasource.firebase.firestore.FirestoreUserDataSource; // Import interface
import com.example.quanlychitieu.data.datasource.local.datasource.LocalUserDataSource; // Import interface/class
import com.example.quanlychitieu.data.mapper.UserMapper;
// import com.example.quanlychitieu.data.datasource.local.entity.UserEntity; // Bỏ nếu không dùng trực tiếp
import com.example.quanlychitieu.domain.model.user.User; // Import User model
import com.example.quanlychitieu.domain.repository.UserRepository;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth; // Chỉ dùng nếu cần instance trực tiếp
import com.google.firebase.auth.FirebaseUser;
// import com.google.firebase.firestore.FirebaseFirestore; // Bỏ nếu dùng DataSource

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class UserRepositoryImpl implements UserRepository {
  private static final String TAG = "UserRepositoryImpl";

  private final FirestoreUserDataSource remoteDataSource; // DataSource cho Firestore
  private final LocalUserDataSource localDataSource; // DataSource cho Local DB (Room)
  private final FirebaseAuthDataSource authDataSource; // DataSource cho Auth
  private final UserMapper mapper; // Mapper nếu cần chuyển đổi giữa User và UserEntity
  private final Executor executor; // Executor cho tác vụ local

  @Inject
  public UserRepositoryImpl(
          FirestoreUserDataSource remoteDataSource,
          LocalUserDataSource localDataSource, // Có thể là null nếu không dùng cache
          FirebaseAuthDataSource authDataSource,
          UserMapper mapper) {
    this.remoteDataSource = remoteDataSource;
    this.localDataSource = localDataSource;
    this.authDataSource = authDataSource;
    this.mapper = mapper;
    this.executor = Executors.newSingleThreadExecutor();
  }

  // Bỏ hàm getUser nếu đã có getUserById
    /*
    @Override
    public Task<User> getUser(String userId) {
        // ... logic cũ ...
    }
    */

  @Override
  public Task<User> getCurrentUser() {
    FirebaseUser firebaseUser = authDataSource.getCurrentUser();
    if (firebaseUser == null) {
      Log.w(TAG, "getCurrentUser: No user signed in.");
      return Tasks.forException(new Exception("No user is signed in"));
    }
    final String userId = firebaseUser.getUid();
    Log.d(TAG, "getCurrentUser: Fetching user data for ID: " + userId);

    // Thử lấy từ cache trước (nếu có localDataSource)
    if (localDataSource != null) {
      return Tasks.call(executor, () -> localDataSource.getUser(userId))
              .continueWithTask(task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                  Log.d(TAG, "getCurrentUser: Found user in local cache.");
                  return Tasks.forResult(task.getResult());
                }
                // Không có trong cache hoặc lỗi -> lấy từ remote
                Log.d(TAG, "getCurrentUser: Not found in cache or error, fetching from remote.");
                return fetchAndCacheUserFromRemote(userId, true); // true vì là current user
              });
    } else {
      // Không dùng cache, lấy thẳng từ remote
      Log.d(TAG, "getCurrentUser: No local cache, fetching directly from remote.");
      return remoteDataSource.getUser(userId); // Giả sử remoteDataSource trả về Task<User>
    }
  }

  // Hàm helper để lấy từ remote và cache
  private Task<User> fetchAndCacheUserFromRemote(String userId, boolean isCurrentUser) {
    return remoteDataSource.getUser(userId)
            .continueWith(task -> {
              if (task.isSuccessful() && task.getResult() != null) {
                User user = task.getResult();
                // ID đã được gán bởi remoteDataSource (ví dụ: dùng toObject với @DocumentId)
                Log.d(TAG, "fetchAndCacheUserFromRemote: Fetched user " + userId + " successfully.");
                if (localDataSource != null) {
                  cacheUser(user, isCurrentUser); // Cache kết quả
                }
                return user;
              } else {
                Log.e(TAG, "fetchAndCacheUserFromRemote: Error getting user " + userId, task.getException());
                throw task.getException() != null ? task.getException() : new Exception("Failed to fetch user from remote");
              }
            });
  }


  // *** SỬA LẠI HÀM NÀY ***
  @Override
  public Task<User> createUser(User user) {
    // Không cần kiểm tra user đăng nhập ở đây vì đang tạo user mới (có thể là chưa đăng ký)
    if (user == null) {
      Log.w(TAG, "createUser: User object is null.");
      return Tasks.forException(new IllegalArgumentException("User object cannot be null"));
    }

    // Đảm bảo các trường mặc định được đặt nếu cần
    if (user.getAvatar() == null || user.getAvatar().isEmpty()) {
      user.setAvatar(User.DEFAULT_AVATAR);
    }
    // Không cần set ID ở đây, remoteDataSource sẽ xử lý (tạo ID mới)
    Log.d(TAG, "Creating user: Name=" + user.getName() + ", Email=" + user.getEmail() + ", Registered=" + user.isRegistered());

    // Gọi DataSource để tạo user trên Firestore
    // Giả định remoteDataSource.createUser trả về Task<User> (chứa User đã được tạo với ID mới)
    // Hoặc trả về Task<String> (chỉ ID mới), khi đó cần gọi getUserById để lấy lại User object đầy đủ
    // *** Giả định remoteDataSource.createUser trả về Task<User> ***
    return remoteDataSource.createUser(user) // Hàm này cần được tạo/sửa trong DataSource
            .continueWith(task -> {
              if (task.isSuccessful() && task.getResult() != null) {
                User createdUser = task.getResult();
                // ID đã được gán bởi remoteDataSource
                Log.d(TAG, "Successfully created user in remote, ID: " + createdUser.getId());
                // Cache user mới vào local nếu cần
                if (localDataSource != null) {
                  // Kiểm tra xem user này có phải là user hiện tại không (thường là không khi tạo user chưa đăng ký)
                  boolean isCurrentUser = isCurrentUser(createdUser.getId());
                  cacheUser(createdUser, isCurrentUser);
                }
                return createdUser; // Trả về User object đã có ID
              } else {
                Log.e(TAG, "Error creating user in remote data source", task.getException());
                throw task.getException() != null ? task.getException() : new Exception("Failed to create user");
              }
            });
  }

  @Override
  public Task<Void> updateUser(User user, Uri imageUri) {
    FirebaseUser firebaseUser = authDataSource.getCurrentUser();
    if (firebaseUser == null) {
      Log.w(TAG, "updateUser: No user signed in.");
      return Tasks.forException(new Exception("No user is signed in"));
    }
    if (user == null) {
      Log.w(TAG, "updateUser: User object is null.");
      return Tasks.forException(new IllegalArgumentException("User cannot be null"));
    }
    // Đảm bảo ID của user cần update khớp với user đang đăng nhập
    // Hoặc nếu logic cho phép sửa user khác (admin?), thì cần kiểm tra quyền
    if (user.getId() == null || !user.getId().equals(firebaseUser.getUid())) {
      Log.e(TAG, "updateUser: Attempting to update user with mismatched ID or null ID.");
      return Tasks.forException(new SecurityException("Cannot update another user's profile or user with null ID."));
    }
    Log.d(TAG, "Updating user ID: " + user.getId());

    // Logic xử lý ảnh (upload nếu có imageUri mới)
    Task<String> uploadTask = Tasks.forResult(user.getAvatar()); // Mặc định là avatar hiện tại
    if (imageUri != null) {
      Log.d(TAG, "updateUser: New image URI provided, starting upload...");
      // Giả sử remoteDataSource có hàm upload trả về Task<String> (URL mới)
      uploadTask = remoteDataSource.uploadUserAvatar(user.getId(), imageUri);
    }

    // Chờ upload ảnh xong (nếu có) rồi mới cập nhật thông tin user
    return uploadTask.continueWithTask(task -> {
      if (!task.isSuccessful()) {
        Log.e(TAG, "updateUser: Image upload failed (if attempted).", task.getException());
        // Quyết định: dừng lại hay vẫn cập nhật thông tin text?
        // Hiện tại: dừng lại nếu upload lỗi
        throw task.getException() != null ? task.getException() : new Exception("Image upload failed");
      }
      String finalImageUrl = task.getResult(); // URL ảnh mới hoặc URL cũ
      user.setAvatar(finalImageUrl); // Cập nhật avatar trong object user
      Log.d(TAG, "updateUser: Image handled (URL: " + finalImageUrl + "). Proceeding to update user data.");

      // Cập nhật thông tin user trên Firestore
      return remoteDataSource.updateUser(user.getId(), user) // Giả sử hàm này nhận ID và User object
              .continueWith(updateDataTask -> {
                if (updateDataTask.isSuccessful()) {
                  Log.d(TAG, "updateUser: User data updated successfully on remote.");
                  // Cập nhật cache local
                  if (localDataSource != null) {
                    cacheUser(user, true); // Cập nhật user hiện tại
                  }
                  return null; // Task<Void> thành công
                } else {
                  Log.e(TAG, "updateUser: Failed to update user data on remote.", updateDataTask.getException());
                  throw updateDataTask.getException() != null ? updateDataTask.getException() : new Exception("Failed to update user data");
                }
              });
    });
  }

  // Bỏ hàm updateUserInfo vì logic đã gộp vào updateUser

  @Override
  public Task<Void> deleteUser() {
    FirebaseUser firebaseUser = authDataSource.getCurrentUser();
    if (firebaseUser == null) {
      Log.w(TAG, "deleteUser: No user signed in.");
      return Tasks.forException(new Exception("No user is signed in"));
    }
    final String userId = firebaseUser.getUid();
    Log.d(TAG, "Deleting user ID: " + userId);

    // Bước 1: Xóa dữ liệu user từ Firestore
    return remoteDataSource.deleteUser(userId)
            .continueWithTask(deleteRemoteTask -> {
              if (!deleteRemoteTask.isSuccessful()) {
                Log.e(TAG, "deleteUser: Failed to delete remote user data.", deleteRemoteTask.getException());
                // Quyết định: dừng hay vẫn thử xóa local và auth?
                // Hiện tại: dừng nếu xóa remote lỗi
                throw deleteRemoteTask.getException() != null ? deleteRemoteTask.getException() : new Exception("Failed to delete remote user data");
              }
              Log.d(TAG, "deleteUser: Remote user data deleted successfully.");
              // Bước 2: Xóa khỏi local database (nếu dùng)
              if (localDataSource != null) {
                executor.execute(() -> localDataSource.deleteUser(userId));
                Log.d(TAG, "deleteUser: Local user data deletion scheduled.");
              }
              // Bước 3: Xóa tài khoản Firebase Auth
              Log.d(TAG, "deleteUser: Deleting Firebase Auth account.");
              return firebaseUser.delete(); // Trả về Task của việc xóa Auth
            }).continueWith(finalDeleteTask -> {
              if (!finalDeleteTask.isSuccessful()) {
                Log.e(TAG, "deleteUser: Failed to delete Firebase Auth account.", finalDeleteTask.getException());
                throw finalDeleteTask.getException() != null ? finalDeleteTask.getException() : new Exception("Failed to delete Firebase Auth account");
              }
              Log.d(TAG, "deleteUser: Firebase Auth account deleted successfully.");
              return null; // Task<Void> thành công
            });
  }

  @Override
  public Task<Void> updateUserMoney(int money) {
    FirebaseUser firebaseUser = authDataSource.getCurrentUser();
    if (firebaseUser == null) {
      Log.w(TAG, "updateUserMoney: No user signed in.");
      return Tasks.forException(new Exception("No user is signed in"));
    }
    final String userId = firebaseUser.getUid();
    Log.d(TAG, "Updating money for user " + userId + " to " + money);

    // Cập nhật tiền trên Firestore
    // Giả sử remoteDataSource có hàm này
    return remoteDataSource.updateUserMoney(userId, money)
            .continueWith(task -> {
              if (task.isSuccessful()) {
                Log.d(TAG, "updateUserMoney: Remote update successful.");
                // Cập nhật trong local database (nếu dùng)
                if (localDataSource != null) {
                  executor.execute(() -> {
                    // Lấy user từ cache và cập nhật tiền
                    User user = localDataSource.getUser(userId);
                    if (user != null) {
                      user.setMoney(money);
                      localDataSource.updateUser(user); // Giả sử có hàm update
                      Log.d(TAG, "updateUserMoney: Local cache updated.");
                    } else {
                      Log.w(TAG, "updateUserMoney: User not found in local cache to update money.");
                    }
                  });
                }
                return null; // Task<Void> thành công
              } else {
                Log.e(TAG, "updateUserMoney: Error updating money on remote.", task.getException());
                throw task.getException() != null ? task.getException() : new Exception("Failed to update user money");
              }
            });
  }

  @Override
  public Task<Boolean> isEmailExists(String email) {
    if (email == null || email.isEmpty()) {
      return Tasks.forResult(false); // Email rỗng không được coi là tồn tại
    }
    Log.d(TAG, "Checking if email exists: " + email);
    // Nên kiểm tra trên Firestore để có kết quả chính xác nhất
    // Giả sử remoteDataSource có hàm này
    return remoteDataSource.isEmailExists(email);
        /*
        // Hoặc dùng FirebaseAuth như code cũ (nhưng có thể không chính xác nếu user tạo bằng cách khác)
        return Tasks.call(executor, () -> {
            try {
                FirebaseAuth auth = FirebaseAuth.getInstance();
                // fetchSignInMethodsForEmail trả về list các provider đã dùng email này
                boolean exists = !Tasks.await(auth.fetchSignInMethodsForEmail(email)).getSignInMethods().isEmpty();
                Log.d(TAG, "Email " + email + " exists check result: " + exists);
                return exists;
            } catch (Exception e) {
                Log.e(TAG, "Error checking email existence via FirebaseAuth", e);
                // Trả về false khi có lỗi để tránh chặn luồng đăng ký/thêm bạn sai
                return false;
            }
        });
        */
  }

  @Override
  public Task<User> getUserById(String userId) {
    if (userId == null || userId.isEmpty()) {
      Log.w(TAG, "getUserById: userId is null or empty.");
      return Tasks.forException(new IllegalArgumentException("User ID cannot be null or empty"));
    }
    Log.d(TAG, "getUserById: " + userId);

    // Thử lấy từ cache trước
    if (localDataSource != null) {
      return Tasks.call(executor, () -> localDataSource.getUser(userId))
              .continueWithTask(task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                  Log.d(TAG, "getUserById: Found user " + userId + " in local cache.");
                  return Tasks.forResult(task.getResult());
                }
                Log.d(TAG, "getUserById: User " + userId + " not in cache or error, fetching from remote.");
                // Lấy từ remote và cache lại
                return fetchAndCacheUserFromRemote(userId, isCurrentUser(userId));
              });
    } else {
      // Không có cache, lấy thẳng từ remote
      Log.d(TAG, "getUserById: No local cache, fetching directly from remote for " + userId);
      return remoteDataSource.getUser(userId); // Giả sử remoteDataSource trả về Task<User>
    }
  }


  /**
   * Lưu hoặc cập nhật thông tin user vào local database (cache).
   * @param user User object cần cache (phải có ID).
   * @param isCurrentUser Đánh dấu user này có phải là user đang đăng nhập không.
   */
  private void cacheUser(User user, boolean isCurrentUser) {
    if (localDataSource == null) return; // Không cache nếu không có local source
    if (user == null) {
      Log.e(TAG, "Cannot cache user: user is null");
      return;
    }
    if (user.getId() == null || user.getId().isEmpty()) {
      Log.e(TAG, "Cannot cache user: user ID is null/empty");
      return;
    }

    executor.execute(() -> {
      try {
        Log.d(TAG, "Caching user ID: " + user.getId() + ", isCurrent: " + isCurrentUser);
        // Kiểm tra xem user đã tồn tại chưa để quyết định insert hay update
        User existingUser = localDataSource.getUser(user.getId());
        if (existingUser != null) {
          Log.d(TAG, "Updating existing user in cache: " + user.getId());
          localDataSource.updateUser(user); // Giả sử có hàm update
        } else {
          Log.d(TAG, "Inserting new user into cache: " + user.getId());
          long result = localDataSource.saveUser(user); // Giả sử có hàm save/insert
          if (result == -1) {
            Log.e(TAG, "Failed to insert user into local database: " + user.getId());
            return;
          }
        }

        // Nếu là user hiện tại, cập nhật trong local source
        if (isCurrentUser) {
          localDataSource.setCurrentUser(user.getId()); // Giả sử có hàm này
          Log.d(TAG, "Set user " + user.getId() + " as current user in cache.");
        }
      } catch (Exception e) {
        Log.e(TAG, "Error caching user: " + e.getMessage(), e);
      }
    });
  }

  /**
   * Kiểm tra xem userId có phải là của user đang đăng nhập không.
   */
  private boolean isCurrentUser(String userId) {
    FirebaseUser currentUser = authDataSource.getCurrentUser();
    return currentUser != null && userId != null && currentUser.getUid().equals(userId);
  }
}

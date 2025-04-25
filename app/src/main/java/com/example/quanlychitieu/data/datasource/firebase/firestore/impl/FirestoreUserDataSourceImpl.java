package com.example.quanlychitieu.data.datasource.firebase.firestore.impl;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.example.quanlychitieu.core.utils.CloudinaryManager;
import com.example.quanlychitieu.data.datasource.firebase.firestore.FirestoreConstants;
import com.example.quanlychitieu.data.datasource.firebase.firestore.FirestoreUserDataSource;
import com.example.quanlychitieu.domain.model.user.User;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.android.gms.tasks.TaskCompletionSource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Implementation of FirestoreUserDataSource
 * Handles all user-related Firestore operations
 */
public class FirestoreUserDataSourceImpl implements FirestoreUserDataSource {
  private static final String TAG = "FirestoreUserDataSource";
  private static final String AVATAR_STORAGE_PATH = "avatars/";

  private final Context context;
  private final FirebaseFirestore firestore;
  private final FirebaseStorage storage;
  private final FirebaseAuth auth;
  private final CollectionReference usersCollection;

  public FirestoreUserDataSourceImpl(Context context) {
    this.context = context;
    this.firestore = FirebaseFirestore.getInstance();
    this.storage = FirebaseStorage.getInstance();
    this.auth = FirebaseAuth.getInstance();
    this.usersCollection = firestore.collection(FirestoreConstants.COLLECTION_USERS);
  }

  @Override
  public Task<Void> createUser(String userId, User user) {
    Map<String, Object> userData = new HashMap<>();
    userData.put("name", user.getName());
    userData.put("birthday", user.getBirthday());
    userData.put("avatar", user.getAvatar());
    userData.put("gender", user.isGender());
    userData.put("money", user.getMoney());
    userData.put(FirestoreConstants.FIELD_CREATED_AT, System.currentTimeMillis());
    userData.put(FirestoreConstants.FIELD_UPDATED_AT, System.currentTimeMillis());

    return usersCollection.document(userId).set(userData);
  }

  @Override
  public Task<User> getUser(String userId) {
    return usersCollection.document(userId).get()
        .continueWith(task -> {
          if (!task.isSuccessful()) {
            throw task.getException();
          }

          DocumentSnapshot document = task.getResult();
          if (document == null || !document.exists()) {
            return new User(); // Return default user
          }

          return User.fromFirebase(document);
        });
  }

  @Override
  public Task<Void> updateUser(String userId, User user) {
    // Directly use the toMap method from User class
    return usersCollection.document(userId).update(user.toMap());
  }

  @Override
  public Task<String> uploadUserAvatar(String userId, Uri imageUri) {
    if (imageUri == null) {
      return Tasks.forException(new IllegalArgumentException("Image URI cannot be null"));
    }

    TaskCompletionSource<String> taskCompletionSource = new TaskCompletionSource<>();

    CloudinaryManager.getInstance(context)
        .uploadImage(
            context,
            imageUri,
            "avatars/" + userId,
            new CloudinaryManager.UploadCallback() {
                @Override
                public void onSuccess(String imageUrl) {
                    taskCompletionSource.setResult(imageUrl);
                }

                @Override
                public void onError(Exception e) {
                    taskCompletionSource.setException(e);
                }
            });

    return taskCompletionSource.getTask();
  }

  @Override
  public Task<Void> updateUserAvatar(String userId, String avatarUrl) {
    return usersCollection.document(userId)
        .update("avatar", avatarUrl,
            FirestoreConstants.FIELD_UPDATED_AT, System.currentTimeMillis());
  }

  @Override
  public Task<Void> updateUserMoney(String userId, int amount) {
    return usersCollection.document(userId)
        .update("money", amount,
            FirestoreConstants.FIELD_UPDATED_AT, System.currentTimeMillis());
  }

  @Override
  public Task<Void> deleteUser(String userId) {
    // Xóa dữ liệu người dùng từ Firestore
    return usersCollection.document(userId).delete()
        .continueWithTask(task -> {
          if (!task.isSuccessful()) {
            throw task.getException();
          }

          // Xóa các tài liệu liên quan đến người dùng
          return deleteUserRelatedData(userId);
        });
  }

  /**
   * Xóa các dữ liệu liên quan đến người dùng như chi tiêu, ngân sách, v.v.
   */
  private Task<Void> deleteUserRelatedData(String userId) {
    // Xóa dữ liệu chi tiêu
    Task<Void> deleteSpendingTask = firestore.collection("spending")
        .whereEqualTo("userId", userId)
        .get()
        .continueWithTask(task -> {
          if (!task.isSuccessful()) {
            return Tasks.forResult(null);
          }

          List<Task<Void>> deleteTasks = new ArrayList<>();
          for (DocumentSnapshot doc : task.getResult().getDocuments()) {
            deleteTasks.add(doc.getReference().delete());
          }

          return Tasks.whenAll(deleteTasks);
        });

    // Xóa dữ liệu ngân sách
    Task<Void> deleteBudgetTask = firestore.collection("budget")
        .whereEqualTo("userId", userId)
        .get()
        .continueWithTask(task -> {
          if (!task.isSuccessful()) {
            return Tasks.forResult(null);
          }

          List<Task<Void>> deleteTasks = new ArrayList<>();
          for (DocumentSnapshot doc : task.getResult().getDocuments()) {
            deleteTasks.add(doc.getReference().delete());
          }

          return Tasks.whenAll(deleteTasks);
        });

    // Xóa dữ liệu hệ thống khác
    Task<Void> deleteDataTask = firestore.collection("data")
        .document(userId)
        .delete();

    // Xóa dữ liệu ví
    Task<Void> deleteWalletTask = firestore.collection("wallet")
        .document(userId)
        .delete();

    // Xóa dữ liệu thông tin
    Task<Void> deleteInfoTask = firestore.collection("info")
        .document(userId)
        .delete();

    // Kết hợp tất cả các tác vụ xóa
    return Tasks.whenAll(
        deleteSpendingTask,
        deleteBudgetTask,
        deleteDataTask,
        deleteWalletTask,
        deleteInfoTask);
  }

  @Override
  public Task<User> getCurrentUser() {
    String userId = auth.getCurrentUser().getUid();
    return getUser(userId);
  }
}

package com.example.quanlychitieu.domain.repository;

import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.quanlychitieu.domain.model.user.User;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class UserRepository {
    private static final String TAG = "UserRepository";
    private static final String COLLECTION_USERS = "users";

    private final FirebaseFirestore firestore;
    private final FirebaseStorage storage;

    public UserRepository(FirebaseFirestore firestore) {
        this.firestore = firestore;
        this.storage = FirebaseStorage.getInstance();
    }

    /**
     * Lấy thông tin người dùng từ Firestore theo userId
     */
    public Task<User> getUser(String userId) {
        return firestore.collection(COLLECTION_USERS)
                .document(userId)
                .get()
                .continueWith(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            return User.fromFirebase(document);
                        } else {
                            Log.d(TAG, "User not found: " + userId);
                            throw new Exception("User not found");
                        }
                    } else {
                        Log.e(TAG, "Error getting user", task.getException());
                        throw task.getException();
                    }
                });
    }

    /**
     * Lấy thông tin người dùng hiện tại
     */
    public Task<User> getCurrentUser() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            return Tasks.forException(new Exception("No user is signed in"));
        }
        return getUser(currentUser.getUid());
    }

    /**
     * Tạo người dùng mới
     */
    public Task<Void> createUser(User user) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            return Tasks.forException(new Exception("No user is signed in"));
        }

        return firestore.collection(COLLECTION_USERS)
                .document(currentUser.getUid())
                .set(user.toMap());
    }

    /**
     * Cập nhật thông tin người dùng
     */
    public Task<Void> updateUser(User user, Uri imageUri) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            return Tasks.forException(new Exception("No user is signed in"));
        }

        // Nếu không có ảnh mới, chỉ cập nhật thông tin
        if (imageUri == null) {
            return updateUserInfo(currentUser.getUid(), user);
        }

        // Nếu có ảnh mới, upload ảnh trước rồi cập nhật thông tin
        return uploadImage(imageUri, currentUser.getUid())
                .continueWithTask(uploadTask -> {
                    if (uploadTask.isSuccessful() && uploadTask.getResult() != null) {
                        // Lấy URL của ảnh đã upload
                        return uploadTask.getResult().getStorage().getDownloadUrl();
                    } else {
                        throw uploadTask.getException();
                    }
                })
                .continueWithTask(urlTask -> {
                    if (urlTask.isSuccessful() && urlTask.getResult() != null) {
                        // Cập nhật URL ảnh vào user
                        user.setAvatar(urlTask.getResult().toString());
                        return updateUserInfo(currentUser.getUid(), user);
                    } else {
                        throw urlTask.getException();
                    }
                });
    }

    /**
     * Cập nhật thông tin người dùng không bao gồm ảnh
     */
    private Task<Void> updateUserInfo(String userId, User user) {
        return firestore.collection(COLLECTION_USERS)
                .document(userId)
                .update(user.toMap());
    }

    /**
     * Upload ảnh lên Firebase Storage
     */
    private Task<UploadTask.TaskSnapshot> uploadImage(Uri imageUri, String userId) {
        String fileName = UUID.randomUUID().toString();
        StorageReference storageRef = storage.getReference()
                .child("profile_images")
                .child(userId)
                .child(fileName);

        return storageRef.putFile(imageUri);
    }

    /**
     * Xóa tài khoản người dùng
     */
    public Task<Void> deleteUser() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            return Tasks.forException(new Exception("No user is signed in"));
        }

        // Xóa dữ liệu người dùng trên Firestore
        Task<Void> deleteUserDataTask = firestore.collection(COLLECTION_USERS)
                .document(currentUser.getUid())
                .delete();

        // Xóa tài khoản Authentication
        Task<Void> deleteAuthTask = currentUser.delete();

        // Thực hiện song song cả hai task
        return Tasks.whenAll(deleteUserDataTask, deleteAuthTask);
    }

    /**
     * Cập nhật tiền hàng tháng của người dùng
     */
    public Task<Void> updateUserMoney(int money) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            return Tasks.forException(new Exception("No user is signed in"));
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("money", money);

        return firestore.collection(COLLECTION_USERS)
                .document(currentUser.getUid())
                .update(updates);
    }

    /**
     * Kiểm tra xem email đã tồn tại chưa
     */
    public Task<Boolean> isEmailExists(String email) {
        return firestore.collection(COLLECTION_USERS)
                .whereEqualTo("email", email)
                .get()
                .continueWith(task -> {
                    if (task.isSuccessful()) {
                        return !task.getResult().isEmpty();
                    } else {
                        throw task.getException();
                    }
                });
    }
}
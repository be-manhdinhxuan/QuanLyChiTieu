package com.example.quanlychitieu.data.datasource.firebase.firestore.impl;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.example.quanlychitieu.core.utils.CloudinaryManager; // Import CloudinaryManager nếu dùng
import com.example.quanlychitieu.data.datasource.firebase.firestore.FirestoreConstants;
import com.example.quanlychitieu.data.datasource.firebase.firestore.FirestoreUserDataSource;
import com.example.quanlychitieu.domain.model.user.User; // Đảm bảo import đúng model User
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth; // Import FirebaseAuth nếu cần trực tiếp
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot; // Import QuerySnapshot
import com.google.firebase.storage.FirebaseStorage; // Import nếu dùng Firebase Storage
import com.google.firebase.storage.StorageReference; // Import nếu dùng Firebase Storage
import com.google.android.gms.tasks.TaskCompletionSource;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import dagger.hilt.android.qualifiers.ApplicationContext; // Import ApplicationContext

/**
 * Implementation of FirestoreUserDataSource
 * Handles all user-related Firestore operations
 */
@Singleton
public class FirestoreUserDataSourceImpl implements FirestoreUserDataSource {
    private static final String TAG = "FirestoreUserDataSource";
    // Bỏ AVATAR_STORAGE_PATH nếu dùng Cloudinary
    // private static final String AVATAR_STORAGE_PATH = "avatars/";

    private final Context context; // Context cần cho CloudinaryManager
    private final FirebaseFirestore firestore;
    // Bỏ storage nếu dùng Cloudinary
    // private final FirebaseStorage storage;
    // Bỏ auth nếu không dùng trực tiếp
    // private final FirebaseAuth auth;
    private final CollectionReference usersCollection;

    @Inject
    public FirestoreUserDataSourceImpl(@ApplicationContext Context context, // Inject ApplicationContext
                                       FirebaseFirestore firestore // Inject Firestore
            /*, FirebaseStorage storage, FirebaseAuth auth */) { // Bỏ inject nếu không dùng
        this.context = context;
        this.firestore = firestore;
        // this.storage = storage;
        // this.auth = auth;
        this.usersCollection = firestore.collection(FirestoreConstants.COLLECTION_USERS);
    }

    // *** SỬA LẠI HÀM NÀY ***
    @Override
    public Task<User> createUser(User user) {
        if (user == null) {
            return Tasks.forException(new IllegalArgumentException("User object cannot be null"));
        }

        // Tạo document mới để lấy ID tự động
        DocumentReference newUserRef = usersCollection.document();
        String newUserId = newUserRef.getId();

        // Gán ID mới được tạo vào đối tượng User
        user.setId(newUserId);

        // Đảm bảo các trường mặc định hoặc cần thiết khác được đặt
        if (user.getAvatar() == null || user.getAvatar().isEmpty()) {
            user.setAvatar(User.DEFAULT_AVATAR);
        }
        // Đặt thời gian tạo/cập nhật nếu cần (có thể dùng ServerTimestamp)
        // user.setCreatedAt(System.currentTimeMillis());
        // user.setUpdatedAt(System.currentTimeMillis());

        Log.d(TAG, "Creating user with generated ID: " + newUserId);
        // Lấy dữ liệu từ User object để lưu (dùng toMap hoặc để Firestore tự xử lý)
        // Map<String, Object> userData = user.toMap(); // Nếu User có toMap() và bạn muốn dùng Map

        // Lưu user object vào Firestore bằng ID mới tạo
        // Firestore sẽ tự động chuyển đổi User object thành Map nếu các getter/setter đúng chuẩn
        return newUserRef.set(user) // Truyền trực tiếp User object
                .continueWith(task -> {
                    if (!task.isSuccessful()) {
                        Log.e(TAG, "Error creating user document with ID: " + newUserId, task.getException());
                        throw task.getException() != null ? task.getException() : new Exception("Failed to set user document");
                    }
                    Log.d(TAG, "Successfully created user document with ID: " + newUserId);
                    // Trả về chính đối tượng User đã được gán ID
                    return user;
                });
    }

    @Override
    public Task<User> getUser(String userId) {
        if (userId == null || userId.isEmpty()) {
            return Tasks.forException(new IllegalArgumentException("User ID cannot be null or empty"));
        }
        Log.d(TAG, "Getting user with ID: " + userId);
        return usersCollection.document(userId).get()
                .continueWith(task -> {
                    if (!task.isSuccessful()) {
                        Log.e(TAG, "Error getting user document: " + userId, task.getException());
                        throw task.getException() != null ? task.getException() : new Exception("Failed to get user document");
                    }

                    DocumentSnapshot document = task.getResult();
                    if (document != null && document.exists()) {
                        // Dùng toObject để parse, ID sẽ được gán tự động nếu User có @DocumentId
                        // Hoặc dùng User.fromFirebase nếu bạn giữ phương thức đó và nó hoạt động đúng
                        User user = document.toObject(User.class); // Ưu tiên cách này nếu User model đúng chuẩn
                        // User user = User.fromFirebase(document); // Cách thay thế
                        if (user != null) {
                            // Nếu không dùng @DocumentId, cần gán ID thủ công
                            // user.setId(document.getId());
                            Log.d(TAG, "Successfully retrieved and parsed user: " + userId);
                            return user;
                        } else {
                            Log.w(TAG, "Failed to parse document to User object: " + userId);
                            // Trả về User rỗng hoặc lỗi tùy logic
                            return new User(); // Trả về User rỗng
                        }
                    } else {
                        Log.w(TAG, "User document not found: " + userId);
                        return null; // Hoặc trả về User rỗng: new User();
                    }
                });
    }

    @Override
    public Task<Void> updateUser(String userId, User user) {
        if (userId == null || userId.isEmpty()) {
            return Tasks.forException(new IllegalArgumentException("User ID cannot be null or empty for update"));
        }
        if (user == null) {
            return Tasks.forException(new IllegalArgumentException("User object cannot be null for update"));
        }
        Log.d(TAG, "Updating user ID: " + userId);
        // Sử dụng set với SetOptions.merge() để chỉ cập nhật các trường có trong Map
        // Hoặc dùng update() nếu bạn chắc chắn các trường cần cập nhật
        // Map<String, Object> updates = user.toMap(); // Lấy Map từ User (đảm bảo toMap() đúng)
        // return usersCollection.document(userId).set(updates, SetOptions.merge());

        // Hoặc truyền thẳng User object nếu Firestore hỗ trợ tốt cho model của bạn
        return usersCollection.document(userId).set(user, com.google.firebase.firestore.SetOptions.merge());
    }

    @Override
    public Task<String> uploadUserAvatar(String userId, Uri imageUri) {
        if (userId == null || userId.isEmpty() || imageUri == null) {
            return Tasks.forException(new IllegalArgumentException("User ID and Image URI cannot be null or empty"));
        }
        Log.d(TAG, "Uploading avatar for user: " + userId);

        TaskCompletionSource<String> taskCompletionSource = new TaskCompletionSource<>();

        // Sử dụng CloudinaryManager
        CloudinaryManager.getInstance(context)
                .uploadImage(
                        context,
                        imageUri,
                        "avatars/" + userId, // Path trên Cloudinary (ví dụ)
                        new CloudinaryManager.UploadCallback() {
                            @Override
                            public void onSuccess(String imageUrl) {
                                Log.d(TAG, "Avatar uploaded successfully to Cloudinary for user " + userId + ". URL: " + imageUrl);
                                // Sau khi upload thành công, cập nhật URL vào Firestore
                                updateUserAvatar(userId, imageUrl)
                                        .addOnSuccessListener(aVoid -> taskCompletionSource.setResult(imageUrl))
                                        .addOnFailureListener(e -> {
                                            Log.e(TAG, "Failed to update avatar URL in Firestore after upload", e);
                                            taskCompletionSource.setException(new Exception("Uploaded image but failed to update profile.", e));
                                        });
                            }

                            @Override
                            public void onError(Exception e) {
                                Log.e(TAG, "Failed to upload avatar to Cloudinary for user " + userId, e);
                                taskCompletionSource.setException(e);
                            }
                        });

        return taskCompletionSource.getTask();
    }

    @Override
    public Task<Void> updateUserAvatar(String userId, String avatarUrl) {
        if (userId == null || userId.isEmpty()) {
            return Tasks.forException(new IllegalArgumentException("User ID cannot be null or empty"));
        }
        Log.d(TAG, "Updating avatar URL for user " + userId + " to: " + avatarUrl);
        // Chỉ cập nhật trường avatar và thời gian cập nhật
        Map<String, Object> updates = new HashMap<>();
        updates.put("avatar", avatarUrl);
        updates.put(FirestoreConstants.FIELD_UPDATED_AT, System.currentTimeMillis()); // Hoặc ServerTimestamp

        return usersCollection.document(userId).update(updates);
    }

    @Override
    public Task<Void> updateUserMoney(String userId, int amount) {
        if (userId == null || userId.isEmpty()) {
            return Tasks.forException(new IllegalArgumentException("User ID cannot be null or empty"));
        }
        Log.d(TAG, "Updating money for user " + userId + " to: " + amount);
        // Chỉ cập nhật trường money và thời gian cập nhật
        Map<String, Object> updates = new HashMap<>();
        updates.put("money", amount);
        updates.put(FirestoreConstants.FIELD_UPDATED_AT, System.currentTimeMillis()); // Hoặc ServerTimestamp

        return usersCollection.document(userId).update(updates);
    }

    @Override
    public Task<Void> deleteUser(String userId) {
        if (userId == null || userId.isEmpty()) {
            return Tasks.forException(new IllegalArgumentException("User ID cannot be null or empty"));
        }
        Log.d(TAG, "Deleting user data for ID: " + userId);
        // Bước 1: Xóa document user trong collection 'users'
        return usersCollection.document(userId).delete()
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        Log.e(TAG, "Failed to delete user document: " + userId, task.getException());
                        throw task.getException() != null ? task.getException() : new Exception("Failed to delete user document");
                    }
                    Log.d(TAG, "Successfully deleted user document: " + userId + ". Now deleting related data.");
                    // Bước 2: Xóa các dữ liệu liên quan (spending, data, wallets, etc.)
                    return deleteUserRelatedData(userId);
                });
    }

    /**
     * Xóa các dữ liệu liên quan đến người dùng.
     * Lưu ý: Việc xóa nhiều collection dựa trên query có thể không hiệu quả
     * và có thể bị giới hạn bởi Firestore. Cân nhắc dùng Cloud Functions cho việc này.
     */
    private Task<Void> deleteUserRelatedData(String userId) {
        Log.d(TAG, "Deleting related data for user: " + userId);
        // Xóa dữ liệu chi tiêu (spending collection)
        Task<Void> deleteSpendingTask = deleteCollectionByUserId("spending", userId);

        // Xóa dữ liệu ngân sách (budget collection) - nếu có
        Task<Void> deleteBudgetTask = deleteCollectionByUserId("budget", userId);

        // Xóa document trong data collection
        Task<Void> deleteDataTask = firestore.collection("spending").document(userId).delete();

        // Xóa document trong wallet collection (sửa tên collection nếu khác)
        Task<Void> deleteWalletTask = firestore.collection("wallets").document(userId).delete(); // Giả sử là 'wallets'

        // Xóa document trong info collection (nếu có)
        Task<Void> deleteInfoTask = firestore.collection("info").document(userId).delete();

        // Kết hợp tất cả các tác vụ xóa
        return Tasks.whenAll(
                        deleteSpendingTask,
                        deleteBudgetTask,
                        deleteDataTask,
                        deleteWalletTask,
                        deleteInfoTask)
                .continueWith(task -> {
                    if (!task.isSuccessful()) {
                        Log.e(TAG, "Error occurred during related data deletion for user: " + userId, task.getException());
                        // Có thể throw lỗi hoặc chỉ log tùy yêu cầu
                    } else {
                        Log.d(TAG, "Successfully deleted related data for user: " + userId);
                    }
                    return null; // Task<Void>
                });
    }

    // Hàm helper để xóa tất cả document trong collection dựa trên userId
    private Task<Void> deleteCollectionByUserId(String collectionName, String userId) {
        Log.d(TAG, "Deleting documents in collection '" + collectionName + "' for user: " + userId);
        return firestore.collection(collectionName)
                .whereEqualTo("userId", userId) // Giả sử các collection này có trường userId
                .get()
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        Log.e(TAG, "Error querying collection '" + collectionName + "' for deletion.", task.getException());
                        // Trả về lỗi hoặc task thành công để không chặn các bước xóa khác
                        return Tasks.forResult(null);
                    }
                    QuerySnapshot snapshot = task.getResult();
                    if (snapshot == null || snapshot.isEmpty()) {
                        Log.d(TAG, "No documents found in '" + collectionName + "' for user " + userId + " to delete.");
                        return Tasks.forResult(null); // Không có gì để xóa
                    }

                    List<Task<Void>> deleteTasks = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        deleteTasks.add(doc.getReference().delete());
                    }
                    Log.d(TAG, "Deleting " + deleteTasks.size() + " documents from '" + collectionName + "' for user " + userId);
                    return Tasks.whenAll(deleteTasks); // Trả về task gộp các lệnh xóa
                });
    }


    @Override
    public Task<Boolean> isEmailExists(String email) {
        if (email == null || email.isEmpty()) {
            return Tasks.forResult(false);
        }
        Log.d(TAG, "Checking existence for email: " + email);
        // Query trong collection 'users'
        return usersCollection.whereEqualTo("email", email).limit(1).get()
                .continueWith(task -> {
                    if (!task.isSuccessful()) {
                        Log.e(TAG, "Error checking email existence: " + email, task.getException());
                        // Trả về false khi có lỗi để tránh chặn luồng sai
                        return false;
                    }
                    QuerySnapshot snapshot = task.getResult();
                    boolean exists = snapshot != null && !snapshot.isEmpty();
                    Log.d(TAG, "Email " + email + " exists: " + exists);
                    return exists;
                });
    }

    // Bỏ hàm getCurrentUser() vì Repository sẽ gọi getUser(currentUserId)
    /*
    @Override
    public Task<User> getCurrentUser() {
        // Logic này nên nằm trong Repository để lấy ID từ AuthDataSource
        // String userId = auth.getCurrentUser().getUid();
        // return getUser(userId);
    }
    */
}

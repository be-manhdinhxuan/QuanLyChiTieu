package com.example.quanlychitieu.data.repository.impl;

import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.quanlychitieu.data.datasource.firebase.auth.FirebaseAuthDataSource;
import com.example.quanlychitieu.data.datasource.firebase.firestore.FirestoreSpendingDataSource;
import com.example.quanlychitieu.data.datasource.firebase.storage.FirebaseStorageDataSource;
import com.example.quanlychitieu.data.datasource.local.datasource.LocalSpendingDataSource;
import com.example.quanlychitieu.data.mapper.SpendingMapper;
import com.example.quanlychitieu.domain.model.spending.Spending;
import com.example.quanlychitieu.domain.repository.SpendingRepository;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Implementation of SpendingRepository that combines remote (Firebase) and
 * local (Room) datasources
 */
@Singleton
public class SpendingRepositoryImpl implements SpendingRepository {
    private static final String TAG = "SpendingRepositoryImpl";

    private final FirestoreSpendingDataSource remoteDataSource;
    private final LocalSpendingDataSource localDataSource;
    private final FirebaseAuthDataSource authDataSource;
    private final FirebaseStorageDataSource storageDataSource;
    private final SpendingMapper mapper;
    private final Executor executor;

    @Inject
    public SpendingRepositoryImpl(
            FirestoreSpendingDataSource remoteDataSource,
            LocalSpendingDataSource localDataSource,
            FirebaseAuthDataSource authDataSource,
            FirebaseStorageDataSource storageDataSource,
            SpendingMapper mapper) {
        this.remoteDataSource = remoteDataSource;
        this.localDataSource = localDataSource;
        this.authDataSource = authDataSource;
        this.storageDataSource = storageDataSource;
        this.mapper = mapper;
        this.executor = Executors.newSingleThreadExecutor();
    }

    // ... (getAllSpendings, getSpendingsByDate, getSpendingById giữ nguyên) ...
    @Override
    public Task<List<Spending>> getAllSpendings() {
        FirebaseUser firebaseUser = authDataSource.getCurrentUser();
        if (firebaseUser == null) {
            Log.w(TAG, "getAllSpendings: No user signed in.");
            return Tasks.forException(new Exception("No user is signed in"));
        }
        final String userId = firebaseUser.getUid();
        Log.d(TAG, "getAllSpendings for user: " + userId);
        
        return remoteDataSource.getAllActiveSpendings() // New method to get non-deleted spendings
                .continueWith(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        List<Spending> spendings = task.getResult();
                        Log.d(TAG, "Successfully fetched " + spendings.size() 
                            + " active spendings from remote.");
                        if (localDataSource != null) {
                            cacheSpendings(userId, spendings);
                        }
                        return spendings;
                    } else {
                        Log.e(TAG, "Error getting all spendings from remote", task.getException());
                        throw task.getException();
                    }
                });
    }

    @Override
    public Task<List<Spending>> getSpendingsByDate(Date startDate, Date endDate) {
        FirebaseUser firebaseUser = authDataSource.getCurrentUser();
        if (firebaseUser == null) { Log.w(TAG, "getSpendingsByDate: No user signed in."); return Tasks.forException(new Exception("No user is signed in")); }
        final String userId = firebaseUser.getUid();
        Log.d(TAG, "getSpendingsByDate for user: " + userId + " from " + startDate + " to " + endDate);
        return remoteDataSource.getSpendingsByDateRange(startDate, endDate)
                .continueWith(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        List<Spending> spendings = task.getResult();
                        Log.d(TAG, "Successfully fetched " + spendings.size() + " spendings by date from remote.");
                        if (localDataSource != null) { cacheSpendings(userId, spendings); }
                        return spendings;
                    } else {
                        Log.e(TAG, "Error getting spendings by date from remote", task.getException());
                        throw task.getException();
                    }
                });
    }

    @Override
    public Task<Spending> getSpendingById(String spendingId) {
        FirebaseUser firebaseUser = authDataSource.getCurrentUser();
        if (firebaseUser == null) { Log.w(TAG, "getSpendingById: No user signed in."); return Tasks.forException(new Exception("No user is signed in")); }
        if (spendingId == null || spendingId.isEmpty()) { Log.w(TAG, "getSpendingById: spendingId is null or empty."); return Tasks.forException(new IllegalArgumentException("Spending ID cannot be null or empty.")); }
        final String userId = firebaseUser.getUid();
        Log.d(TAG, "getSpendingById: " + spendingId + " for user: " + userId);
        return remoteDataSource.getSpending(spendingId)
                .continueWithTask(remoteTask -> {
                    if (remoteTask.isSuccessful() && remoteTask.getResult() != null) {
                        Spending spending = remoteTask.getResult();
                        Log.d(TAG, "Successfully fetched spending " + spendingId + " from remote.");
                        if (spending.getUserId() == null || !spending.getUserId().equals(userId)) {
                            Log.w(TAG, "Fetched spending " + spendingId + " but user ID mismatch or null. Returning error.");
                            return Tasks.forException(new SecurityException("Spending does not belong to the current user."));
                        }
                        if (localDataSource != null) { cacheSpending(userId, spending); }
                        return Tasks.forResult(spending);
                    } else {
                        Log.e(TAG, "Error getting spending " + spendingId + " from remote", remoteTask.getException());
                        throw remoteTask.getException() != null ? remoteTask.getException() : new Exception("Spending not found or error occurred");
                    }
                });
    }


    @Override
    public Task<String> addSpending(Spending spending) {
        FirebaseUser firebaseUser = authDataSource.getCurrentUser();
        if (firebaseUser == null) { Log.w(TAG, "addSpending: No user signed in."); return Tasks.forException(new Exception("No user is signed in")); }
        if (spending == null) { Log.w(TAG, "addSpending: spending object is null."); return Tasks.forException(new IllegalArgumentException("Spending object cannot be null")); }

        final String userId = firebaseUser.getUid();
        spending.setUserId(userId);

        Log.d(TAG, "Adding spending for user: " + userId);

        return remoteDataSource.addSpending(spending)
                .continueWith(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        String newSpendingId = task.getResult();
                        Log.d(TAG, "Successfully added spending to remote, received ID: " + newSpendingId);

                        // *** SỬA LOGIC CACHE Ở ĐÂY ***
                        if (localDataSource != null) {
                            // Truyền spending object gốc (chưa có ID) và ID mới vào hàm cache
                            cacheSpendingById(userId, spending, newSpendingId);
                        }
                        return newSpendingId;
                    } else {
                        Log.e(TAG, "Error adding spending to remote", task.getException());
                        throw task.getException();
                    }
                });
    }

    // Bỏ addSpendingWithImage nếu không dùng
    @Override
    public Task<String> addSpendingWithImage(Spending spending, Uri imageUri) {
        // ... (Giữ nguyên logic hoặc bỏ đi) ...
        Log.w(TAG, "addSpendingWithImage is deprecated, use addSpending then uploadSpendingImage separately.");
        if (imageUri == null) return addSpending(spending);
        return addSpending(spending)
                .continueWithTask(addSpendingTask -> {
                    if (!addSpendingTask.isSuccessful()) throw addSpendingTask.getException();
                    String spendingId = addSpendingTask.getResult();
                    if (spendingId == null || spendingId.isEmpty()) throw new Exception("addSpending returned null or empty ID.");
                    Log.d(TAG, "Spending added (ID: " + spendingId + "), now uploading image.");
                    return uploadSpendingImage(spendingId, imageUri)
                            .continueWithTask(uploadTask -> {
                                if (!uploadTask.isSuccessful()) {
                                    Log.e(TAG, "Failed to upload image for new spending " + spendingId, uploadTask.getException());
                                } else {
                                    String imageUrl = uploadTask.getResult();
                                    Log.d(TAG, "Image uploaded successfully for " + spendingId + ". URL: " + imageUrl);
                                    // Cần cập nhật spending với URL ảnh
                                    Log.w(TAG,"Need to implement logic to update spending record with image URL: " + imageUrl);
                                    // Ví dụ: updateSpendingImageField(spendingId, imageUrl);
                                }
                                return Tasks.forResult(spendingId);
                            });
                });
    }

    @Override
    public Task<Void> updateSpending(Spending spending) {
        // ... (Giữ nguyên logic update, cache dùng spending object đã có ID) ...
        FirebaseUser firebaseUser = authDataSource.getCurrentUser();
        if (firebaseUser == null) { Log.w(TAG, "updateSpending: No user signed in."); return Tasks.forException(new Exception("No user is signed in")); }
        if (spending == null || spending.getId() == null || spending.getId().isEmpty()) { Log.w(TAG, "updateSpending: spending object or its ID is null/empty."); return Tasks.forException(new IllegalArgumentException("Spending object and its ID cannot be null or empty for update")); }
        final String userId = firebaseUser.getUid();
        spending.setUserId(userId);
        Log.d(TAG, "Updating spending ID: " + spending.getId() + " for user: " + userId);
        return remoteDataSource.updateSpending(spending)
                .continueWith(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Successfully updated spending " + spending.getId() + " on remote.");
                        if (localDataSource != null) { cacheSpending(userId, spending); }
                        return null;
                    } else {
                        Log.e(TAG, "Error updating spending " + spending.getId(), task.getException());
                        throw task.getException();
                    }
                });
    }

    // Bỏ updateSpendingWithImage nếu không dùng
    @Override
    public Task<Void> updateSpendingWithImage(Spending spending, Uri newImageUri, boolean isImageRemoved) {
        Log.w(TAG, "updateSpendingWithImage is deprecated, use updateSpending in Adapter which handles image logic.");
        return Tasks.forException(new UnsupportedOperationException("Use updateSpending in Adapter/ViewModel"));
    }

    @Override
    public Task<Void> deleteSpending(String spendingId) {
        FirebaseUser firebaseUser = authDataSource.getCurrentUser();
        if (firebaseUser == null) { 
            Log.w(TAG, "deleteSpending: No user signed in."); 
            return Tasks.forException(new Exception("No user is signed in")); 
        }
        if (spendingId == null || spendingId.isEmpty()) { 
            Log.w(TAG, "deleteSpending: spendingId is null or empty."); 
            return Tasks.forException(new IllegalArgumentException("Spending ID cannot be null or empty for delete")); 
        }
        final String userId = firebaseUser.getUid();
        Log.d(TAG, "Deleting spending ID: " + spendingId + " for user: " + userId);
        
        return remoteDataSource.getSpending(spendingId)
                .continueWithTask(getTask -> {
                    String imageUrl = null; 
                    Spending spendingToDelete = null;
                    if (getTask.isSuccessful() && getTask.getResult() != null) {
                        spendingToDelete = getTask.getResult();
                        if (spendingToDelete.getUserId() == null || !spendingToDelete.getUserId().equals(userId)) {
                            Log.e(TAG, "User " + userId + " attempting to delete spending " + spendingId + " owned by " + spendingToDelete.getUserId());
                            throw new SecurityException("User does not have permission to delete this spending.");
                        }
                        imageUrl = spendingToDelete.getImage();
                    } else { 
                        Log.w(TAG, "Could not get spending details before deleting " + spendingId, getTask.getException()); 
                    }

                    Task<Void> deleteImageTask = Tasks.forResult(null);
                    if (imageUrl != null && !imageUrl.isEmpty()) { 
                        deleteImageTask = deleteSpendingImage(spendingId); 
                    }

                    String finalImageUrl = imageUrl;
                    return deleteImageTask.continueWithTask(delImgTask -> {
                        if (!delImgTask.isSuccessful()) { 
                            Log.e(TAG, "Failed to delete image for " + spendingId + " (URL: " + finalImageUrl + "), proceeding.", delImgTask.getException()); 
                        }
                        else if (finalImageUrl != null) { 
                            Log.d(TAG, "Successfully deleted image for " + spendingId); 
                        }
                        return remoteDataSource.hardDeleteSpending(spendingId); // Thay đổi từ deleteSpending sang hardDeleteSpending
                    });
                })
                .continueWithTask(deleteDocTask -> {
                    if (deleteDocTask.isSuccessful()) {
                        Log.d(TAG, "Successfully deleted spending document " + spendingId + " from remote.");
                        if (localDataSource != null) {
                            final String compositeIdToDelete = userId + "/" + spendingId;
                            executor.execute(() -> localDataSource.deleteSpending(compositeIdToDelete));
                        }
                        return Tasks.forResult(null);
                    } else {
                        Log.e(TAG, "Error deleting spending document " + spendingId, deleteDocTask.getException());
                        throw deleteDocTask.getException();
                    }
                });
    }


    @Override
    public Task<Void> syncSpendings() {
        // ... (Giữ nguyên logic sync, đảm bảo cacheSpendings đúng) ...
        FirebaseUser firebaseUser = authDataSource.getCurrentUser();
        if (firebaseUser == null) { Log.w(TAG, "syncSpendings: No user signed in."); return Tasks.forException(new Exception("No user is signed in")); }
        final String userId = firebaseUser.getUid();
        Log.d(TAG, "Syncing spendings for user: " + userId);
        return remoteDataSource.getAllSpendings()
                .continueWith(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        List<Spending> spendings = task.getResult();
                        Log.d(TAG, "Sync successful, fetched " + spendings.size() + " spendings. Caching...");
                        if (localDataSource != null) {
                            executor.execute(() -> {
                                // localDataSource.deleteAllSpendingsByUser(userId); // Cần hàm xóa cũ
                                cacheSpendings(userId, spendings);
                                Log.d(TAG, "Finished caching synced spendings.");
                            });
                        }
                    } else { Log.e(TAG, "Error syncing spendings from remote", task.getException()); }
                    return null;
                });
    }

    @Override
    public Task<Integer> getTotalSpending(Date startDate, Date endDate) {
        // ... (Giữ nguyên logic) ...
        FirebaseUser firebaseUser = authDataSource.getCurrentUser();
        if (firebaseUser == null) { Log.w(TAG, "getTotalSpending: No user signed in."); return Tasks.forException(new Exception("No user is signed in")); }
        final String userId = firebaseUser.getUid();
        Log.d(TAG, "Getting total spending for user: " + userId + " from " + startDate + " to " + endDate);
        return remoteDataSource.getTotalSpending(userId, startDate, endDate)
                .continueWith(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        int total = task.getResult();
                        Log.d(TAG, "Successfully calculated total spending: " + total);
                        return total;
                    } else {
                        Log.e(TAG, "Error getting total spending", task.getException());
                        throw task.getException();
                    }
                });
    }

    // --- Image Handling Methods ---
    @Override
    public Task<String> getSpendingImageUrl(String spendingId) {
        // ... (Giữ nguyên logic) ...
        if (spendingId == null || spendingId.isEmpty()) { return Tasks.forException(new IllegalArgumentException("Spending ID cannot be null or empty.")); }
        Log.d(TAG, "Getting image URL for spending: " + spendingId);
        return remoteDataSource.getSpending(spendingId).continueWith(task -> {
            if (task.isSuccessful() && task.getResult() != null) { return task.getResult().getImage(); }
            else { Log.e(TAG,"Error getting spending to retrieve image URL", task.getException()); return null; }
        });
    }

    @Override
    public Task<Void> deleteSpendingImage(String spendingId) {
        // ... (Giữ nguyên logic) ...
        if (spendingId == null || spendingId.isEmpty()) { return Tasks.forException(new IllegalArgumentException("Spending ID cannot be null or empty.")); }
        Log.d(TAG, "Deleting image for spending: " + spendingId);
        return remoteDataSource.getSpending(spendingId).continueWithTask(getTask -> {
            if (getTask.isSuccessful() && getTask.getResult() != null) {
                String imageUrl = getTask.getResult().getImage();
                if (imageUrl != null && !imageUrl.isEmpty()) {
                    // Gọi hàm xóa của DataSource tương ứng
                    if (storageDataSource != null) { // Nếu dùng Firebase Storage
                        // String imagePath = SPENDING_IMAGES_PATH + "/" + spendingId;
                        // return storageDataSource.deleteFile(imagePath);
                        // Cần logic lấy path từ URL nếu dùng Cloudinary hoặc URL không theo quy tắc
                        Log.w(TAG,"Firebase Storage delete logic needs path from URL or different approach.");
                        return Tasks.forResult(null); // Tạm thời
                    } else { // Nếu dùng Cloudinary qua remoteDataSource
                        Log.w(TAG,"Image deletion logic via remoteDataSource (Cloudinary?) not implemented here.");
                        // return remoteDataSource.deleteSpendingImageWithUrl(imageUrl);
                        return Tasks.forResult(null);
                    }
                } else { Log.d(TAG,"Spending " + spendingId + " has no image URL to delete."); return Tasks.forResult(null); }
            } else { Log.w(TAG,"Could not get spending " + spendingId + " to delete its image.", getTask.getException()); return Tasks.forResult(null); }
        });
    }

    @Override
    public Task<String> uploadSpendingImage(String spendingId, Uri imageUri) {
        // ... (Giữ nguyên logic) ...
        if (spendingId == null || spendingId.isEmpty() || imageUri == null) { return Tasks.forException(new IllegalArgumentException("Spending ID and Image URI cannot be null or empty.")); }
        Log.d(TAG, "Uploading image for spending: " + spendingId);
        return remoteDataSource.uploadSpendingImage(spendingId, imageUri); // Giả định remoteDataSource xử lý Cloudinary
    }

    // --- Caching Methods ---
    private void cacheSpendings(String userId, List<Spending> spendings) {
        if (localDataSource == null || spendings == null || spendings.isEmpty()) return;
        executor.execute(() -> {
            Log.d(TAG, "Caching " + spendings.size() + " spendings for user " + userId);
            for (Spending spending : spendings) {
                if (spending != null && spending.getId() != null) { // Cần ID để cache
                    localDataSource.saveSpending(spending); // Giả sử hàm này nhận Spending đã có ID
                }
            }
            Log.d(TAG, "Finished caching spendings for user " + userId);
        });
    }

    private void cacheSpending(String userId, Spending spending) {
        if (localDataSource == null || spending == null || spending.getId() == null) return; // Cần ID
        executor.execute(() -> {
            Log.d(TAG, "Caching spending ID: " + spending.getId() + " for user " + userId);
            localDataSource.saveSpending(spending); // Giả sử hàm này nhận Spending đã có ID
            Log.d(TAG, "Finished caching spending ID: " + spending.getId());
        });
    }

    // *** SỬA LẠI HÀM NÀY ***
    private void cacheSpendingById(String userId, Spending spendingData, String spendingId) {
        if (localDataSource == null || spendingData == null || spendingId == null) return;
        executor.execute(() -> {
            Log.d(TAG, "Caching new spending ID: " + spendingId + " for user " + userId);

            // *** CÁCH 1: Truyền ID và Object riêng (Nếu LocalDataSource hỗ trợ) ***
            // localDataSource.saveSpendingWithId(spendingId, spendingData); // Cần tạo hàm này

            // *** CÁCH 2: Dùng mapper để tạo Entity có ID (Nếu dùng Room và Mapper) ***
            if (mapper != null) {
                try {
                    // Giả sử mapper có thể tạo Entity từ Domain và bạn có thể set ID cho Entity
                    // Hoặc mapper nhận thêm ID
                    // SpendingEntity entity = mapper.toEntityWithId(spendingData, spendingId); // Cần tạo hàm này trong mapper
                    // localDataSource.saveSpendingEntity(entity); // Cần tạo hàm này trong LocalDataSource

                    // Cách tiếp cận khác: Tạo bản sao Spending, gán ID rồi map sang Entity
                    Spending spendingToCache = cloneSpendingAndSetId(spendingData, spendingId);
                    if (spendingToCache != null) {
                        localDataSource.saveSpending(spendingToCache); // Giả sử saveSpending nhận object có ID
                        Log.d(TAG, "Finished caching new spending ID: " + spendingId);
                    } else {
                        Log.e(TAG, "Failed to prepare spending object for caching ID: " + spendingId);
                    }

                } catch (Exception e) {
                    Log.e(TAG, "Error mapping or saving spending to local cache for ID: " + spendingId, e);
                }
            } else {
                Log.w(TAG, "Mapper is null, cannot cache spending by ID: " + spendingId);
            }
        });
    }

    // *** BỎ HÀM CLONE GÂY LỖI ***
    // Helper để tạo bản sao Spending với ID mới (CHỈ DÙNG NẾU Spending có setId)
    private Spending cloneSpendingAndSetId(Spending original, String newId) {
        if (original == null || newId == null) return null;
        // Tạo bản sao nông (shallow copy) hoặc sâu (deep copy) tùy nhu cầu
        Spending clone = new Spending();
        clone.setMoney(original.getMoney());
        clone.setType(original.getType());
        clone.setTypeName(original.getTypeName());
        clone.setDateTime(original.getDateTime());
        clone.setMonthKey(original.getMonthKey());
        clone.setNote(original.getNote());
        clone.setLocation(original.getLocation());
        clone.setImage(original.getImage());
        clone.setUserId(original.getUserId());
        clone.setFriends(original.getFriends() != null ? new ArrayList<>(original.getFriends()) : null);
        clone.setCreatedAt(original.getCreatedAt());
        clone.setUpdatedAt(original.getUpdatedAt());

        // *** GÁN ID CHO BẢN SAO (YÊU CẦU Spending CÓ setId) ***
        // Nếu Spending.java không có setId, dòng này sẽ lỗi và hàm trả về null
        try {
            java.lang.reflect.Method setIdMethod = Spending.class.getMethod("setId", String.class);
            setIdMethod.invoke(clone, newId);
            return clone; // Trả về bản sao đã có ID
        } catch (NoSuchMethodException nsme) {
            Log.e(TAG, "Spending class does not have setId(String). Cannot set ID for caching clone.", nsme);
        } catch (Exception e) {
            Log.e(TAG, "Error setting ID on cloned Spending object via reflection", e);
        }
        return null; // Trả về null nếu không gán được ID
    }


    private boolean isCurrentUser(String userId) {
        FirebaseUser currentUser = authDataSource.getCurrentUser();
        return currentUser != null && userId != null && currentUser.getUid().equals(userId);
    }

    // Thêm hàm cập nhật chỉ trường ảnh
    public Task<Void> updateSpendingImage(String spendingId, String imageUrl) {
        if (spendingId == null || spendingId.isEmpty()) {
            return Tasks.forException(new IllegalArgumentException("Spending ID cannot be null or empty."));
        }
        Log.d(TAG, "Updating image URL for spending ID: " + spendingId);
        // Gọi phương thức tương ứng trong DataSource
        return remoteDataSource.updateSpendingImage(spendingId, imageUrl) // Cần tạo hàm này trong DataSource
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        Log.e(TAG, "Failed to update image URL on remote for " + spendingId, task.getException());
                        throw task.getException();
                    }
                    Log.d(TAG, "Successfully updated image URL on remote for " + spendingId);
                    // Cập nhật cache nếu cần
                    if (localDataSource != null) {
                        // Cần hàm update riêng cho ảnh trong localDataSource hoặc lấy về rồi cache lại
                        // executor.execute(() -> localDataSource.updateSpendingImage(spendingId, imageUrl));
                    }
                    return Tasks.forResult(null);
                });
    }

    @Override
    public Task<Void> softDeleteSpending(String spendingId) {
        FirebaseUser firebaseUser = authDataSource.getCurrentUser();
        if (firebaseUser == null) {
            Log.w(TAG, "softDeleteSpending: No user signed in.");
            return Tasks.forException(new Exception("No user is signed in"));
        }
        if (spendingId == null || spendingId.isEmpty()) {
            Log.w(TAG, "softDeleteSpending: spendingId is null or empty.");
            return Tasks.forException(new IllegalArgumentException("Spending ID cannot be null or empty"));
        }

        final String userId = firebaseUser.getUid();
        Log.d(TAG, "Soft deleting spending ID: " + spendingId + " for user: " + userId);

        return remoteDataSource.getSpending(spendingId)
                .continueWithTask(getTask -> {
                    if (!getTask.isSuccessful() || getTask.getResult() == null) {
                        Log.e(TAG, "Error getting spending to soft delete", getTask.getException());
                        throw getTask.getException() != null ? getTask.getException() 
                            : new Exception("Spending not found");
                    }

                    Spending spending = getTask.getResult();
                    if (!userId.equals(spending.getUserId())) {
                        return Tasks.forException(
                            new SecurityException("Spending does not belong to current user"));
                    }

                    // Set isDeleted flag to true
                    spending.setIsDeleted(true);
                    
                    // Update the spending in remote database
                    return remoteDataSource.updateSpending(spending)
                            .continueWithTask(updateTask -> {
                                if (updateTask.isSuccessful()) {
                                    Log.d(TAG, "Successfully soft deleted spending " + spendingId);
                                    
                                    // Update local cache if available
                                    if (localDataSource != null) {
                                        executor.execute(() -> {
                                            localDataSource.updateSpending(spending);
                                        });
                                    }
                                    return Tasks.forResult(null);
                                } else {
                                    Log.e(TAG, "Error soft deleting spending", updateTask.getException());
                                    throw updateTask.getException();
                                }
                            });
                });
    }

    @Override
    public Task<List<Spending>> getDeletedSpendings() {
        FirebaseUser firebaseUser = authDataSource.getCurrentUser();
        if (firebaseUser == null) {
            Log.w(TAG, "getDeletedSpendings: No user signed in.");
            return Tasks.forException(new Exception("No user is signed in"));
        }

        final String userId = firebaseUser.getUid();
        Log.d(TAG, "Getting deleted spendings for user: " + userId);

        return remoteDataSource.getDeletedSpendings()
                .continueWith(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        List<Spending> deletedSpendings = task.getResult();
                        Log.d(TAG, "Successfully fetched " + deletedSpendings.size() 
                            + " deleted spendings from remote.");
                        
                        // Cache deleted spendings if local storage is available
                        if (localDataSource != null) {
                            cacheSpendings(userId, deletedSpendings);
                        }
                        return deletedSpendings;
                    } else {
                        Log.e(TAG, "Error getting deleted spendings", task.getException());
                        throw task.getException() != null ? task.getException() 
                            : new Exception("Error fetching deleted spendings");
                    }
                });
    }

    @Override
    public Task<Void> restoreSpending(String spendingId) {
        FirebaseUser firebaseUser = authDataSource.getCurrentUser();
        if (firebaseUser == null) {
            Log.w(TAG, "restoreSpending: No user signed in.");
            return Tasks.forException(new Exception("No user is signed in"));
        }
        if (spendingId == null || spendingId.isEmpty()) {
            Log.w(TAG, "restoreSpending: spendingId is null or empty.");
            return Tasks.forException(new IllegalArgumentException("Spending ID cannot be null or empty"));
        }

        final String userId = firebaseUser.getUid();
        Log.d(TAG, "Restoring spending ID: " + spendingId + " for user: " + userId);

        return remoteDataSource.getSpending(spendingId)
                .continueWithTask(getTask -> {
                    if (!getTask.isSuccessful() || getTask.getResult() == null) {
                        Log.e(TAG, "Error getting spending to restore", getTask.getException());
                        throw getTask.getException() != null ? getTask.getException() 
                            : new Exception("Spending not found");
                    }

                    Spending spending = getTask.getResult();
                    if (!userId.equals(spending.getUserId())) {
                        return Tasks.forException(
                            new SecurityException("Spending does not belong to current user"));
                    }

                    // Set isDeleted flag to false to restore
                    spending.setIsDeleted(false);
                    
                    // Update the spending in remote database
                    return remoteDataSource.updateSpending(spending)
                            .continueWithTask(updateTask -> {
                                if (updateTask.isSuccessful()) {
                                    Log.d(TAG, "Successfully restored spending " + spendingId);
                                    
                                    // Update local cache if available
                                    if (localDataSource != null) {
                                        executor.execute(() -> {
                                            localDataSource.updateSpending(spending);
                                        });
                                    }
                                    return Tasks.forResult(null);
                                } else {
                                    Log.e(TAG, "Error restoring spending", updateTask.getException());
                                    throw updateTask.getException();
                                }
                            });
                });
    }
}

package com.example.quanlychitieu.data.datasource.firebase.firestore.impl;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.example.quanlychitieu.core.utils.CloudinaryManager;
import com.example.quanlychitieu.data.datasource.firebase.firestore.FirestoreConstants;
import com.example.quanlychitieu.data.datasource.firebase.firestore.FirestoreSpendingDataSource;
import com.example.quanlychitieu.domain.model.spending.Spending;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException; // Thêm import
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import dagger.hilt.android.qualifiers.ApplicationContext;

import javax.inject.Inject;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class FirestoreSpendingDataSourceImpl implements FirestoreSpendingDataSource {
    private static final String TAG = "FirestoreSpendingSource";
    // Giữ lại các hằng số nếu vẫn dùng ở restore/softDelete
    private static final String FIELD_IS_DELETED = "isDeleted";
    private static final String FIELD_DELETED_AT = "deletedAt";

    private final FirebaseFirestore firestore;
    private final FirebaseStorage storage; // Giữ lại nếu dùng Firebase Storage cho ảnh
    private final FirebaseAuth auth;
    private final CollectionReference spendingsCollection;
    // private final SimpleDateFormat dateFormat = new SimpleDateFormat("MM_yyyy", Locale.getDefault()); // Không cần nếu không cập nhật data collection
    private final Context context; // Cần cho Cloudinary

    @Inject
    public FirestoreSpendingDataSourceImpl(
            FirebaseFirestore firestore,
            FirebaseStorage storage, // Giữ lại nếu dùng
            FirebaseAuth auth,
            @ApplicationContext Context context) {
        this.firestore = firestore;
        this.storage = storage;
        this.auth = auth;
        this.context = context;
        this.spendingsCollection = firestore.collection(FirestoreConstants.COLLECTION_SPENDINGS);
    }

    // --- addSpending (Giữ nguyên logic cũ đã đúng) ---
    @Override
    public Task<String> addSpending(Spending spending) {
        if (auth.getCurrentUser() == null) {
            return Tasks.forException(new IllegalStateException("User not authenticated"));
        }
        String userId = auth.getCurrentUser().getUid();
        DocumentReference spendingRef = spendingsCollection.document();
        String newSpendingId = spendingRef.getId();

        spending.setUserId(userId);
        spending.setCreatedAt(System.currentTimeMillis());
        spending.setUpdatedAt(System.currentTimeMillis());

        int absoluteMoney = Math.abs(spending.getMoney());
        spending.setMoney(spending.isIncome() ? absoluteMoney : -absoluteMoney);

        Log.d(TAG, "Attempting to add spending with generated ID: " + newSpendingId);
        Log.d(TAG, "User ID: " + userId);
        Map<String, Object> spendingData = spending.toMap(); // Dùng toMap() đã bỏ ID
        Log.d(TAG, "Data to save: " + spendingData.toString());

        return spendingRef.set(spendingData)
                .continueWith(task -> {
                    if (!task.isSuccessful()) {
                        Log.e(TAG, "Error setting spending document", task.getException());
                        throw task.getException();
                    }
                    Log.d(TAG, "Successfully added spending with ID: " + newSpendingId);
                    return newSpendingId;
                });
    }

    // --- uploadSpendingImage (Giữ nguyên) ---
    @Override
    public Task<String> uploadSpendingImage(String spendingId, Uri imageUri) {
        if (imageUri == null) return Tasks.forException(new IllegalArgumentException("Image URI cannot be null"));
        if (auth.getCurrentUser() == null) return Tasks.forException(new IllegalStateException("User not authenticated"));

        TaskCompletionSource<String> taskCompletionSource = new TaskCompletionSource<>();
        String userId = auth.getCurrentUser().getUid();

        CloudinaryManager.getInstance(context)
                .uploadImage(context, imageUri, "spendings/" + userId + "/" + spendingId,
                        new CloudinaryManager.UploadCallback() {
                            @Override
                            public void onSuccess(String imageUrl) { taskCompletionSource.setResult(imageUrl); }
                            @Override
                            public void onError(Exception e) { taskCompletionSource.setException(e); }
                        });
        return taskCompletionSource.getTask();
    }

    // --- getSpending (Giữ nguyên) ---
    @Override
    public Task<Spending> getSpending(String spendingId) {
        if (spendingId == null || spendingId.isEmpty()) {
            return Tasks.forException(new IllegalArgumentException("Spending ID cannot be null or empty"));
        }
        Log.d(TAG, "Getting spending with ID: " + spendingId);
        return spendingsCollection.document(spendingId).get()
                .continueWith(task -> {
                    if (!task.isSuccessful()) {
                        Log.e(TAG, "Error getting spending document: " + spendingId, task.getException());
                        throw task.getException();
                    }
                    DocumentSnapshot document = task.getResult();
                    if (document != null && document.exists()) {
                        Spending spending = document.toObject(Spending.class);
                        if (spending != null) {
                            Log.d(TAG, "Successfully retrieved and parsed spending: " + spendingId);
                        } else {
                            Log.w(TAG, "Failed to parse document to Spending object: " + spendingId);
                            return null;
                        }
                        return spending;
                    } else {
                        Log.w(TAG, "Spending document not found: " + spendingId);
                        return null;
                    }
                });
    }

    // --- getAllSpendings (Đã sửa đúng để lọc isDeleted=false) ---
    @Override
    public Task<List<Spending>> getAllSpendings() {
        if (auth.getCurrentUser() == null) {
            return Tasks.forException(new IllegalStateException("User not authenticated"));
        }
        String userId = auth.getCurrentUser().getUid();
        Log.d(TAG, "Getting all active spendings for user: " + userId);

        return spendingsCollection
                .whereEqualTo(FirestoreConstants.FIELD_USER_ID, userId)
                .whereEqualTo(FIELD_IS_DELETED, false) // Chỉ lấy chi tiêu chưa bị xóa
                .orderBy(FirestoreConstants.FIELD_SPENDING_DATE_TIME, Query.Direction.DESCENDING)
                .get()
                .continueWith(task -> {
                    if (!task.isSuccessful()) {
                        Log.e(TAG, "Error getting all spendings for user: " + userId, task.getException());
                        throw task.getException();
                    }
                    List<Spending> spendings = new ArrayList<>();
                    QuerySnapshot querySnapshot = task.getResult();
                    if (querySnapshot != null) {
                        for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                            Spending spending = document.toObject(Spending.class);
                            if (spending != null) {
                                spendings.add(spending);
                            }
                        }
                    }
                    return spendings;
                });
    }

    // --- getSpendingsByMonth (Đã sửa đúng để lọc isDeleted=false) ---
    @Override
    public Task<List<Spending>> getSpendingsByMonth(Date date) {
        if (auth.getCurrentUser() == null) {
            return Tasks.forException(new IllegalStateException("User not authenticated"));
        }
        String userId = auth.getCurrentUser().getUid();
        SimpleDateFormat monthKeyFormat = new SimpleDateFormat("MM_yyyy", Locale.getDefault()); // Định dạng key tháng
        String monthKey = monthKeyFormat.format(date);
        Log.d(TAG, "Getting spendings for user " + userId + " and monthKey " + monthKey);

        return spendingsCollection
                .whereEqualTo(FirestoreConstants.FIELD_USER_ID, userId)
                .whereEqualTo(FirestoreConstants.FIELD_MONTH_KEY, monthKey)
                .whereEqualTo(FIELD_IS_DELETED, false) // Chỉ lấy chưa xóa
                .orderBy(FirestoreConstants.FIELD_SPENDING_DATE_TIME, Query.Direction.DESCENDING)
                .get()
                .continueWith(task -> {
                    if (!task.isSuccessful()) {
                        Log.e(TAG, "Error getting spendings for month: " + monthKey, task.getException());
                        throw task.getException();
                    }
                    List<Spending> spendings = new ArrayList<>();
                    QuerySnapshot querySnapshot = task.getResult();
                    if (querySnapshot != null) {
                        for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                            Spending spending = document.toObject(Spending.class);
                            if (spending != null) {
                                spendings.add(spending);
                            }
                        }
                    }
                    return spendings;
                });
    }

    // --- getSpendingsByDateRange (Đã sửa đúng để lọc isDeleted=false) ---
    @Override
    public Task<List<Spending>> getSpendingsByDateRange(Date startDate, Date endDate) {
        if (auth.getCurrentUser() == null) {
            return Tasks.forException(new IllegalStateException("User not authenticated"));
        }
        String userId = auth.getCurrentUser().getUid();

        return spendingsCollection
                .whereEqualTo(FirestoreConstants.FIELD_USER_ID, userId)
                .whereEqualTo(FIELD_IS_DELETED, false) // Chỉ lấy chưa xóa
                .whereGreaterThanOrEqualTo(FirestoreConstants.FIELD_SPENDING_DATE_TIME, startDate)
                .whereLessThanOrEqualTo(FirestoreConstants.FIELD_SPENDING_DATE_TIME, endDate) // Dùng <= cho endDate
                .orderBy(FirestoreConstants.FIELD_SPENDING_DATE_TIME, Query.Direction.DESCENDING)
                .get()
                .continueWith(task -> {
                    if (!task.isSuccessful()) {
                        Log.e(TAG, "Error getting spendings by date range", task.getException());
                        throw task.getException();
                    }
                    List<Spending> spendings = new ArrayList<>();
                    for (DocumentSnapshot doc : task.getResult()) {
                        Spending spending = doc.toObject(Spending.class);
                        if (spending != null) {
                            spendings.add(spending);
                        }
                    }
                    return spendings;
                });
    }


    // *** HÀM updateSpending ĐÃ SỬA ĐỔI ***
    @Override
    public Task<Void> updateSpending(Spending spending) {
        if (auth.getCurrentUser() == null) {
            return Tasks.forException(new IllegalStateException("User not authenticated"));
        }
        if (spending == null || spending.getId() == null || spending.getId().isEmpty()) {
            return Tasks.forException(new IllegalArgumentException("Spending or Spending ID cannot be null or empty for update"));
        }
        // Không cần lấy userId ở đây vì không dùng để cập nhật collection data nữa
        String spendingId = spending.getId();

        Log.d(TAG, "Updating spending with ID: " + spendingId);

        // Đảm bảo money đúng
        int absoluteMoney = Math.abs(spending.getMoney());
        spending.setMoney(spending.isIncome() ? absoluteMoney : -absoluteMoney);

        // Cập nhật thời gian
        spending.setUpdatedAt(System.currentTimeMillis());

        // *** LOẠI BỎ HOÀN TOÀN LOGIC KIỂM TRA THAY ĐỔI THÁNG VÀ CẬP NHẬT COLLECTION data/spending/{userId} ***
        // Chỉ cần cập nhật trực tiếp document chi tiêu trong collection "spending"

        // Sử dụng toMap() đã bỏ ID
        Map<String, Object> spendingUpdates = spending.toMap();

        return spendingsCollection.document(spendingId).update(spendingUpdates)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Successfully updated spending document: " + spendingId))
                .addOnFailureListener(e -> Log.e(TAG, "Error updating spending document: " + spendingId, e));

        // Code cũ liên quan đến kiểm tra tháng và cập nhật collection "data" đã được xóa.
    }


    // *** SỬA TÊN HÀM VÀ LOGIC ĐỂ BỎ CẬP NHẬT COLLECTION DATA ***
    @Override
    public Task<Void> hardDeleteSpending(String spendingId) { // *** ĐỔI TÊN HÀM ***
        if (auth.getCurrentUser() == null) {
            return Tasks.forException(new IllegalStateException("User not authenticated"));
        }
        if (spendingId == null || spendingId.isEmpty()) {
            return Tasks.forException(new IllegalArgumentException("Spending ID cannot be null or empty for delete"));
        }
        // Không cần lấy userId ở đây nữa

        Log.d(TAG, "Hard deleting spending with ID: " + spendingId);

        // Lấy thông tin spending chỉ để xóa ảnh (nếu cần)
        return spendingsCollection.document(spendingId).get()
                .continueWithTask(getTask -> {
                    if (!getTask.isSuccessful()) {
                        // Nếu không lấy được thông tin, vẫn tiếp tục xóa document
                        Log.w(TAG, "Error getting spending before hard delete (continuing anyway): " + spendingId, getTask.getException());
                    }

                    DocumentSnapshot spendingSnapshot = getTask.getResult();
                    String imageUrl = null;
                    if (spendingSnapshot != null && spendingSnapshot.exists()) {
                        imageUrl = spendingSnapshot.getString("image");
                    }

                    // *** LOẠI BỎ LOGIC CẬP NHẬT COLLECTION DATA ***

                    // Xóa ảnh nếu có
                    Task<Void> deleteImageTask = Tasks.forResult(null);
                    if (imageUrl != null && !imageUrl.isEmpty()) {
                        try {
                            Log.d(TAG, "Attempting to delete image associated with spending: " + imageUrl);
                            // Thêm logic xóa ảnh từ Cloudinary hoặc Firebase Storage ở đây nếu cần
                            // Ví dụ Firebase Storage:
                            // StorageReference storageRef = storage.getReferenceFromUrl(imageUrl);
                            // deleteImageTask = storageRef.delete();
                            Log.w(TAG, "Image deletion logic (Cloudinary/Firebase Storage) needs implementation here.");
                        } catch (Exception e) {
                            Log.e(TAG, "Error initiating image deletion for: " + imageUrl, e);
                            // Quyết định xem có nên dừng hay tiếp tục
                        }
                    }

                    // Chờ xóa ảnh xong rồi xóa document
                    return deleteImageTask.continueWithTask(taskAfterImageDelete -> {
                        if (!taskAfterImageDelete.isSuccessful()) {
                            Log.e(TAG, "Error deleting image during hard delete (continuing anyway)", taskAfterImageDelete.getException());
                        }
                        Log.d(TAG, "Proceeding to hard delete spending document: " + spendingId);
                        return spendingsCollection.document(spendingId).delete(); // Thực hiện xóa cứng
                    });
                });
    }


    // --- Các hàm helper (getMonthsBetweenDates, isSameMonth - không cần thiết nữa nếu không cập nhật data) ---
    /*
    private List<Date> getMonthsBetweenDates(Date startDate, Date endDate) { ... }
    private boolean isSameMonth(Date date1, Date date2) { ... }
    private List<String> getListFromMap(Map<String, Object> map, String key) { ... } // Vẫn có thể hữu ích ở nơi khác
    */

    // --- getTotalSpending (Giữ nguyên) ---
    @Override
    public Task<Integer> getTotalSpending(String userId, Date startDate, Date endDate) {
        if (userId == null || userId.isEmpty() || startDate == null || endDate == null || startDate.after(endDate)) {
            return Tasks.forException(new IllegalArgumentException("Invalid arguments for getTotalSpending"));
        }
        Log.d(TAG, "Calculating total spending for user " + userId + " between " + startDate + " and " + endDate);

        return spendingsCollection
                .whereEqualTo(FirestoreConstants.FIELD_USER_ID, userId)
                .whereEqualTo(FIELD_IS_DELETED, false) // Chỉ tính các khoản chưa xóa
                .whereGreaterThanOrEqualTo(FirestoreConstants.FIELD_SPENDING_DATE_TIME, startDate)
                .whereLessThanOrEqualTo(FirestoreConstants.FIELD_SPENDING_DATE_TIME, endDate)
                .get()
                .continueWith(task -> {
                    if (!task.isSuccessful()) {
                        Log.e(TAG, "Error getting spendings for total calculation", task.getException());
                        throw task.getException();
                    }
                    QuerySnapshot querySnapshot = task.getResult();
                    int total = 0;
                    if (querySnapshot != null) {
                        for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                            // Ưu tiên lấy kiểu Long để tránh lỗi ClassCastException
                            Number moneyNum = (Number) document.get("money");
                            if (moneyNum != null) {
                                int money = moneyNum.intValue();
                                if (money < 0) { // Chỉ cộng các khoản chi
                                    total += Math.abs(money);
                                }
                            }
                        }
                    }
                    Log.d(TAG, "Total spending calculated: " + total);
                    return total;
                });
    }


    // --- updateSpendingImage (Giữ nguyên) ---
    @Override
    public Task<Void> updateSpendingImage(String spendingId, String imageUrl) {
        if (auth.getCurrentUser() == null) return Tasks.forException(new IllegalStateException("User not authenticated"));
        if (spendingId == null || spendingId.isEmpty()) return Tasks.forException(new IllegalArgumentException("Spending ID cannot be null or empty"));

        Log.d(TAG, "Updating image URL for spending: " + spendingId);
        Map<String, Object> updates = new HashMap<>();
        updates.put("image", imageUrl);
        updates.put("updatedAt", System.currentTimeMillis());

        return spendingsCollection.document(spendingId).update(updates)
                .continueWith(task -> {
                    if (!task.isSuccessful()) {
                        Log.e(TAG, "Error updating spending image", task.getException());
                        throw task.getException();
                    }
                    Log.d(TAG, "Successfully updated image URL for spending: " + spendingId);
                    return null;
                });
    }

    // --- getAllActiveSpendings (Đã đúng) ---
    @Override
    public Task<List<Spending>> getAllActiveSpendings() {
        if (auth.getCurrentUser() == null) return Tasks.forException(new IllegalStateException("User not authenticated"));
        String userId = auth.getCurrentUser().getUid();
        return spendingsCollection
                .whereEqualTo(FirestoreConstants.FIELD_USER_ID, userId)
                .whereEqualTo(FIELD_IS_DELETED, false)
                .orderBy(FirestoreConstants.FIELD_SPENDING_DATE_TIME, Query.Direction.DESCENDING)
                .get()
                .continueWith(task -> {
                    if (!task.isSuccessful()) throw task.getException();
                    List<Spending> spendings = new ArrayList<>();
                    for (DocumentSnapshot doc : task.getResult()) {
                        Spending spending = doc.toObject(Spending.class);
                        if (spending != null) spendings.add(spending);
                    }
                    return spendings;
                });
    }

    // --- getDeletedSpendings (Đã đúng) ---
    @Override
    public Task<List<Spending>> getDeletedSpendings() {
        if (auth.getCurrentUser() == null) return Tasks.forException(new IllegalStateException("User not authenticated"));
        String userId = auth.getCurrentUser().getUid();
        return spendingsCollection
                .whereEqualTo(FirestoreConstants.FIELD_USER_ID, userId)
                .whereEqualTo(FIELD_IS_DELETED, true)
                .orderBy(FIELD_DELETED_AT, Query.Direction.DESCENDING) // Sắp xếp theo thời gian xóa
                .get()
                .continueWith(task -> {
                    if (!task.isSuccessful()) throw task.getException();
                    List<Spending> spendings = new ArrayList<>();
                    for (DocumentSnapshot doc : task.getResult()) {
                        Spending spending = doc.toObject(Spending.class);
                        if (spending != null) spendings.add(spending);
                    }
                    return spendings;
                });
    }

    // --- softDeleteSpending (Sửa lại tên trường nếu cần) ---
    @Override
    public Task<Void> softDeleteSpending(String spendingId) {
        if (auth.getCurrentUser() == null) return Tasks.forException(new IllegalStateException("User not authenticated"));
        if (spendingId == null || spendingId.isEmpty()) return Tasks.forException(new IllegalArgumentException("Spending ID cannot be null or empty"));

        Map<String, Object> updates = new HashMap<>();
        updates.put("isDeleted", true); // Đảm bảo tên trường đúng là "isDeleted"
        updates.put("deletedAt", System.currentTimeMillis()); // Đảm bảo tên trường đúng là "deletedAt"
        updates.put("updatedAt", System.currentTimeMillis()); // Cập nhật cả updatedAt

        return spendingsCollection.document(spendingId).update(updates)
                .continueWith(task -> {
                    if (!task.isSuccessful()) throw task.getException();
                    Log.d(TAG, "Successfully soft deleted spending: " + spendingId);
                    return null;
                });
    }

    // --- restoreSpending (Sửa lại tên trường nếu cần) ---
    @Override
    public Task<Void> restoreSpending(String spendingId) {
        if (auth.getCurrentUser() == null) return Tasks.forException(new IllegalStateException("User not authenticated"));
        if (spendingId == null || spendingId.isEmpty()) return Tasks.forException(new IllegalArgumentException("Spending ID cannot be null or empty"));

        Map<String, Object> updates = new HashMap<>();
        updates.put("isDeleted", false); // Đảm bảo tên trường đúng
        updates.put("deletedAt", null);   // Đảm bảo tên trường đúng và giá trị null
        updates.put("updatedAt", System.currentTimeMillis());

        return spendingsCollection.document(spendingId).update(updates)
                .continueWith(task -> {
                    if (!task.isSuccessful()) throw task.getException();
                    Log.d(TAG, "Successfully restored spending: " + spendingId);
                    return null;
                });
    }

    // --- hardDeleteSpending (Đã đúng tên) ---
    // @Override // Annotation này đã đúng vì interface có hardDeleteSpending
    // public Task<Void> hardDeleteSpending(String spendingId) { ... } // Logic bên trong giữ nguyên như đã sửa

    // --- getDeletedSpendingsCount (Đã đúng) ---
    @Override
    public Task<Integer> getDeletedSpendingsCount() {
        if (auth.getCurrentUser() == null) return Tasks.forException(new IllegalStateException("User not authenticated"));
        String userId = auth.getCurrentUser().getUid();
        return spendingsCollection
                .whereEqualTo(FirestoreConstants.FIELD_USER_ID, userId)
                .whereEqualTo(FIELD_IS_DELETED, true)
                .get()
                .continueWith(task -> {
                    if (!task.isSuccessful()) throw task.getException();
                    return task.getResult().size();
                });
    }

    // --- purgeOldDeletedSpendings (Đã đúng) ---
    @Override
    public Task<Void> purgeOldDeletedSpendings(Date date) {
        if (auth.getCurrentUser() == null) return Tasks.forException(new IllegalStateException("User not authenticated"));
        String userId = auth.getCurrentUser().getUid();
        long cutoffTime = date.getTime();
        return spendingsCollection
                .whereEqualTo(FirestoreConstants.FIELD_USER_ID, userId)
                .whereEqualTo(FIELD_IS_DELETED, true)
                .whereLessThan(FIELD_DELETED_AT, cutoffTime)
                .get()
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) throw task.getException();
                    List<Task<Void>> deleteTasks = new ArrayList<>();
                    for (DocumentSnapshot doc : task.getResult()) {
                        deleteTasks.add(doc.getReference().delete());
                    }
                    // Sử dụng whenAll chứ không phải whenAllComplete nếu bạn muốn trả về Task<Void>
                    return Tasks.whenAll(deleteTasks);
                });
    }
}

package com.example.quanlychitieu.views.calendar;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.quanlychitieu.domain.model.spending.Spending; // Đảm bảo import đúng lớp Spending đã cập nhật
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException; // Import để bắt lỗi Firestore

import java.util.ArrayList;
import java.util.List;
// Bỏ AtomicInteger nếu không dùng nữa
// import java.util.concurrent.atomic.AtomicInteger;

public class CalendarViewModel extends ViewModel {
    private static final String TAG = "CalendarViewModel"; // Thêm TAG để log
    private final FirebaseFirestore firestore;

    public CalendarViewModel() {
        firestore = FirebaseFirestore.getInstance();
    }

    public LiveData<List<Spending>> loadSpendingList(List<String> idList) {
        MutableLiveData<List<Spending>> resultLiveData = new MutableLiveData<>();

        if (idList == null || idList.isEmpty()) {
            Log.d(TAG, "Input ID list is null or empty. Returning empty list.");
            resultLiveData.setValue(new ArrayList<>()); // Trả về list rỗng ngay lập tức
            return resultLiveData;
        }

        Log.d(TAG, "Loading spending list for " + idList.size() + " IDs.");

        List<Task<DocumentSnapshot>> tasks = new ArrayList<>();
        for (String id : idList) {
            if (id != null && !id.isEmpty()) { // Kiểm tra ID hợp lệ
                // Tạo task để lấy từng document
                Task<DocumentSnapshot> task = firestore.collection("spending") // Đảm bảo đúng tên collection
                        .document(id)
                        .get();
                tasks.add(task);
            } else {
                Log.w(TAG, "Skipping null or empty ID in the list.");
            }
        }

        if (tasks.isEmpty()) {
            Log.d(TAG, "No valid IDs found to fetch. Returning empty list.");
            resultLiveData.setValue(new ArrayList<>());
            return resultLiveData;
        }

        // Chờ tất cả các task hoàn thành (thành công hoặc thất bại)
        Tasks.whenAllComplete(tasks)
                .addOnCompleteListener(allTasks -> {
                    List<Spending> spendingListResult = new ArrayList<>();
                    int successCount = 0;
                    int failCount = 0;
                    int notFoundCount = 0;
                    int parseFailCount = 0;

                    Log.d(TAG, "All spending fetch tasks completed. Processing results...");

                    for (Task<?> completedTask : allTasks.getResult()) {
                        if (completedTask.isSuccessful()) {
                            DocumentSnapshot document = (DocumentSnapshot) completedTask.getResult();
                            if (document != null && document.exists()) {
                                try {
                                    // SỬ DỤNG toObject() để chuyển đổi
                                    Spending spending = document.toObject(Spending.class);
                                    if (spending != null) {
                                        // ID sẽ được tự động điền bởi @DocumentId trong lớp Spending
                                        spendingListResult.add(spending);
                                        successCount++;
                                    } else {
                                        // Lỗi khi parse document thành object Spending
                                        Log.w(TAG, "Failed to parse document to Spending object: " + document.getId());
                                        parseFailCount++;
                                    }
                                } catch (Exception e) {
                                    // Bắt các lỗi khác trong quá trình parse (ví dụ: kiểu dữ liệu không khớp nghiêm trọng)
                                    Log.e(TAG, "Error parsing document " + document.getId() + " to Spending object", e);
                                    parseFailCount++;
                                }
                            } else {
                                // Document không tồn tại
                                Log.w(TAG, "Document snapshot was null or did not exist for a requested ID.");
                                notFoundCount++;
                            }
                        } else {
                            // Task thất bại (lỗi mạng, quyền truy cập,...)
                            Exception exception = completedTask.getException();
                            Log.e(TAG, "Failed to fetch a spending document.", exception);
                            failCount++;
                            // Có thể thêm thông tin chi tiết hơn về lỗi nếu cần
                            // if (exception instanceof FirebaseFirestoreException) {
                            //     FirebaseFirestoreException firestoreEx = (FirebaseFirestoreException) exception;
                            //     Log.e(TAG, "Firestore error code: " + firestoreEx.getCode());
                            // }
                        }
                    }

                    Log.d(TAG, "Processing complete. Success: " + successCount +
                            ", Parse Failed: " + parseFailCount +
                            ", Not Found: " + notFoundCount +
                            ", Fetch Failed: " + failCount);

                    // Cập nhật LiveData với danh sách kết quả (có thể là rỗng nếu tất cả lỗi)
                    // Sắp xếp lại theo ngày giảm dần nếu cần (vì whenAllComplete không đảm bảo thứ tự)
                    spendingListResult.sort((s1, s2) -> {
                        if (s1.getDateTime() == null && s2.getDateTime() == null) return 0;
                        if (s1.getDateTime() == null) return 1; // nulls last
                        if (s2.getDateTime() == null) return -1; // nulls last
                        return s2.getDateTime().compareTo(s1.getDateTime());
                    });
                    resultLiveData.postValue(spendingListResult); // Dùng postValue nếu có thể gọi từ background thread
                });

        return resultLiveData;
    }
}
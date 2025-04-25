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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * Implementation of FirestoreSpendingDataSource
 * Handles all spending-related Firestore operations
 */
public class FirestoreSpendingDataSourceImpl implements FirestoreSpendingDataSource {
  private static final String TAG = "FirestoreSpendingSource";

  private final FirebaseFirestore firestore;
  private final FirebaseStorage storage;
  private final FirebaseAuth auth;
  private final CollectionReference spendingsCollection;
  private final SimpleDateFormat dateFormat = new SimpleDateFormat("MM_yyyy", Locale.getDefault());
  private final Context context;

  public FirestoreSpendingDataSourceImpl(Context context) {
    this.context = context;
    this.firestore = FirebaseFirestore.getInstance();
    this.storage = FirebaseStorage.getInstance();
    this.auth = FirebaseAuth.getInstance();
    this.spendingsCollection = firestore.collection(FirestoreConstants.COLLECTION_SPENDINGS);
  }

  @Override
  public Task<String> addSpending(Spending spending) {
    // Lấy ID của người dùng hiện tại
    String userId = auth.getCurrentUser().getUid();

    // Tạo document reference mới cho spending
    DocumentReference spendingRef = spendingsCollection.document();
    String spendingId = spendingRef.getId();

    // Cập nhật ID cho spending
    spending.setId(spendingId);

    // Lấy dữ liệu của spending
    Map<String, Object> spendingData = spending.toMap();
    spendingData.put(FirestoreConstants.FIELD_USER_ID, userId);
    spendingData.put(FirestoreConstants.FIELD_CREATED_AT, System.currentTimeMillis());
    spendingData.put(FirestoreConstants.FIELD_UPDATED_AT, System.currentTimeMillis());

    // Lấy reference đến document data của user
    DocumentReference dataRef = firestore.collection(FirestoreConstants.COLLECTION_DATA)
        .document(userId);

    // Tạo task để cập nhật thông tin
    return dataRef.get().continueWithTask(task -> {
      if (!task.isSuccessful()) {
        throw task.getException();
      }

      // Lấy tháng từ ngày chi tiêu
      String monthKey = dateFormat.format(spending.getDateTime());

      // Lấy danh sách spending IDs hiện tại
      DocumentSnapshot dataSnapshot = task.getResult();
      List<String> spendingIds = new ArrayList<>();
      if (dataSnapshot.exists() && dataSnapshot.getData() != null &&
          dataSnapshot.getData().containsKey(monthKey)) {
        spendingIds = (List<String>) dataSnapshot.getData().get(monthKey);
      }

      if (spendingIds == null) {
        spendingIds = new ArrayList<>();
      }

      // Thêm ID mới vào danh sách
      List<String> finalSpendingIds = new ArrayList<>(spendingIds);
      finalSpendingIds.add(spendingId);

      // Lưu spending vào Firestore
      return spendingRef.set(spendingData)
          .continueWithTask(saveTask -> {
            if (!saveTask.isSuccessful()) {
              throw saveTask.getException();
            }

            // Cập nhật data để thêm ID vào tháng tương ứng
            return dataRef.update(monthKey, finalSpendingIds);
          })
          .continueWith(updateTask -> {
            if (!updateTask.isSuccessful()) {
              throw updateTask.getException();
            }

            return spendingId;
          });
    });
  }

  @Override
  public Task<String> uploadSpendingImage(String spendingId, Uri imageUri) {
    if (imageUri == null) {
      return Tasks.forException(new IllegalArgumentException("Image URI cannot be null"));
    }

    TaskCompletionSource<String> taskCompletionSource = new TaskCompletionSource<>();
    String userId = auth.getCurrentUser().getUid();

    CloudinaryManager.getInstance(context)
        .uploadImage(
            context,
            imageUri,
            "spendings/" + userId + "/" + spendingId,
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
  public Task<Spending> getSpending(String spendingId) {
    return spendingsCollection.document(spendingId).get()
        .continueWith(task -> {
          if (!task.isSuccessful()) {
            throw task.getException();
          }

          DocumentSnapshot document = task.getResult();
          if (document == null || !document.exists()) {
            return null;
          }

          return Spending.fromFirestore(document);
        });
  }

  @Override
  public Task<List<Spending>> getAllSpendings() {
    String userId = auth.getCurrentUser().getUid();

    return spendingsCollection
        .whereEqualTo(FirestoreConstants.FIELD_USER_ID, userId)
        .orderBy(FirestoreConstants.FIELD_SPENDING_DATE_TIME, Query.Direction.DESCENDING)
        .get()
        .continueWith(task -> {
          if (!task.isSuccessful()) {
            throw task.getException();
          }

          QuerySnapshot querySnapshot = task.getResult();
          List<Spending> spendings = new ArrayList<>();

          for (DocumentSnapshot document : querySnapshot.getDocuments()) {
            Spending spending = Spending.fromFirestore(document);
            if (spending != null) {
              spendings.add(spending);
            }
          }

          return spendings;
        });
  }

  @Override
  public Task<List<Spending>> getSpendingsByMonth(Date date) {
    String userId = auth.getCurrentUser().getUid();
    String monthKey = dateFormat.format(date);

    // Lấy danh sách IDs từ tài liệu "data"
    return firestore.collection(FirestoreConstants.COLLECTION_DATA)
        .document(userId)
        .get()
        .continueWithTask(task -> {
          if (!task.isSuccessful()) {
            throw task.getException();
          }

          DocumentSnapshot dataSnapshot = task.getResult();
          if (!dataSnapshot.exists() || dataSnapshot.getData() == null ||
              !dataSnapshot.getData().containsKey(monthKey)) {
            return Tasks.forResult(new ArrayList<>());
          }

          List<String> spendingIds = (List<String>) dataSnapshot.getData().get(monthKey);
          if (spendingIds == null || spendingIds.isEmpty()) {
            return Tasks.forResult(new ArrayList<>());
          }

          // Truy vấn tất cả spending theo IDs
          List<Task<DocumentSnapshot>> spendingTasks = new ArrayList<>();
          for (String id : spendingIds) {
            spendingTasks.add(spendingsCollection.document(id).get());
          }

          return Tasks.whenAllComplete(spendingTasks)
              .continueWith(spendingsTask -> {
                List<Spending> spendingList = new ArrayList<>();

                for (Task<DocumentSnapshot> docTask : spendingTasks) {
                  if (docTask.isSuccessful() && docTask.getResult() != null &&
                      docTask.getResult().exists()) {
                    Spending spending = Spending.fromFirestore(docTask.getResult());
                    if (spending != null) {
                      spendingList.add(spending);
                    }
                  }
                }

                return spendingList;
              });
        });
  }

  @Override
  public Task<List<Spending>> getSpendingsByDateRange(Date startDate, Date endDate) {
    String userId = auth.getCurrentUser().getUid();

    // Lấy tất cả các tháng giữa ngày bắt đầu và ngày kết thúc
    List<Date> monthsToQuery = getMonthsBetweenDates(startDate, endDate);
    List<Task<List<Spending>>> tasks = new ArrayList<>();

    // Truy vấn chi tiêu cho mỗi tháng
    for (Date month : monthsToQuery) {
      tasks.add(getSpendingsByMonth(month));
    }

    // Kết hợp tất cả các task và lọc theo khoảng thời gian cụ thể
    return Tasks.whenAllSuccess(tasks).continueWith(task -> {
      List<Spending> allSpendings = new ArrayList<>();

      for (Object result : task.getResult()) {
        allSpendings.addAll((List<Spending>) result);
      }

      // Lọc chi tiêu theo khoảng thời gian cụ thể
      List<Spending> filteredSpendings = new ArrayList<>();
      for (Spending spending : allSpendings) {
        Date spendingDate = spending.getDateTime();
        if (!spendingDate.before(startDate) && !spendingDate.after(endDate)) {
          filteredSpendings.add(spending);
        }
      }

      return filteredSpendings;
    });
  }

  @Override
  public Task<List<Spending>> getSpendingsByType(int type) {
    String userId = auth.getCurrentUser().getUid();

    return spendingsCollection
        .whereEqualTo(FirestoreConstants.FIELD_USER_ID, userId)
        .whereEqualTo(FirestoreConstants.FIELD_SPENDING_TYPE, type)
        .orderBy(FirestoreConstants.FIELD_SPENDING_DATE_TIME, Query.Direction.DESCENDING)
        .get()
        .continueWith(task -> {
          if (!task.isSuccessful()) {
            throw task.getException();
          }

          QuerySnapshot querySnapshot = task.getResult();
          List<Spending> spendings = new ArrayList<>();

          for (DocumentSnapshot document : querySnapshot.getDocuments()) {
            Spending spending = Spending.fromFirestore(document);
            if (spending != null) {
              spendings.add(spending);
            }
          }

          return spendings;
        });
  }

  @Override
  public Task<Void> updateSpending(Spending spending) {
    String userId = auth.getCurrentUser().getUid();

    // Lấy spending hiện tại để so sánh thời gian
    return spendingsCollection.document(spending.getId()).get()
        .continueWithTask(task -> {
          if (!task.isSuccessful()) {
            throw task.getException();
          }

          DocumentSnapshot spendingSnapshot = task.getResult();
          Map<String, Object> data = spendingSnapshot.getData();
          if (data == null) {
            return Tasks.forException(new IllegalStateException("Spending not found"));
          }

          // Lấy ngày cũ
          Date oldDate = null;
          if (data.containsKey(FirestoreConstants.FIELD_SPENDING_DATE_TIME)) {
            oldDate = ((com.google.firebase.Timestamp) data.get(FirestoreConstants.FIELD_SPENDING_DATE_TIME)).toDate();
          } else if (data.containsKey("date")) {
            oldDate = ((com.google.firebase.Timestamp) data.get("date")).toDate();
          }

          if (oldDate == null) {
            oldDate = new Date();
          }

          // Kiểm tra xem tháng có thay đổi hay không
          boolean monthChanged = !isSameMonth(oldDate, spending.getDateTime());
          Date finalOldDate = oldDate;

          // Nếu tháng thay đổi, cần cập nhật lại ở bảng data
          if (monthChanged) {
            return firestore.collection(FirestoreConstants.COLLECTION_DATA)
                .document(userId)
                .get()
                .continueWithTask(dataTask -> {
                  if (!dataTask.isSuccessful()) {
                    throw dataTask.getException();
                  }

                  DocumentSnapshot dataSnapshot = dataTask.getResult();
                  Map<String, Object> dataMap = dataSnapshot.getData();
                  if (dataMap == null) {
                    dataMap = new HashMap<>();
                  }

                  // Xóa ID khỏi tháng cũ
                  String oldMonthKey = dateFormat.format(finalOldDate);
                  List<String> oldMonthSpendingIds = (List<String>) dataMap.get(oldMonthKey);
                  if (oldMonthSpendingIds != null) {
                    oldMonthSpendingIds.remove(spending.getId());
                  }

                  // Thêm ID vào tháng mới
                  String newMonthKey = dateFormat.format(spending.getDateTime());
                  List<String> newMonthSpendingIds = dataMap.containsKey(newMonthKey)
                      ? (List<String>) dataMap.get(newMonthKey)
                      : new ArrayList<>();
                  if (newMonthSpendingIds == null) {
                    newMonthSpendingIds = new ArrayList<>();
                  }
                  if (!newMonthSpendingIds.contains(spending.getId())) {
                    newMonthSpendingIds.add(spending.getId());
                  }

                  // Cập nhật cả hai tháng
                  Map<String, Object> updates = new HashMap<>();
                  updates.put(oldMonthKey, oldMonthSpendingIds);
                  updates.put(newMonthKey, newMonthSpendingIds);

                  return firestore.collection(FirestoreConstants.COLLECTION_DATA)
                      .document(userId)
                      .update(updates);
                });
          }

          // Nếu tháng không thay đổi, không cần cập nhật ở bảng data
          return Tasks.forResult(null);
        })
        .continueWithTask(updateDataTask -> {
          // Cập nhật thông tin spending
          Map<String, Object> updates = spending.toMap();
          updates.put(FirestoreConstants.FIELD_UPDATED_AT, System.currentTimeMillis());

          return spendingsCollection.document(spending.getId()).update(updates);
        });
  }

  @Override
  public Task<Void> deleteSpending(String spendingId) {
    String userId = auth.getCurrentUser().getUid();

    // Lấy thông tin spending
    return spendingsCollection.document(spendingId).get()
        .continueWithTask(task -> {
          if (!task.isSuccessful()) {
            throw task.getException();
          }

          DocumentSnapshot spendingSnapshot = task.getResult();
          if (!spendingSnapshot.exists() || spendingSnapshot.getData() == null) {
            return Tasks.forResult(null);
          }

          Spending spending = Spending.fromFirestore(spendingSnapshot);
          if (spending == null) {
            return Tasks.forResult(null);
          }

          // Lấy tháng của spending
          String monthKey = dateFormat.format(spending.getDateTime());

          // Cập nhật bảng data để xóa ID
          return firestore.collection(FirestoreConstants.COLLECTION_DATA)
              .document(userId)
              .get()
              .continueWithTask(dataTask -> {
                if (!dataTask.isSuccessful()) {
                  throw dataTask.getException();
                }

                DocumentSnapshot dataSnapshot = dataTask.getResult();
                if (!dataSnapshot.exists() || dataSnapshot.getData() == null ||
                    !dataSnapshot.getData().containsKey(monthKey)) {
                  return Tasks.forResult(null);
                }

                List<String> spendingIds = (List<String>) dataSnapshot.getData().get(monthKey);
                if (spendingIds != null) {
                  spendingIds.remove(spendingId);
                  return firestore.collection(FirestoreConstants.COLLECTION_DATA)
                      .document(userId)
                      .update(monthKey, spendingIds);
                }

                return Tasks.forResult(null);
              })
              .continueWithTask(updateDataTask -> {
                // Xóa ảnh nếu có
                if (spending.getImage() != null && !spending.getImage().isEmpty()) {
                  // Lấy reference từ URL
                  StorageReference storageRef = storage.getReferenceFromUrl(spending.getImage());
                  return storageRef.delete()
                      .continueWithTask(deleteImageTask -> {
                        // Xóa document spending
                        return spendingsCollection.document(spendingId).delete();
                      });
                }

                // Nếu không có ảnh, chỉ xóa document spending
                return spendingsCollection.document(spendingId).delete();
              });
        });
  }

  /**
   * Lấy danh sách các tháng giữa hai ngày
   */
  private List<Date> getMonthsBetweenDates(Date startDate, Date endDate) {
    List<Date> months = new ArrayList<>();
    Calendar calendar = Calendar.getInstance();

    // Thiết lập tháng bắt đầu
    calendar.setTime(startDate);
    calendar.set(Calendar.DAY_OF_MONTH, 1);

    // Clone ngày kết thúc để so sánh
    Calendar endCalendar = Calendar.getInstance();
    endCalendar.setTime(endDate);

    // Thêm từng tháng vào danh sách
    while (calendar.get(Calendar.YEAR) <= endCalendar.get(Calendar.YEAR) &&
        calendar.get(Calendar.MONTH) <= endCalendar.get(Calendar.MONTH)) {
      months.add(calendar.getTime());

      // Chuyển sang tháng tiếp theo
      calendar.add(Calendar.MONTH, 1);
    }

    return months;
  }

  /**
   * Kiểm tra xem hai ngày có cùng tháng không
   */
  private boolean isSameMonth(Date date1, Date date2) {
    Calendar cal1 = Calendar.getInstance();
    Calendar cal2 = Calendar.getInstance();
    cal1.setTime(date1);
    cal2.setTime(date2);
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
        cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH);
  }

  @Override
  public Task<Integer> getTotalSpending(String userId, Date startDate, Date endDate) {
    return spendingsCollection
        .whereEqualTo(FirestoreConstants.FIELD_USER_ID, userId)
        .whereGreaterThanOrEqualTo(FirestoreConstants.FIELD_SPENDING_DATE_TIME, startDate)
        .whereLessThanOrEqualTo(FirestoreConstants.FIELD_SPENDING_DATE_TIME, endDate)
        .get()
        .continueWith(task -> {
          if (!task.isSuccessful()) {
            throw task.getException();
          }

          QuerySnapshot querySnapshot = task.getResult();
          int total = 0;

          for (DocumentSnapshot document : querySnapshot.getDocuments()) {
            Spending spending = Spending.fromFirestore(document);
            if (spending != null) {
              total += spending.getMoney();
            }
          }

          return total;
        });
  }

  @Override
  public Task<List<Object[]>> getSpendingGroupByCategory(String userId, Date startDate, Date endDate) {
    return spendingsCollection
        .whereEqualTo(FirestoreConstants.FIELD_USER_ID, userId)
        .whereGreaterThanOrEqualTo(FirestoreConstants.FIELD_SPENDING_DATE_TIME, startDate)
        .whereLessThanOrEqualTo(FirestoreConstants.FIELD_SPENDING_DATE_TIME, endDate)
        .get()
        .continueWith(task -> {
          if (!task.isSuccessful()) {
            throw task.getException();
          }

          QuerySnapshot querySnapshot = task.getResult();
          Map<Integer, Integer> categoryTotals = new HashMap<>();

          // Tính tổng cho mỗi category
          for (DocumentSnapshot document : querySnapshot.getDocuments()) {
            Spending spending = Spending.fromFirestore(document);
            if (spending != null) {
              int categoryId = spending.getType();
              int currentTotal = categoryTotals.getOrDefault(categoryId, 0);
              categoryTotals.put(categoryId, currentTotal + spending.getMoney());
            }
          }

          // Chuyển đổi Map thành List<Object[]>
          List<Object[]> result = new ArrayList<>();
          for (Map.Entry<Integer, Integer> entry : categoryTotals.entrySet()) {
            result.add(new Object[]{entry.getKey(), entry.getValue()});
          }

          return result;
        });
  }
}

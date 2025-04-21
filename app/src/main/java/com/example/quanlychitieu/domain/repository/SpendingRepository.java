package com.example.quanlychitieu.domain.repository;

import android.net.Uri;

import com.example.quanlychitieu.domain.model.spending.Spending;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Calendar;

public class SpendingRepository {
  private final FirebaseFirestore firestore;
  private final FirebaseStorage storage;
  private final FirebaseAuth auth;
  private final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MM_yyyy", Locale.getDefault());

  public SpendingRepository(FirebaseFirestore firestore) {
    this.firestore = firestore;
    this.storage = FirebaseStorage.getInstance();
    this.auth = FirebaseAuth.getInstance();
  }

  public Task<Void> addSpending(Spending spending, Uri imageUri) {
    // Tạo document mới cho spending
    final DocumentReference spendingRef = firestore.collection("spending").document();
    spending.setId(spendingRef.getId());

    // Lấy reference đến document data của user
    final DocumentReference dataRef = firestore.collection("data")
        .document(auth.getCurrentUser().getUid());

    // Cập nhật spending với id mới
    return dataRef.get().continueWithTask(task -> {
      if (!task.isSuccessful()) {
        throw task.getException();
      }

      DocumentSnapshot dataSnapshot = task.getResult();
      final String monthKey = DATE_FORMAT.format(spending.getDateTime());

      List<String> spendingIds = new ArrayList<>();
      if (dataSnapshot.exists() && dataSnapshot.getData() != null &&
          dataSnapshot.getData().containsKey(monthKey)) {
        spendingIds = (List<String>) dataSnapshot.getData().get(monthKey);
      }

      if (spendingIds == null) {
        spendingIds = new ArrayList<>();
      }

      // Thêm ID của spending mới vào danh sách
      final List<String> finalSpendingIds = new ArrayList<>(spendingIds);
      finalSpendingIds.add(spending.getId());

      // Nếu có hình ảnh, upload lên Firebase Storage
      if (imageUri != null) {
        final StorageReference imageRef = storage.getReference()
            .child("spending/" + spending.getId() + ".png");

        return imageRef.putFile(imageUri).continueWithTask(uploadTask -> {
          if (!uploadTask.isSuccessful()) {
            throw uploadTask.getException();
          }

          return imageRef.getDownloadUrl();
        }).continueWithTask(urlTask -> {
          if (!urlTask.isSuccessful()) {
            throw urlTask.getException();
          }

          // Lấy URL của ảnh đã upload
          spending.setImage(urlTask.getResult().toString());

          // Lưu spending vào Firestore
          return spendingRef.set(spending.toMap());
        }).continueWithTask(saveTask -> {
          if (!saveTask.isSuccessful()) {
            throw saveTask.getException();
          }

          // Cập nhật data để thêm ID của spending mới
          return dataRef.update(monthKey, finalSpendingIds);
        });
      } else {
        // Nếu không có hình ảnh, lưu spending luôn
        return spendingRef.set(spending.toMap())
            .continueWithTask(saveTask -> {
              if (!saveTask.isSuccessful()) {
                throw saveTask.getException();
              }

              // Cập nhật data để thêm ID của spending mới
              return dataRef.update(monthKey, finalSpendingIds);
            });
      }
    });
  }

  public Task<Void> updateSpending(Spending spending, Uri newImageUri, boolean isImageRemoved) {
    final DocumentReference spendingRef = firestore.collection("spending").document(spending.getId());
    final DocumentReference dataRef = firestore.collection("data")
        .document(auth.getCurrentUser().getUid());

    // Lấy spending hiện tại để so sánh thời gian
    return spendingRef.get().continueWithTask(task -> {
      if (!task.isSuccessful()) {
        throw task.getException();
      }

      DocumentSnapshot spendingSnapshot = task.getResult();
      Map<String, Object> data = spendingSnapshot.getData();
      final Date oldDate = ((com.google.firebase.Timestamp) data.get("date")).toDate();

      // Kiểm tra xem tháng có thay đổi hay không
      final boolean monthChanged = !isSameMonth(oldDate, spending.getDateTime());

      // Nếu tháng thay đổi, cần cập nhật lại collection data
      if (monthChanged) {
        return dataRef.get().continueWithTask(dataTask -> {
          if (!dataTask.isSuccessful()) {
            throw dataTask.getException();
          }

          DocumentSnapshot dataSnapshot = dataTask.getResult();
          Map<String, Object> dataMap = dataSnapshot.getData();

          // Xóa spending ID khỏi tháng cũ
          final String oldMonthKey = DATE_FORMAT.format(oldDate);
          List<String> oldMonthSpendingIds = (List<String>) dataMap.get(oldMonthKey);
          if (oldMonthSpendingIds != null) {
            oldMonthSpendingIds.remove(spending.getId());
          }

          // Thêm spending ID vào tháng mới
          final String newMonthKey = DATE_FORMAT.format(spending.getDateTime());
          List<String> newMonthSpendingIds = dataMap.containsKey(newMonthKey) ? (List<String>) dataMap.get(newMonthKey)
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

          return dataRef.update(updates);
        });
      }

      // Nếu tháng không thay đổi, không cần cập nhật collection data
      return Tasks.forResult(null);
    }).continueWithTask(updateDataTask -> {
      // Xử lý hình ảnh
      if (isImageRemoved) {
        // Nếu hình ảnh bị xóa
        final StorageReference imageRef = storage.getReference()
            .child("spending/" + spending.getId() + ".png");
        spending.setImage(null);

        return imageRef.delete().continueWithTask(deleteTask -> {
          return spendingRef.update(spending.toMap());
        });
      } else if (newImageUri != null) {
        // Nếu có hình ảnh mới
        final StorageReference imageRef = storage.getReference()
            .child("spending/" + spending.getId() + ".png");

        return imageRef.putFile(newImageUri).continueWithTask(uploadTask -> {
          return imageRef.getDownloadUrl();
        }).continueWithTask(urlTask -> {
          spending.setImage(urlTask.getResult().toString());
          return spendingRef.update(spending.toMap());
        });
      } else {
        // Nếu không thay đổi hình ảnh
        return spendingRef.update(spending.toMap());
      }
    });
  }

  public Task<Void> deleteSpending(Spending spending) {
    final DocumentReference spendingRef = firestore.collection("spending").document(spending.getId());
    final DocumentReference dataRef = firestore.collection("data")
        .document(auth.getCurrentUser().getUid());
    final String monthKey = DATE_FORMAT.format(spending.getDateTime());

    return dataRef.get().continueWithTask(task -> {
      if (!task.isSuccessful()) {
        throw task.getException();
      }

      DocumentSnapshot dataSnapshot = task.getResult();
      Map<String, Object> data = dataSnapshot.getData();

      if (data != null && data.containsKey(monthKey)) {
        List<String> spendingIds = (List<String>) data.get(monthKey);
        spendingIds.remove(spending.getId());
        return dataRef.update(monthKey, spendingIds);
      }

      return Tasks.forResult(null);
    }).continueWithTask(updateDataTask -> {
      // Xóa ảnh nếu có
      if (spending.getImage() != null) {
        final StorageReference imageRef = storage.getReference()
            .child("spending/" + spending.getId() + ".png");
        return imageRef.delete();
      }
      return Tasks.forResult(null);
    }).continueWithTask(deleteImageTask -> {
      // Xóa document spending
      return spendingRef.delete();
    });
  }

  public Task<List<Spending>> getSpendingByMonth(Date date) {
    final String userId = auth.getCurrentUser().getUid();
    final String monthKey = DATE_FORMAT.format(date);

    return firestore.collection("data")
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

          // Lấy tất cả spending theo ID
          return firestore.collection("spending")
              .whereIn("id", spendingIds)
              .get()
              .continueWith(queryTask -> {
                if (!queryTask.isSuccessful()) {
                  throw queryTask.getException();
                }

                List<Spending> spendingList = new ArrayList<>();
                for (DocumentSnapshot doc : queryTask.getResult().getDocuments()) {
                  spendingList.add(Spending.fromFirebase(doc));
                }
                return spendingList;
              });
        });
  }

  private boolean isSameMonth(Date date1, Date date2) {
    Calendar cal1 = Calendar.getInstance();
    Calendar cal2 = Calendar.getInstance();
    cal1.setTime(date1);
    cal2.setTime(date2);
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
        cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH);
  }
}

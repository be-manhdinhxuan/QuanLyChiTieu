package com.example.quanlychitieu.domain.repository;

import android.net.Uri;

import com.example.quanlychitieu.domain.model.spending.Spending;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
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

/**
 * Interface định nghĩa các hoạt động liên quan đến khoản chi tiêu
 */
public interface SpendingRepository {
  /**
   * Lấy tất cả khoản chi tiêu của người dùng hiện tại
   */
  Task<List<Spending>> getAllSpendings();

  /**
   * Lấy khoản chi tiêu trong khoảng thời gian
   */
  Task<List<Spending>> getSpendingsByDate(Date startDate, Date endDate);

  /**
   * Lấy khoản chi tiêu theo danh mục
   */
  Task<List<Spending>> getSpendingsByCategory(String categoryId);

  /**
   * Lấy chi tiết khoản chi tiêu theo ID
   */
  Task<Spending> getSpending(String spendingId);

  /**
   * Thêm khoản chi tiêu mới
   * 
   * @return ID của khoản chi tiêu mới
   */
  Task<String> addSpending(Spending spending);

  /**
   * Cập nhật khoản chi tiêu
   */
  Task<Void> updateSpending(Spending spending);

  /**
   * Xóa khoản chi tiêu
   */
  Task<Void> deleteSpending(String spendingId);

  /**
   * Đồng bộ hóa khoản chi tiêu từ remote đến local
   */
  Task<Void> syncSpendings();

  /**
   * Tính tổng chi tiêu trong khoảng thời gian
   */
  Task<Integer> getTotalSpending(Date startDate, Date endDate);

  /**
   * Lấy thống kê chi tiêu theo danh mục trong khoảng thời gian
   * 
   * @return List<Object[]> Mỗi phần tử là một mảng chứa [categoryId, tổng chi
   *         tiêu]
   */
  Task<List<Object[]>> getSpendingGroupByCategory(Date startDate, Date endDate);
}

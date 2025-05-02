package com.example.quanlychitieu.presentation.features.analytics.calendar.screen;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.quanlychitieu.R;
import com.example.quanlychitieu.databinding.FragmentCalendarBinding;
import com.example.quanlychitieu.domain.adapter.spending.SpendingAdapter;
import com.example.quanlychitieu.domain.model.spending.Spending;
import com.example.quanlychitieu.presentation.features.spending.list.screen.ViewListSpendingActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.*;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class CalendarFragment extends Fragment {
    private static final String TAG = "CalendarFragment";
    private FragmentCalendarBinding binding;
    private SpendingAdapter spendingAdapter;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private NumberFormat numberFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
    // Thêm biến để lưu listener registration
    private ListenerRegistration spendingListener;
    private Date currentSelectedDate; // Giữ lại để biết ngày đang chọn
    private String currentUserId; // Lưu userId để kiểm tra

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Lấy userId ban đầu, nhưng sẽ lấy lại khi gắn listener
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            currentUserId = user.getUid();
        } else {
            // Xử lý trường hợp user null ngay từ đầu nếu cần (ví dụ: chuyển login)
            Log.w(TAG, "User is null during onCreate.");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCalendarBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupViews();
        // Load data for current date by default
        currentSelectedDate = new Date(); // Đặt ngày hiện tại làm ngày mặc định
        loadDataForDate(new Date());
    }

    @Override
    public void onStart() {
        super.onStart();
        // Khi fragment quay lại foreground, load lại dữ liệu cho ngày đang chọn
        // để đảm bảo dữ liệu mới nhất (nếu không dùng listener real-time hoàn toàn)
        // Hoặc chỉ cần đảm bảo listener được gắn lại nếu cần
        if (currentSelectedDate != null) {
            Log.d(TAG, "onStart: Re-attaching listener for date: " + dateFormat.format(currentSelectedDate));
            loadDataForDate(currentSelectedDate); // Gắn lại listener cho ngày hiện tại
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        // Hủy listener khi fragment không còn hiển thị
        if (spendingListener != null) {
            Log.d(TAG, "onStop: Removing listener.");
            spendingListener.remove();
            spendingListener = null; // Đặt về null để biết listener đã bị hủy
        }
    }

    private void setupViews() {
        // Setup RecyclerView
        spendingAdapter = new SpendingAdapter();
        binding.spendingList.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.spendingList.setAdapter(spendingAdapter);

        // Setup click listener
        spendingAdapter.setOnSpendingClickListener(typeSpending -> {
            if (typeSpending == null || typeSpending.getSpendings() == null || 
                typeSpending.getSpendings().isEmpty()) {
                return;
            }
            
            // Tạo ArrayList<String> chứa các ID từ danh sách Spending
            ArrayList<String> spendingIds = new ArrayList<>();
            for (Spending spending : typeSpending.getSpendings()) {
                if (spending.getId() != null) {
                    spendingIds.add(spending.getId());
                }
            }

            if (spendingIds.isEmpty()) {
                Log.w(TAG, "No valid spending IDs found");
                return;
            }

            // Mở ViewListSpendingActivity với danh sách ID
            Intent intent = ViewListSpendingActivity.createIntent(
                requireContext(),
                spendingIds,
                typeSpending.getType(),
                typeSpending.getTypeName()
            );
            startActivity(intent);
        });

        // Setup CalendarView
        binding.calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            Calendar calendar = Calendar.getInstance();
            calendar.set(year, month, dayOfMonth);
            Date selectedDate = calendar.getTime();
            // Chỉ load lại nếu ngày thay đổi
            if (currentSelectedDate == null || !isSameDay(selectedDate, currentSelectedDate)) {
                Log.d(TAG, "Date changed in CalendarView: " + dateFormat.format(selectedDate));
                loadDataForDate(selectedDate);
            }

        });
    }

    // Hàm kiểm tra xem hai Date có cùng ngày, tháng, năm không
    private boolean isSameDay(Date date1, Date date2) {
        if (date1 == null || date2 == null) {
            return false;
        }
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date1);
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date2);
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }

    private void loadDataForDate(Date selectedDate) {
        // Lấy user và userId mới nhất ngay trước khi query
        FirebaseUser liveCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (liveCurrentUser == null) {
            Log.w(TAG, "User is null when trying to load data. Aborting.");
            updateUI(new ArrayList<>()); // Xóa dữ liệu cũ trên UI
            return;
        }
        String liveUserId = liveCurrentUser.getUid();
        currentUserId = liveUserId; // Cập nhật userId hiện tại

        // Lưu ngày được chọn
        currentSelectedDate = selectedDate;
        Log.d(TAG, "Loading data for date: " + dateFormat.format(selectedDate) + " User: " + liveUserId);

        // --- Tính toán startDate và endDate (giữ nguyên) ---
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(selectedDate);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        Date startDate = calendar.getTime();

        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        Date endDate = calendar.getTime();
        // --- Kết thúc tính toán startDate và endDate ---

        // Hủy listener cũ nếu có
        if (spendingListener != null) {
            spendingListener.remove();
            Log.d(TAG, "Removed previous listener.");
        }

        // *** Bắt đầu Query ***
        // Tạo query ban đầu
        Query query = FirebaseFirestore.getInstance()
                .collection("spending")
                .whereEqualTo("userId", liveUserId) // Dùng liveUserId
                .whereGreaterThanOrEqualTo("dateTime", startDate)
                .whereLessThanOrEqualTo("dateTime", endDate)
                .whereEqualTo("isDeleted", false);

        // Đăng ký listener mới vào query đã lọc
        spendingListener = query.addSnapshotListener((querySnapshot, error) -> {
            // *** THÊM KIỂM TRA NGAY LẬP TỨC ***
            if (!isAdded() || getContext() == null || binding == null) {
                Log.w(TAG, "onEvent received but Fragment state is invalid. Ignoring.");
                return; // Thoát sớm
            }
            // Kiểm tra xem ngày được chọn có còn khớp không (phòng trường hợp callback đến trễ sau khi đổi ngày)
            if (currentSelectedDate == null || !isSameDay(selectedDate, currentSelectedDate)) {
                Log.w(TAG, "onEvent received for a different date (" + dateFormat.format(selectedDate) +
                        ") than currently selected (" + (currentSelectedDate != null ? dateFormat.format(currentSelectedDate) : "null") + "). Ignoring.");
                return;
            }
            // *** KẾT THÚC KIỂM TRA ***

            if (error != null) {
                Log.e(TAG, "Error listening for spending updates for date: " + dateFormat.format(selectedDate), error);
                // *** XỬ LÝ LỖI PERMISSION_DENIED RIÊNG ***
                if (error.getCode() == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                    Log.w(TAG, "Permission denied during listen, likely due to logout. Suppressing UI error.");
                    // Không hiển thị Toast, nhưng cập nhật UI về trạng thái rỗng
                    updateUI(new ArrayList<>());
                } else if (error.getCode() == FirebaseFirestoreException.Code.FAILED_PRECONDITION && error.getMessage() != null && error.getMessage().contains("index")) {
                    Log.e(TAG, " Firestore query requires an index.", error);
                    showError(getString(R.string.error_firestore_index_required));
                    updateUI(new ArrayList<>()); // Cập nhật UI với danh sách rỗng khi có lỗi
                }
                else {
                    // Các lỗi khác thì hiển thị Toast
                    showError(getString(R.string.error_loading_data) + ": " + error.getMessage());
                    updateUI(new ArrayList<>());
                }
                return;
            }

            if (querySnapshot != null) {
                List<Spending> spendingList = new ArrayList<>();
                for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                    try {
                        Spending spending = document.toObject(Spending.class);

                        // *** THÊM KIỂM TRA isDeleted Ở CLIENT (Để chắc chắn) ***
                        if (spending != null && !spending.isDeleted()) {
                            // Chỉ thêm những spending chưa bị xóa
                            spendingList.add(spending);
                        } else if (spending != null) {
                            // Ghi log nếu có spending đã bị xóa lọt qua query (không nên xảy ra)
                            Log.w(TAG, "Skipping processing for deleted spending (Calendar): id=" + document.getId());
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing spending document: " + document.getId(), e);
                    }
                }

                Log.d(TAG, "Real-time update: Loaded " + spendingList.size() +
                        " non-deleted spendings for date: " + dateFormat.format(selectedDate));
                // Cập nhật UI với danh sách đã lọc
                updateUI(spendingList);
            } else {
                // Trường hợp querySnapshot là null (ít xảy ra nhưng nên xử lý)
                Log.w(TAG, "Snapshot was null for date: " + dateFormat.format(selectedDate));
                updateUI(new ArrayList<>());
            }
        });
        Log.d(TAG, "Attached new listener for date: " + dateFormat.format(selectedDate));
    }

    private void updateUI(List<Spending> spendingList) {
        // Thêm kiểm tra isAdded và binding trước khi thao tác UI
        if (!isAdded() || binding == null || getActivity() == null) {
            Log.w(TAG, "updateUI cancelled: Fragment not added, binding is null, or activity is null.");
            return;
        }

        getActivity().runOnUiThread(() -> {
            // Kiểm tra binding lại lần nữa trong runOnUiThread
            if (binding == null) return;

            if (spendingList == null || spendingList.isEmpty()) {
                showNoData();
                // Đảm bảo adapter cũng được cập nhật với list rỗng
                if (spendingAdapter != null) {
                    spendingAdapter.setItems(new ArrayList<>());
                }
            } else {
                hideNoData();
                Collections.sort(spendingList, (s1, s2) -> {
                    if (s1.getDateTime() == null) return 1;
                    if (s2.getDateTime() == null) return -1;
                    return s2.getDateTime().compareTo(s1.getDateTime());
                });
                if (spendingAdapter != null) {
                    spendingAdapter.setItems(spendingList); // Đảm bảo adapter có hàm setItems
                }
            }
        });
    }

    private void showNoData() {
        if (binding != null) {
            binding.spendingList.setVisibility(View.GONE);
            binding.textNoData.setVisibility(View.VISIBLE);
        }
    }

    private void hideNoData() {
        if (binding != null) {
            binding.spendingList.setVisibility(View.VISIBLE);
            binding.textNoData.setVisibility(View.GONE);
        }
    }

    // Hàm hiển thị lỗi Toast
    private void showError(String message) {
        if (getContext() != null && isAdded()) { // Kiểm tra cả context và isAdded
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Hủy listener khi view bị destroy
        if (spendingListener != null) {
            Log.d(TAG, "onDestroyView: Removing listener.");
            spendingListener.remove();
            spendingListener = null; // Quan trọng: đặt về null
        }
        binding = null; // Quan trọng: đặt binding về null
    }

    // Thêm phương thức để reload data
    public void reloadCurrentData() {
        if (currentSelectedDate != null) {
            Log.d(TAG, "Reloading data explicitly for date: " + dateFormat.format(currentSelectedDate));
            loadDataForDate(currentSelectedDate);
        } else {
            Log.w(TAG, "Cannot reload data, currentSelectedDate is null.");
            // Có thể load ngày hiện tại nếu muốn
            // loadDataForDate(new Date());
        }
    }
}

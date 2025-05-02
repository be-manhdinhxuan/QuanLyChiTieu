package com.example.quanlychitieu.presentation.features.analytics.main.screen;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
// Bỏ import không dùng: import android.widget.ImageButton;
// Bỏ import không dùng: import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.quanlychitieu.R;
import com.example.quanlychitieu.databinding.FragmentAnalyticBinding;
import com.example.quanlychitieu.domain.adapter.item.LegendAdapter;
import com.example.quanlychitieu.domain.adapter.item.OtherDetailsAdapter;
import com.example.quanlychitieu.domain.adapter.item.TransactionListAdapter;
import com.example.quanlychitieu.domain.constants.SpendingType; // Đảm bảo bạn có class/enum này
import com.example.quanlychitieu.domain.model.item.LegendItem;
import com.example.quanlychitieu.domain.model.spending.Spending; // Đảm bảo bạn có class này
import com.example.quanlychitieu.presentation.features.auth.login.screen.LoginActivity;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.*;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.MPPointF;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.*;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.EventListener;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class AnalyticFragment extends Fragment {
    private FragmentAnalyticBinding binding;
    private Calendar currentDate = Calendar.getInstance();
    // Thay đổi SimpleDateFormat để phù hợp với các định dạng khác nhau
    private SimpleDateFormat dayMonthYearFormatter = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private SimpleDateFormat monthYearFormatter = new SimpleDateFormat("MM/yyyy", Locale.getDefault());
    private SimpleDateFormat yearFormatter = new SimpleDateFormat("yyyy", Locale.getDefault());

    private NumberFormat numberFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String userId;
    private FirebaseAuth auth;
    // Danh sách chung để lưu trữ dữ liệu chi tiêu
    private List<Spending> spendingList = new ArrayList<>();

    // *** THÊM BIẾN ĐỂ QUẢN LÝ LISTENER ***
    private ListenerRegistration spendingListenerRegistration;
    private List<LegendItem> otherSpendingDetails = new ArrayList<>();

    private RecyclerView legendRecyclerView;
    private LegendAdapter legendAdapter;
    private List<LegendItem> legendDataList = new ArrayList<>(); // Danh sách dữ liệu cho legend
    private static final String TAG = "AnalyticFragment";


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        auth = FirebaseAuth.getInstance();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAnalyticBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        userId = getUserId();
        if (userId == null) {
            // Nếu không lấy được userId (chưa đăng nhập), không cần làm gì thêm ở đây
            // vì getUserId() đã xử lý chuyển màn hình
            return;
        }
        // Setup RecyclerView chú thích
        legendRecyclerView = binding.legendRecyclerView;
        legendAdapter = new LegendAdapter(legendDataList); // Khởi tạo adapter
        legendRecyclerView.setAdapter(legendAdapter);
        initViews();
        setupListeners(); // Gọi setupListeners
        setupCharts();
        setupChartToggle();
    }

    // *** THÊM onStart VÀ onStop ĐỂ QUẢN LÝ LISTENER ***
    @Override
    public void onStart() {
        super.onStart();
        Log.d("AnalyticFragment", "onStart: Starting listener.");
        // Bắt đầu lắng nghe dữ liệu khi Fragment hiển thị
        restartListener();
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d("AnalyticFragment", "onStop: Stopping listener.");
        // Dừng lắng nghe khi Fragment không còn hiển thị
        stopListeningForData();
    }

    private void initViews() {
        binding.timeTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                resetDateForTab(tab.getPosition());
                // Tải lại dữ liệu (thực chất là restart listener) khi chọn tab mới
                restartListener();
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}
            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void setupListeners() {
        binding.prevDateButton.setOnClickListener(v -> navigateDate(-1));
        binding.nextDateButton.setOnClickListener(v -> navigateDate(1));
    }

    private void resetDateForTab(int tabPosition) {
        currentDate = Calendar.getInstance(); // Lấy ngày hiện tại
        // Có thể thêm logic để đặt về đầu tuần/tháng/năm nếu muốn
    }

    private void setupCharts() {
        setupPieChart();
        setupBarChart();
    }

    private void setupChartToggle() {
        // Mặc định hiển thị biểu đồ tròn
        binding.pieChart.setVisibility(View.VISIBLE);
        binding.barChart.setVisibility(View.GONE);
        binding.pieChartButton.setSelected(true);
        binding.barChartButton.setSelected(false); // Đảm bảo nút bar không được chọn ban đầu

        binding.pieChartButton.setOnClickListener(v -> {
            if (!binding.pieChartButton.isSelected()) {
                binding.pieChart.setVisibility(View.VISIBLE);
                binding.barChart.setVisibility(View.GONE);
                binding.pieChartButton.setSelected(true);
                binding.barChartButton.setSelected(false);
                updateCurrentlyVisibleChart(); // Cập nhật ngay lập tức với dữ liệu hiện có
                // Không cần animate ở đây vì dữ liệu đã có sẵn
            }
        });

        binding.barChartButton.setOnClickListener(v -> {
            if (!binding.barChartButton.isSelected()) {
                binding.pieChart.setVisibility(View.GONE);
                binding.barChart.setVisibility(View.VISIBLE);
                binding.pieChartButton.setSelected(false);
                binding.barChartButton.setSelected(true);
                updateCurrentlyVisibleChart(); // Cập nhật ngay lập tức với dữ liệu hiện có
                // Không cần animate ở đây vì dữ liệu đã có sẵn
            }
        });
    }

    private void setupPieChart() {
        PieChart pieChart = binding.pieChart;
        pieChart.setUsePercentValues(true); // Giữ lại vì PercentFormatter cần
        pieChart.getDescription().setEnabled(false);
        pieChart.setExtraOffsets(5, 10, 5, 5);
        pieChart.setDragDecelerationFrictionCoef(0.95f);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(Color.WHITE);
        pieChart.setTransparentCircleRadius(61f);
        pieChart.setHoleRadius(58f);
        pieChart.setDrawCenterText(true);
        pieChart.setRotationEnabled(true);
        pieChart.setHighlightPerTapEnabled(true);
        pieChart.setMaxAngle(360f);

        // *** THAY ĐỔI ***: Ẩn chú thích màu (Legend)
        Legend l = pieChart.getLegend();
        l.setEnabled(false);

        // *** THAY ĐỔI ***: Ẩn tên loại chi tiêu (label) vẽ bên ngoài/gần slice
        pieChart.setDrawEntryLabels(false);
        // Không cần setEntryLabelColor nữa vì đã ẩn label

        pieChart.animateY(1300, Easing.EaseInOutQuad);

        pieChart.setCenterTextSize(16f);
        pieChart.setCenterTextColor(ContextCompat.getColor(requireContext(), R.color.primary));

        pieChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                // Thêm kiểm tra isAdded và data != null
                if (e instanceof PieEntry && isAdded() && e.getData() != null) {
                    PieEntry pe = (PieEntry) e;
                    try {
                        // *** Lấy type từ data của PieEntry ***
                        int selectedType = (int) pe.getData();

                        if (selectedType == SpendingType.OTHER) {
                            // Nhấn vào slice "Khác" -> Hiển thị các loại tổng hợp
                            showOtherSpendingDetailsDialog(); // Hàm này giả sử bạn đã có
                        } else {
                            // *** Nhấn vào slice thường ***
                            // -> Hiển thị danh sách giao dịch chi tiết
                            Log.d("AnalyticFragment", "Clicked on regular slice. Type: " + selectedType);
                            showTransactionListDialog(selectedType); // Gọi hàm mới sẽ tạo ở Bước 6
                        }
                    } catch (Exception ex) {
                        Log.e("AnalyticFragment", "Error processing selected pie entry data", ex);
                        // Có thể hiển thị dialog đơn giản nếu lỗi xảy ra
                        if (pe.getLabel() != null) {
                            double total = getTotalSpendingFromList();
                            double actualAmount = (pe.getValue() / 100.0) * total;
                            showSpendingTypeDetail(pe.getLabel(), actualAmount, pe.getValue());
                        }
                    }
                }
            }

            @Override
            public void onNothingSelected() {}
        });
    }

    private void setupBarChart() {
        BarChart barChart = binding.barChart;

        // Cấu hình cơ bản
        barChart.setDrawBarShadow(false);
        barChart.setDrawValueAboveBar(true);
        barChart.getDescription().setEnabled(false);
        barChart.setPinchZoom(false); // Tắt pinch zoom
        barChart.setDoubleTapToZoomEnabled(false); // Tắt double tap zoom
        barChart.setDrawGridBackground(false);
        barChart.setHighlightFullBarEnabled(false); // Chỉ highlight 1 cột

        // Cấu hình trục X
        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f); // Khoảng cách giữa các label là 1
        xAxis.setTextColor(Color.BLACK);
        xAxis.setTextSize(10f); // Giảm kích thước chữ nếu cần
        xAxis.setDrawAxisLine(true);
        xAxis.setLabelRotationAngle(-45); // Xoay label nếu bị chồng chéo

        // Cấu hình trục Y bên trái
        YAxis leftAxis = barChart.getAxisLeft();
        leftAxis.setDrawGridLines(true); // Vẽ lưới ngang
        leftAxis.setSpaceTop(35f); // Khoảng trống phía trên
        leftAxis.setAxisMinimum(0f); // Giá trị nhỏ nhất là 0
        leftAxis.setTextColor(Color.BLACK);
        leftAxis.setTextSize(12f);
        // Định dạng giá trị trục Y
        leftAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                // Hiển thị dạng rút gọn (ví dụ: 1K, 1M)
                if (value >= 1000000) {
                    return String.format(Locale.US, "%.1fM", value / 1000000.0);
                } else if (value >= 1000) {
                    return String.format(Locale.US, "%.0fK", value / 1000.0);
                } else {
                    return String.format(Locale.US, "%.0f", value);
                }
            }
        });


        // Tắt trục Y bên phải
        barChart.getAxisRight().setEnabled(false);

        // Cấu hình Legend (chú thích)
        Legend legend = barChart.getLegend();
        legend.setEnabled(false); // Tắt chú thích cho biểu đồ cột đơn giản

        // Thêm animation
        barChart.animateY(1000);

        // Thêm listener cho việc chọn giá trị
        barChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                if (e != null) {
                    float value = e.getY();
                    int index = (int) e.getX();
                    showBarChartDetail(index, value);
                }
            }
            @Override
            public void onNothingSelected() {}
        });
    }

    // Hiển thị chi tiết khi chọn cột
    private void showBarChartDetail(int index, float value) {
        String dateStr = getLabelForIndex(index); // Lấy nhãn (ngày/tháng/năm) cho index
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Chi tiết chi tiêu")
                .setMessage(String.format("Thời gian: %s\nChi tiêu: %s",
                        dateStr,
                        formatMoney(value))) // Dùng formatMoney đã có
                .setPositiveButton("Đóng", null)
                .show();
    }

    // Lấy nhãn cho trục X của BarChart dựa vào index và tab đang chọn
    private String getLabelForIndex(int index) {
        Calendar cal = (Calendar) currentDate.clone();
        int selectedTab = binding.timeTabLayout.getSelectedTabPosition();

        switch (selectedTab) {
            case 0: // Week
                // Điều chỉnh calendar về ngày đầu tuần và cộng thêm index ngày
                cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
                cal.add(Calendar.DAY_OF_MONTH, index);
                return dayMonthYearFormatter.format(cal.getTime());
            case 1: // Month
                // Điều chỉnh calendar về ngày đầu tháng và đặt ngày là index + 1
                cal.set(Calendar.DAY_OF_MONTH, 1); // Đặt về ngày 1 trước
                if (index < cal.getActualMaximum(Calendar.DAY_OF_MONTH)) {
                    cal.set(Calendar.DAY_OF_MONTH, index + 1); // Đặt ngày đúng
                    return dayMonthYearFormatter.format(cal.getTime());
                } else {
                    return String.valueOf(index + 1); // Trả về số nếu index > ngày cuối tháng
                }
            case 2: // Year
                // Điều chỉnh calendar về tháng đầu năm và đặt tháng là index
                cal.set(Calendar.MONTH, index);
                return monthYearFormatter.format(cal.getTime()); // Hiển thị MM/yyyy
            default:
                return "";
        }
    }

    // *** HÀM MỚI ĐỂ RESTART LISTENER ***
    private void restartListener() {
        stopListeningForData(); // Dừng listener cũ (nếu có)
        // Lấy lại userId mới nhất trước khi bắt đầu listener mới
        userId = getUserId();
        if (userId != null) {
            calculateDateRangeAndStartListener(); // Bắt đầu listener mới
        } else {
            Log.w(TAG, "Cannot restart listener because userId is null.");
            // Xóa dữ liệu cũ trên UI nếu người dùng đã đăng xuất
            spendingList.clear();
            updateCurrentlyVisibleChart();
        }
    }

    // *** HÀM MỚI ĐỂ TÍNH TOÁN NGÀY VÀ BẮT ĐẦU LISTENER ***
    private void calculateDateRangeAndStartListener() {
        if (binding == null || userId == null) {
            // Đảm bảo binding và userId tồn tại
            Log.w("AnalyticFragment", "Binding or UserId is null, cannot start listener.");
            return;
        }

        Calendar startDate = (Calendar) currentDate.clone();
        Calendar endDate = (Calendar) currentDate.clone();
        String currentPeriodText = "";

        int selectedTab = binding.timeTabLayout.getSelectedTabPosition();
        switch (selectedTab) {
            case 0: // Week
                startDate.set(Calendar.DAY_OF_WEEK, startDate.getFirstDayOfWeek());
                endDate = (Calendar) startDate.clone();
                endDate.add(Calendar.DAY_OF_WEEK, 6);
                currentPeriodText = String.format("Tuần %d, %s",
                        startDate.get(Calendar.WEEK_OF_YEAR),
                        yearFormatter.format(startDate.getTime()));
                break;
            case 1: // Month
                startDate.set(Calendar.DAY_OF_MONTH, 1);
                endDate.set(Calendar.DAY_OF_MONTH, endDate.getActualMaximum(Calendar.DAY_OF_MONTH));
                currentPeriodText = monthYearFormatter.format(startDate.getTime());
                break;
            case 2: // Year
                startDate.set(Calendar.DAY_OF_YEAR, 1);
                endDate.set(Calendar.DAY_OF_YEAR, endDate.getActualMaximum(Calendar.DAY_OF_YEAR));
                currentPeriodText = yearFormatter.format(startDate.getTime());
                break;
        }

        startDate.set(Calendar.HOUR_OF_DAY, 0);
        startDate.set(Calendar.MINUTE, 0);
        startDate.set(Calendar.SECOND, 0);
        startDate.set(Calendar.MILLISECOND, 0);

        endDate.set(Calendar.HOUR_OF_DAY, 23);
        endDate.set(Calendar.MINUTE, 59);
        endDate.set(Calendar.SECOND, 59);
        endDate.set(Calendar.MILLISECOND, 999);

        updateDateRangeText(currentPeriodText);
        // Bắt đầu lắng nghe với khoảng ngày đã tính toán
        startListeningForData(startDate.getTime(), endDate.getTime());
    }

    // Hàm cập nhật Text hiển thị khoảng thời gian
    private void updateDateRangeText(String text) {
        if (binding != null) {
            binding.dateRangeText.setText(text);
        }
    }

    // *** HÀM fetchSpendingData ĐƯỢC THAY THẾ BẰNG startListeningForData ***
    private void startListeningForData(Date startDate, Date endDate) {
        // Lấy lại user và userId ngay trước khi query
        FirebaseUser liveCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (liveCurrentUser == null) {
            Log.w(TAG, "User is null when trying to attach listener. Aborting.");
            spendingList.clear();
            updateCurrentlyVisibleChart();
            return;
        }
        String liveUserId = liveCurrentUser.getUid(); // Sử dụng UID mới nhất

        Log.d(TAG, "Attaching listener with User ID: " + liveUserId);
        Log.d(TAG, "Date range: " + startDate + " to " + endDate);

        // *** Bắt đầu Query ***
        // Tạo query ban đầu
        Query query = db.collection("spending")
                .whereEqualTo("userId", liveUserId) // Dùng liveUserId
                .whereGreaterThanOrEqualTo("dateTime", startDate)
                .whereLessThanOrEqualTo("dateTime", endDate)
                .whereEqualTo("isDeleted", false);

        // Gắn listener vào query đã lọc
        spendingListenerRegistration = query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot snapshots,
                                @Nullable FirebaseFirestoreException e) {
                // *** THÊM KIỂM TRA NGAY LẬP TỨC ***
                if (!isAdded() || getContext() == null || binding == null) {
                    Log.w(TAG, "onEvent received but Fragment state is invalid (not added, null context, or null binding). Ignoring.");
                    // Không cần remove listener ở đây vì onStop/onDestroyView sẽ xử lý
                    return;
                }

                if (e != null) {
                    // --- Xử lý lỗi (giữ nguyên) ---
                    Log.e("AnalyticFragment", "Listen failed.", e);
                    // Kiểm tra lỗi Permission Denied hoặc lỗi cần Index
                    if (e.getCode() == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                        Log.w(TAG, "Permission denied during listen, likely due to logout. Suppressing UI error.");
                        spendingList.clear();
                        updateCurrentlyVisibleChart(); // Cập nhật để hiển thị "Không có dữ liệu"
                    } else if (e.getCode() == FirebaseFirestoreException.Code.FAILED_PRECONDITION && e.getMessage() != null && e.getMessage().contains("index")) {
                        // *** XỬ LÝ LỖI THIẾU INDEX ***
                        Log.e(TAG, " Firestore query requires an index.", e);
                        spendingList.clear();
                        updateCurrentlyVisibleChart();
                        Toast.makeText(getContext(), "Lỗi truy vấn: Cần tạo Index trong Firestore (kiểm tra Logcat để lấy link)", Toast.LENGTH_LONG).show();

                    }
                    else {
                        Toast.makeText(getContext(),
                                "Lỗi kết nối: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                        spendingList.clear();
                        updateCurrentlyVisibleChart();
                    }
                    return;
                    // --- Kết thúc xử lý lỗi ---
                }

                // Xử lý dữ liệu khi thành công...
                if (snapshots != null) {
                    Log.d("AnalyticFragment", "Snapshot received with " + snapshots.size() + " documents.");
                    spendingList.clear(); // Xóa dữ liệu cũ

                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        try { // Bao quanh bằng try-catch để tránh crash nếu một document bị lỗi parse
                            Spending spending = doc.toObject(Spending.class);

                            // *** SỬA ĐIỀU KIỆN KIỂM TRA Ở ĐÂY ***
                            if (spending != null && spending.getDateTime() != null && !spending.isDeleted()) {
                                // Chỉ thêm spending hợp lệ VÀ chưa bị xóa
                                spendingList.add(spending);
                            } else if (spending != null && spending.isDeleted()) {
                                // Ghi log nếu có spending đã bị xóa lọt qua query (không nên xảy ra)
                                Log.w("AnalyticFragment", "Skipping already deleted spending: " + doc.getId());
                            }
                            else {
                                Log.w("AnalyticFragment", "Snapshot: Skipping document due to null object or dateTime: " + doc.getId());
                            }
                        } catch(Exception parseError) {
                            Log.e("AnalyticFragment", "Error parsing document " + doc.getId(), parseError);
                        }
                    } // Kết thúc vòng lặp for

                    Log.d("AnalyticFragment", "Listener updated spendingList with " + spendingList.size() + " NON-DELETED items.");
                    // Cập nhật biểu đồ đang hiển thị với dữ liệu mới từ snapshot (chỉ chứa item chưa xóa)
                    updateCurrentlyVisibleChart();
                } else {
                    Log.w("AnalyticFragment", "Snapshot was null.");
                    // Xử lý trường hợp snapshot null (ví dụ: xóa dữ liệu cũ)
                    spendingList.clear();
                    updateCurrentlyVisibleChart();
                }
            }
        });
    }

    // *** HÀM MỚI ĐỂ DỪNG LISTENER ***
    private void stopListeningForData() {
        if (spendingListenerRegistration != null) {
            spendingListenerRegistration.remove();
            spendingListenerRegistration = null;
            Log.d(TAG, "Listener stopped.");
        }
    }

    // *** HÀM MỚI ***
    // Cập nhật biểu đồ đang được hiển thị
    private void updateCurrentlyVisibleChart() {
        // Thêm kiểm tra isAdded() và getContext() để đảm bảo an toàn khi callback bất đồng bộ
        if (binding == null || !isAdded() || getContext() == null) {
            Log.w("AnalyticFragment", "updateCurrentlyVisibleChart cancelled: binding null or fragment not added/context null");
            return;
        }

        boolean isPieVisible = binding.pieChart.getVisibility() == View.VISIBLE;
        updateChartData(isPieVisible); // Gọi hàm cập nhật chính

        // Cập nhật trạng thái hiển thị của RecyclerView legend
        binding.legendRecyclerView.setVisibility(isPieVisible ? View.VISIBLE : View.GONE);
    }

    // *** HÀM MỚI ***
    // Tính tổng chi tiêu từ danh sách spendingList
    private double getTotalSpendingFromList() {
        double total = 0;
        for (Spending spending : spendingList) {
            if (spending.getMoney() < 0) { // Chỉ tính các khoản chi
                total += Math.abs(spending.getMoney());
            }
        }
        return total;
    }

    // *** HÀM MỚI HOẶC SỬA ĐỔI ***
    // Hiển thị chi tiết khi chọn phần trong PieChart
    private void showSpendingTypeDetail(String typeName, double actualAmount, float percentage) {
        if (!isAdded() || getContext() == null) return;
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(typeName)
                // Hiển thị cả số tiền và phần trăm
                .setMessage(String.format("Chi tiêu: %s (%.1f%%)", formatMoney(actualAmount), percentage))
                .setPositiveButton("Đóng", null)
                .show();
    }

    private void navigateDate(int value) {
        if (binding == null) return;

        int selectedTab = binding.timeTabLayout.getSelectedTabPosition();
        switch (selectedTab) {
            case 0: // Week
                currentDate.add(Calendar.WEEK_OF_YEAR, value);
                break;
            case 1: // Month
                currentDate.add(Calendar.MONTH, value);
                break;
            case 2: // Year
                currentDate.add(Calendar.YEAR, value);
                break;
        }
        // Tải lại dữ liệu (restart listener) cho khoảng thời gian mới
        restartListener();
    }

    // Hàm này không còn cần thiết vì đã có updateDateRangeText
    /*
    private void updateDateRange(Calendar startDate, Calendar endDate) {
        String dateRange = String.format("%s - %s",
            dayMonthYearFormatter.format(startDate.getTime()),
            dayMonthYearFormatter.format(endDate.getTime()));
        // binding.dateRangeText.setText(dateRange); // Cần có TextView này trong layout
    }
    */

    // *** SỬA ĐỔI QUAN TRỌNG TRONG SWITCH ***
    private String getSpendingTypeName(int type) {
        // Cần return giá trị trả về từ getString
        switch (type) {
            case SpendingType.EATING: return getString(R.string.eating);
            case SpendingType.TRANSPORTATION: return getString(R.string.move);
            case SpendingType.HOUSING: return getString(R.string.rent_house);
            case SpendingType.UTILITIES: return getString(R.string.water_money);
            case SpendingType.PHONE: return getString(R.string.telephone_fee);
            case SpendingType.ELECTRICITY: return getString(R.string.electricity_bill);
            case SpendingType.GAS: return getString(R.string.gas_money);
            case SpendingType.TV: return getString(R.string.tv_money);
            case SpendingType.INTERNET: return getString(R.string.internet_money);
            case SpendingType.FAMILY: return getString(R.string.family_service);
            case SpendingType.HOME_REPAIR: return getString(R.string.repair_and_decorate_the_house);
            case SpendingType.VEHICLE: return getString(R.string.vehicle_maintenance);
            case SpendingType.HEALTHCARE: return getString(R.string.physical_examination);
            case SpendingType.INSURANCE: return getString(R.string.insurance);
            case SpendingType.EDUCATION: return getString(R.string.education);
            case SpendingType.HOUSEWARES: return getString(R.string.housewares);
            case SpendingType.PERSONAL: return getString(R.string.personal_belongings);
            case SpendingType.PET: return getString(R.string.pet);
            case SpendingType.SPORTS: return getString(R.string.sport);
            case SpendingType.BEAUTY: return getString(R.string.beautify);
            case SpendingType.GIFTS: return getString(R.string.gifts_donations);
            case SpendingType.ENTERTAINMENT: return getString(R.string.fun_play);
            // Bỏ SpendingType.SHOPPING nếu không có trong enum/constants của bạn
            // case SpendingType.SHOPPING: return getString(R.string.shopping); // Ví dụ
            // Bỏ các loại thu nhập nếu không cần hiển thị tên trong context này
            // case SpendingType.SALARY: return getString(R.string.salary);
            // case SpendingType.BONUS: return getString(R.string.bonus);
            // case SpendingType.INVESTMENT_RETURN: return getString(R.string.investment_return);
            // case SpendingType.OTHER_INCOME: return getString(R.string.other_income);
            case SpendingType.OTHER:
            default:
                return getString(R.string.other);
        }
    }

    // Giữ nguyên hàm getSpendingTypeColor
    private int getSpendingTypeColor(int type) {
        // Sử dụng màu từ resources nếu có, hoặc giữ nguyên màu cố định
        // Ví dụ dùng màu từ resources: ContextCompat.getColor(requireContext(), R.color.eating_color)
        switch (type) {
            // Chi tiêu hàng tháng
            case SpendingType.EATING: return Color.rgb(255, 99, 71);        // Tomato
            case SpendingType.TRANSPORTATION: return Color.rgb(30, 144, 255);  // DodgerBlue
            case SpendingType.HOUSING: return Color.rgb(50, 205, 50);       // LimeGreen
            case SpendingType.UTILITIES: return Color.rgb(147, 112, 219);   // MediumPurple
            case SpendingType.PHONE: return Color.rgb(255, 165, 0);         // Orange
            case SpendingType.ELECTRICITY: return Color.rgb(255, 215, 0);   // Gold
            case SpendingType.GAS: return Color.rgb(218, 112, 214);        // Orchid
            case SpendingType.TV: return Color.rgb(0, 191, 255);           // DeepSkyBlue
            case SpendingType.INTERNET: return Color.rgb(65, 105, 225);    // RoyalBlue

            // Chi tiêu cần thiết
            case SpendingType.FAMILY: return Color.rgb(255, 182, 193);     // LightPink
            case SpendingType.HOME_REPAIR: return Color.rgb(160, 82, 45);  // Sienna
            case SpendingType.VEHICLE: return Color.rgb(119, 136, 153);    // LightSlateGray
            case SpendingType.HEALTHCARE: return Color.rgb(220, 20, 60);   // Crimson
            case SpendingType.INSURANCE: return Color.rgb(0, 139, 139);    // DarkCyan
            case SpendingType.EDUCATION: return Color.rgb(148, 0, 211);    // DarkViolet
            case SpendingType.HOUSEWARES: return Color.rgb(210, 180, 140); // Tan
            case SpendingType.PERSONAL: return Color.rgb(221, 160, 221);   // Plum
            case SpendingType.PET: return Color.rgb(244, 164, 96);         // SandyBrown

            // Giải trí và lifestyle
            case SpendingType.SPORTS: return Color.rgb(34, 139, 34);       // ForestGreen
            case SpendingType.BEAUTY: return Color.rgb(255, 20, 147);      // DeepPink
            case SpendingType.GIFTS: return Color.rgb(219, 112, 147);      // PaleVioletRed
            case SpendingType.ENTERTAINMENT: return Color.rgb(75, 0, 130);  // Indigo
            // Bỏ SpendingType.SHOPPING nếu không dùng
            // case SpendingType.SHOPPING: return Color.rgb(186, 85, 211);    // MediumOrchid

            // Bỏ các loại Thu nhập nếu không hiển thị màu cho chúng ở đây
            // case SpendingType.SALARY: return Color.rgb(46, 139, 87);       // SeaGreen
            // case SpendingType.BONUS: return Color.rgb(60, 179, 113);       // MediumSeaGreen
            // case SpendingType.INVESTMENT_RETURN: return Color.rgb(32, 178, 170); // LightSeaGreen
            // case SpendingType.OTHER_INCOME: return Color.rgb(143, 188, 143); // DarkSeaGreen

            // Khác
            case SpendingType.OTHER: return Color.rgb(128, 128, 128);      // Gray
            default: return Color.rgb(169, 169, 169);                      // DarkGray
        }
    }


    private String getUserId() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            Log.w("AnalyticFragment", "User not logged in, redirecting to LoginActivity.");
            navigateToLogin();
            return null; // Trả về null để nơi gọi biết không có user ID
        }
        Log.d("AnalyticFragment", "User ID: " + currentUser.getUid());
        return currentUser.getUid();
    }

    private void navigateToLogin() {
        if (getContext() == null || !isAdded()) return; // Thêm kiểm tra isAdded
        Intent loginIntent = new Intent(requireContext(), LoginActivity.class);
        // Xóa hết activity cũ trên stack và tạo task mới cho LoginActivity
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        // Không cần gọi finish() ở đây vì fragment không finish activity chứa nó
        // requireActivity().finish();
    }

    // Hàm animation có thể giữ nguyên nếu bạn muốn dùng
    private void addPulseAnimation(View view) {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 1.1f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 1.1f, 1f);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(scaleX, scaleY);
        animatorSet.setDuration(500); // Giảm thời gian animation
        animatorSet.setInterpolator(new AccelerateDecelerateInterpolator());

        // Bỏ lặp lại và delay nếu không cần
        // animatorSet.setStartDelay(500);
        // animatorSet.setRepeatCount(2); // Ví dụ lặp 2 lần
        animatorSet.start();
    }


    // *** CẬP NHẬT HÀM NÀY ***
    // Cập nhật dữ liệu cho biểu đồ (Pie hoặc Bar) và cả RecyclerView chú thích
    private void updateChartData(boolean isPieChart) {
        // Kiểm tra binding và fragment state một lần nữa ở đầu
        if (binding == null || !isAdded() || getContext() == null) return;

        // Luôn kiểm tra spendingList trước
        if (spendingList == null || spendingList.isEmpty()) {
            Log.d("AnalyticFragment", "spendingList is empty. Setting no data text.");
            String noDataText = "Không có dữ liệu chi tiêu";
            if (isPieChart) {
                binding.pieChart.setData(null);
                binding.pieChart.setNoDataText(noDataText);
                binding.pieChart.invalidate();
                binding.pieChart.setCenterText("");
                // Xóa dữ liệu legend khi không có data
                legendDataList.clear();
                legendAdapter.updateData(legendDataList);
            } else {
                binding.barChart.setData(null);
                binding.barChart.setNoDataText(noDataText);
                binding.barChart.invalidate();
                // Xóa dữ liệu legend khi không có data (nếu đang hiển thị BarChart)
                legendDataList.clear();
                legendAdapter.updateData(legendDataList);
            }
            // Ẩn/Hiện RecyclerView dựa trên trạng thái isPieChart (có thể đã làm trong updateCurrentlyVisibleChart)
            binding.legendRecyclerView.setVisibility(isPieChart ? View.VISIBLE : View.GONE);
            return; // Thoát sớm nếu không có dữ liệu
        }

        // Nếu có dữ liệu, gọi hàm cập nhật tương ứng
        Log.d("AnalyticFragment", "spendingList has data. Updating " + (isPieChart ? "PieChart & Legend" : "BarChart"));
        if (isPieChart) {
            updatePieChartData(); // Hàm này sẽ cập nhật cả PieChart và Legend
            binding.legendRecyclerView.setVisibility(View.VISIBLE); // Đảm bảo legend hiển thị
        } else {
            updateBarChartData();
            // Xóa dữ liệu legend khi hiển thị BarChart
            legendDataList.clear();
            if (legendAdapter != null) legendAdapter.updateData(legendDataList); // Kiểm tra null adapter
            binding.legendRecyclerView.setVisibility(View.GONE);
        }
    }


    // *** CẬP NHẬT HÀM NÀY ĐỂ TẠO CẢ DỮ LIỆU CHO LEGEND ***
    private void updatePieChartData() {
        // Thêm kiểm tra isAdded() và getContext() ở đầu để an toàn
        if (binding == null || !isAdded() || getContext() == null) return;

        final float MIN_PERCENT_THRESHOLD = 1.5f;
        Map<Integer, Double> spendingByType = new HashMap<>();
        double totalSpending = 0;
        otherSpendingDetails.clear();

        for (Spending spending : spendingList) {
            if (spending.getMoney() < 0) {
                double amount = Math.abs(spending.getMoney());
                spendingByType.merge(spending.getType(), amount, Double::sum);
                totalSpending += amount;
            }
        }
        Log.d("AnalyticFragment", "Calculated for Pie: totalSpending = " + totalSpending + ", types = " + spendingByType.size());

        // --- Xử lý khi không có chi tiêu ---
        if (totalSpending == 0) {
            Log.d("AnalyticFragment", "Total spending is zero. Setting no data text for PieChart.");
            binding.pieChart.setData(null);
            binding.pieChart.setNoDataText("Không có chi tiêu trong kỳ này");
            binding.pieChart.invalidate();
            binding.pieChart.setCenterText(formatMoney(0));
            // Cũng xóa dữ liệu legend
            legendDataList.clear();
            legendAdapter.updateData(legendDataList);
            return;
        }

        // --- Chuẩn bị dữ liệu cho PieChart và Legend ---
        ArrayList<PieEntry> finalEntries = new ArrayList<>();
        ArrayList<Integer> finalColors = new ArrayList<>();
        legendDataList.clear(); // Xóa dữ liệu legend cũ trước khi tạo mới
        float otherPercentageSum = 0f;
        List<Integer> otherTypes = new ArrayList<>();
        NumberFormat percentFormat = NumberFormat.getPercentInstance();
        percentFormat.setMaximumFractionDigits(1);

        // Sắp xếp để legend nhất quán (tùy chọn, nhưng nên làm)
        List<Map.Entry<Integer, Double>> sortedList = new ArrayList<>(spendingByType.entrySet());
        sortedList.sort((e1, e2) -> e2.getValue().compareTo(e1.getValue())); // Sắp xếp giảm dần

        // --- Tạo entries cho PieChart và data cho Legend ---
        for (Map.Entry<Integer, Double> entry : sortedList) {
            if (entry.getValue() > 0) {
                float percentage = (float) (entry.getValue() / totalSpending * 100);
                int type = entry.getKey(); // Lấy type
                int color = getSpendingTypeColor(type);
                String name = getSpendingTypeName(type);
                int iconResId = getSpendingTypeIconResId(type);

                if (percentage >= MIN_PERCENT_THRESHOLD) {
                    // Tạo PieEntry và LƯU TYPE vào data
                    PieEntry pieEntry = new PieEntry(percentage, name); // Bỏ icon khỏi PieEntry nếu không cần
                    pieEntry.setData(type); // *** LƯU TYPE VÀO ĐÂY ***
                    finalEntries.add(pieEntry);
                    finalColors.add(color);

                    // Tạo LegendItem cho RecyclerView chú thích bên ngoài
                    String percentValueText = percentFormat.format(percentage / 100.0);
                    legendDataList.add(new LegendItem(color, iconResId, name, percentValueText, entry.getValue()));

                } else {
                    // ... (Lưu chi tiết mục nhỏ vào otherSpendingDetails như cũ) ...
                    otherPercentageSum += percentage;
                    otherTypes.add(type);
                    double actualAmount = entry.getValue();
                    otherSpendingDetails.add(new LegendItem(color, iconResId, name, "", actualAmount));
                }
            }
        }

        // --- Xử lý mục "Khác" cho cả PieChart và Legend ---
        if (otherPercentageSum > 0) {
            String otherLabel = getString(R.string.other);
            int otherColor = getSpendingTypeColor(SpendingType.OTHER);
            int otherIconResId = getSpendingTypeIconResId(SpendingType.OTHER);

            // Tạo PieEntry cho "Khác" và LƯU TYPE OTHER
            PieEntry otherEntry = new PieEntry(otherPercentageSum, otherLabel);
            otherEntry.setData(SpendingType.OTHER); // *** LƯU TYPE OTHER VÀO ĐÂY ***
            finalEntries.add(otherEntry);
            finalColors.add(otherColor);

            // Tạo LegendItem cho "Khác"
            String otherValueText = percentFormat.format(otherPercentageSum / 100.0);
            legendDataList.add(new LegendItem(otherColor, otherIconResId, otherLabel, otherValueText, (otherPercentageSum / 100.0) * totalSpending));

            Log.d("AnalyticFragment", "Grouped " + otherTypes.size() + " small slices into 'Other'");
        }

        Log.d("AnalyticFragment", "Created " + finalEntries.size() + " final entries for PieChart.");
        Log.d("AnalyticFragment", "Created " + legendDataList.size() + " final items for Legend RecyclerView.");

        // --- Cập nhật PieChart ---
        if (finalEntries.isEmpty()) {
            Log.d("AnalyticFragment", "All entries were below threshold or invalid. Setting no data.");
            binding.pieChart.setData(null);
            binding.pieChart.setNoDataText("Không có dữ liệu đủ lớn để hiển thị");
            binding.pieChart.invalidate();
            binding.pieChart.setCenterText(formatMoney(totalSpending));
            // Xóa legend nếu entries rỗng
            legendDataList.clear();
            legendAdapter.updateData(legendDataList);
            return;
        }

        PieDataSet dataSet = new PieDataSet(finalEntries, "");
        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(5f);
        dataSet.setColors(finalColors);
        //dataSet.setDrawIcons(true); // Không cần vẽ icon trên slice nữa
        dataSet.setDrawValues(false); // Vẫn ẩn giá trị trên slice
        //dataSet.setIconsOffset(new MPPointF(0f, 0f)); // Không cần offset nữa

        PieData data = new PieData(dataSet);
        binding.pieChart.setData(data);
        binding.pieChart.setCenterText(formatMoney(totalSpending));
        binding.pieChart.highlightValues(null);
        binding.pieChart.invalidate();

        // --- Cập nhật RecyclerView Chú thích ---
        if (legendAdapter != null) {
            legendAdapter.updateData(legendDataList);
        }
    }

    // *** ĐÃ SỬA ***
    // Cập nhật dữ liệu cho biểu đồ cột (đọc từ spendingList)
    private void updateBarChartData() {
        if (binding == null || !isAdded() || getContext() == null) return; // Kiểm tra lại

        // Hàm calculateSpendingByPeriod đã đọc từ spendingList
        int selectedTab = binding.timeTabLayout.getSelectedTabPosition();
        Map<Integer, Float> spendingByPeriod = calculateSpendingByPeriod(selectedTab);

        ArrayList<BarEntry> entries = new ArrayList<>();
        int maxPeriods = getNumberOfPeriods(selectedTab); // Lấy số lượng khoảng thời gian tối đa

        boolean hasData = false; // Biến kiểm tra xem có dữ liệu không
        for (int i = 0; i < maxPeriods; i++) {
            float value = spendingByPeriod.getOrDefault(i, 0f);
            entries.add(new BarEntry(i, value));
            if (value > 0) {
                hasData = true; // Đánh dấu là có dữ liệu
            }
        }

        Log.d("AnalyticFragment", "Created " + entries.size() + " entries for BarChart. Has data: " + hasData);


        if (!hasData) {
            Log.d("AnalyticFragment", "No spending data for BarChart periods. Setting no data text.");
            binding.barChart.setData(null); // Xóa dữ liệu cũ
            binding.barChart.setNoDataText("Không có chi tiêu trong kỳ này");
            binding.barChart.invalidate();
            return;
        }

        // Cấu hình lại trục X mỗi lần cập nhật dữ liệu
        XAxis xAxis = binding.barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(getLabelsForPeriod(selectedTab)));
        xAxis.setLabelCount(maxPeriods, false); // Đặt số lượng label

        // Tạo và cấu hình dataset
        BarDataSet dataSet = new BarDataSet(entries, "Chi tiêu");
        dataSet.setColor(ContextCompat.getColor(requireContext(), R.color.primary));
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setValueTextSize(10f); // Giảm kích thước chữ trên cột
        // Định dạng giá trị trên cột (chỉ hiển thị nếu > 0)
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                // Chỉ hiển thị giá trị > 0 và rút gọn
                if (value <= 0) return "";
                if (value >= 1000000) {
                    return String.format(Locale.US, "%.1fM", value / 1000000.0);
                } else if (value >= 1000) {
                    return String.format(Locale.US, "%.0fK", value / 1000.0);
                } else {
                    return String.format(Locale.US, "%.0f", value);
                }
            }
        });
        dataSet.setDrawValues(true); // Luôn vẽ giá trị

        // Cập nhật biểu đồ
        BarData barData = new BarData(dataSet);
        // Điều chỉnh độ rộng cột dựa trên số lượng cột
        float barWidth = Math.max(0.1f, Math.min(0.8f, 1f / maxPeriods * 5f)); // Công thức ví dụ
        barData.setBarWidth(barWidth);

        binding.barChart.setData(barData);
        binding.barChart.setFitBars(true); // Tự điều chỉnh khoảng cách cột
        binding.barChart.getDescription().setEnabled(false);
        binding.barChart.highlightValues(null); // Bỏ highlight cũ
        binding.barChart.invalidate(); // Vẽ lại biểu đồ
        // Không cần gọi animate ở đây
    }

    // *** HÀM MỚI ***
    // Lấy số lượng khoảng thời gian (ngày/tháng) tối đa cho tab hiện tại
    private int getNumberOfPeriods(int selectedTab) {
        Calendar tempCal = (Calendar) currentDate.clone();
        switch (selectedTab) {
            case 0: // Week
                return 7; // Luôn là 7 ngày
            case 1: // Month
                return tempCal.getActualMaximum(Calendar.DAY_OF_MONTH); // Số ngày thực tế trong tháng
            case 2: // Year
                return 12; // Luôn là 12 tháng
            default:
                return 0;
        }
    }

    // Lấy nhãn cho các khoảng thời gian
    private String[] getLabelsForPeriod(int selectedTab) {
        int maxPeriods = getNumberOfPeriods(selectedTab);
        String[] labels = new String[maxPeriods];
        Calendar cal = (Calendar) currentDate.clone();

        switch (selectedTab) {
            case 0: // Week
                cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
                SimpleDateFormat weekDayFormat = new SimpleDateFormat("EEE", Locale.getDefault()); // Format "Mon", "Tue"...
                for (int i = 0; i < maxPeriods; i++) {
                    labels[i] = weekDayFormat.format(cal.getTime());
                    cal.add(Calendar.DAY_OF_MONTH, 1);
                }
                return labels;
            // return new String[]{"T2", "T3", "T4", "T5", "T6", "T7", "CN"}; // Hoặc giữ nguyên tiếng Việt
            case 1: // Month
                for (int i = 0; i < maxPeriods; i++) {
                    labels[i] = String.valueOf(i + 1); // Các ngày 1, 2, 3...
                }
                return labels;
            case 2: // Year
                // return new String[]{"T1", "T2", "T3", "T4", "T5", "T6", "T7", "T8", "T9", "T10", "T11", "T12"};
                SimpleDateFormat monthFormat = new SimpleDateFormat("MMM", Locale.getDefault()); // Format "Jan", "Feb"...
                cal.set(Calendar.MONTH, Calendar.JANUARY); // Bắt đầu từ tháng 1
                for (int i = 0; i < maxPeriods; i++) {
                    labels[i] = monthFormat.format(cal.getTime());
                    cal.add(Calendar.MONTH, 1);
                }
                return labels;

            default:
                return new String[]{};
        }
    }

    // Tính toán chi tiêu theo khoảng thời gian (Ngày trong tuần, Ngày trong tháng, Tháng trong năm)
    private Map<Integer, Float> calculateSpendingByPeriod(int selectedTab) {
        Map<Integer, Float> spendingByPeriod = new HashMap<>();
        Calendar tempCal = Calendar.getInstance(); // Calendar tạm để lấy thông tin ngày/tháng/năm

        for (Spending spending : spendingList) {
            if (spending.getMoney() >= 0 || spending.getDateTime() == null) continue; // Bỏ qua thu nhập và spending không có ngày giờ

            tempCal.setTime(spending.getDateTime());

            int key;
            switch (selectedTab) {
                case 0: // Week
                    // Key là index của ngày trong tuần (0=Thứ 2, 1=Thứ 3,... 6=Chủ nhật)
                    // Cần điều chỉnh dựa trên getFirstDayOfWeek() của Locale
                    int dayOfWeek = tempCal.get(Calendar.DAY_OF_WEEK); // SUNDAY=1, MONDAY=2... SATURDAY=7
                    int firstDay = currentDate.getFirstDayOfWeek(); // VD: SUNDAY=1
                    key = (dayOfWeek - firstDay + 7) % 7; // Tính toán index 0-6
                    break;
                case 1: // Month
                    key = tempCal.get(Calendar.DAY_OF_MONTH) - 1; // Key là index ngày (0-30)
                    break;
                case 2: // Year
                    key = tempCal.get(Calendar.MONTH); // Key là index tháng (0-11)
                    break;
                default:
                    continue; // Bỏ qua nếu tab không hợp lệ
            }

            float amount = Math.abs(spending.getMoney()); // Luôn dùng giá trị dương
            spendingByPeriod.merge(key, amount, Float::sum); // Cộng dồn số tiền
        }
        Log.d("AnalyticFragment", "Calculated spending by period (" + selectedTab + "): " + spendingByPeriod.size() + " periods with data.");
        return spendingByPeriod;
    }

    // Định dạng tiền tệ
    private String formatMoney(double amount) {
        return numberFormat.format(amount);
    }
    // Overload cho kiểu float
    private String formatMoney(float amount) {
        return numberFormat.format(amount);
    }

    // Lấy màu cho biểu đồ (có thể dùng lại getSpendingTypeColor nếu muốn)
    private int[] getChartColors() {
        // Lấy màu từ resources để dễ quản lý
        List<Integer> colors = new ArrayList<>();
        colors.add(ContextCompat.getColor(requireContext(), R.color.chart_color_1));
        colors.add(ContextCompat.getColor(requireContext(), R.color.chart_color_2));
        colors.add(ContextCompat.getColor(requireContext(), R.color.chart_color_3));
        colors.add(ContextCompat.getColor(requireContext(), R.color.chart_color_4));
        colors.add(ContextCompat.getColor(requireContext(), R.color.chart_color_5));

        // Thêm các màu khác nếu cần
        // ...

        int[] colorArray = new int[colors.size()];
        for (int i=0; i < colors.size(); i++) {
            colorArray[i] = colors.get(i);
        }
        return colorArray;
    }

    private int getSpendingTypeIconResId(int type) {
        switch (type) {
            // Ánh xạ từ SpendingType sang R.drawable.icon_cua_ban
            case SpendingType.EATING: return R.drawable.ic_eat;
            case SpendingType.TRANSPORTATION: return R.drawable.ic_taxi;
            case SpendingType.HOUSING: return R.drawable.ic_house;
            case SpendingType.UTILITIES: return R.drawable.ic_water;
            case SpendingType.PHONE: return R.drawable.ic_phone;
            case SpendingType.ELECTRICITY: return R.drawable.ic_electricity;
            case SpendingType.GAS: return R.drawable.ic_gas;
            case SpendingType.TV: return R.drawable.ic_tv;
            case SpendingType.INTERNET: return R.drawable.ic_internet;
            case SpendingType.FAMILY: return R.drawable.ic_family;
            case SpendingType.HOME_REPAIR: return R.drawable.ic_house_2;
            case SpendingType.VEHICLE: return R.drawable.ic_tools;
            case SpendingType.HEALTHCARE: return R.drawable.ic_doctor;
            case SpendingType.INSURANCE: return R.drawable.ic_health_insurance;
            case SpendingType.EDUCATION: return R.drawable.ic_education;
            case SpendingType.HOUSEWARES: return R.drawable.ic_armchair;
            case SpendingType.PERSONAL: return R.drawable.ic_toothbrush;
            case SpendingType.PET: return R.drawable.ic_pet;
            case SpendingType.SPORTS: return R.drawable.ic_sports;
            case SpendingType.BEAUTY: return R.drawable.ic_diamond;
            case SpendingType.GIFTS: return R.drawable.ic_give_love;
            case SpendingType.ENTERTAINMENT: return R.drawable.ic_game_pad;
            case SpendingType.OTHER:
            default:
                return R.drawable.ic_box; // Icon mặc định cho "Khác"
        }
    }

    private void showTransactionListDialog(int selectedType) {
        // Kiểm tra trước khi thực hiện
        if (!isAdded() || getContext() == null) {
            Log.w("AnalyticFragment", "Cannot show transactions: Fragment not added or context null.");
            return;
        }

        Log.d("AnalyticFragment", "Filtering transactions for type: " + selectedType);
        List<Spending> filteredTransactions = new ArrayList<>();
        for (Spending s : spendingList) { // Duyệt qua danh sách gốc hiện tại
            if (s.getType() == selectedType && s.getMoney() < 0) {
                filteredTransactions.add(s);
            }
        }
        Log.d("AnalyticFragment", "Found " + filteredTransactions.size() + " transactions.");

        if (filteredTransactions.isEmpty()) {
            Toast.makeText(getContext(), "Không có giao dịch chi tiết cho mục này", Toast.LENGTH_SHORT).show();
            return;
        }

        // Sắp xếp theo ngày giờ giảm dần (mới nhất lên đầu)
        filteredTransactions.sort((s1, s2) -> {
            if (s1.getDateTime() == null && s2.getDateTime() == null) return 0;
            if (s1.getDateTime() == null) return 1;
            if (s2.getDateTime() == null) return -1;
            return s2.getDateTime().compareTo(s1.getDateTime());
        });

        // Tạo Dialog
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_transaction_list, null); // Inflate layout dialog

        RecyclerView rvTransactions = dialogView.findViewById(R.id.transactionListRecyclerView);
        TransactionListAdapter transactionAdapter = new TransactionListAdapter(requireContext(), filteredTransactions); // Dùng Adapter mới

        rvTransactions.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvTransactions.setAdapter(transactionAdapter);

        builder.setView(dialogView)
                .setTitle("Chi tiết: " + getSpendingTypeName(selectedType)) // Đặt tiêu đề
                .setPositiveButton("Đóng", (dialog, which) -> dialog.dismiss());

        Log.d("AnalyticFragment", "Showing transaction list dialog.");
        builder.create().show();
    }

    // Thêm lại hàm này vào class AnalyticFragment
    private void showOtherSpendingDetailsDialog() {
        if (!isAdded() || getContext() == null || otherSpendingDetails.isEmpty()) {
            Log.w("AnalyticFragment", "Cannot show 'Other' details: Fragment not added, context null, or details list empty.");
            if (otherSpendingDetails.isEmpty() && isAdded() && getContext() != null) { // Check thêm isAdded/context trước khi Toast
                Toast.makeText(getContext(), "Không có chi tiết cho mục 'Khác'", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        // Sắp xếp danh sách chi tiết theo số tiền giảm dần (tùy chọn)
        otherSpendingDetails.sort((item1, item2) -> Double.compare(item2.getActualAmount(), item1.getActualAmount()));

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        // Đảm bảo bạn đã tạo layout dialog_other_details.xml
        View dialogView = inflater.inflate(R.layout.dialog_other_details, null);

        RecyclerView rvDetails = dialogView.findViewById(R.id.otherDetailsRecyclerView); // Đảm bảo ID đúng
        // Giả sử bạn đã tạo OtherDetailsAdapter hoặc dùng LegendAdapter nếu phù hợp
        // Quan trọng: Cần Context để khởi tạo NumberFormat trong Adapter (nếu adapter cần)
        OtherDetailsAdapter detailsAdapter = new OtherDetailsAdapter(requireContext(), otherSpendingDetails); // Giả sử có Adapter này

        rvDetails.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvDetails.setAdapter(detailsAdapter);

        builder.setView(dialogView) // Set view tùy chỉnh cho dialog
                .setTitle("Chi tiết mục Khác") // Set tiêu đề
                .setPositiveButton("Đóng", (dialog, which) -> dialog.dismiss()); // Nút đóng

        builder.create().show(); // Tạo và hiển thị dialog
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d("AnalyticFragment", "onDestroyView: Stopping listener and clearing binding.");
        stopListeningForData(); // **QUAN TRỌNG:** Dừng listener khi view bị hủy
        binding = null;
    }
}
package com.example.quanlychitieu.presentation.features.main.home.screen;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull; // Thêm import
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.quanlychitieu.R;
import com.example.quanlychitieu.databinding.FragmentHomeBinding;
import com.example.quanlychitieu.domain.adapter.spending.SpendingAdapter;
import com.example.quanlychitieu.domain.model.spending.Spending;
import com.example.quanlychitieu.presentation.features.auth.login.screen.LoginActivity;
import com.example.quanlychitieu.presentation.features.profile.edit.screen.EditProfileActivity;
import com.example.quanlychitieu.presentation.features.spending.add.screen.AddSpendingActivity;
// Import EditSpendingActivity để lấy request code nếu cần
import com.example.quanlychitieu.presentation.features.spending.edit.screen.EditSpendingActivity;
import com.example.quanlychitieu.presentation.features.spending.list.screen.ViewListSpendingActivity;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.*;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class HomeFragment extends Fragment {
    // *** THÊM REQUEST CODE CHO EDIT SPENDING ***
    private static final int REQUEST_EDIT_SPENDING = 101; // Đảm bảo mã này khớp khi gọi startActivityForResult
    private static final int REQUEST_VIEW_SPENDING = 102; // Thêm REQUEST_VIEW_SPENDING

    private static final int REQUEST_ADD_SPENDING = 100;
    private static final int REQUEST_EDIT_PROFILE_FOR_BUDGET = 1003;
    private static final String TAG = "HomeFragment";
    private FragmentHomeBinding binding;
    private SpendingAdapter adapter;
    private List<Date> months = new ArrayList<>();
    private String currentMonthKey;
    private SimpleDateFormat firestoreDateFormat = new SimpleDateFormat("MM_yyyy", Locale.getDefault());
    private NumberFormat numberFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
    private FirebaseAuth auth;
    private FirebaseUser currentUser;
    private ListenerRegistration transactionListener;
    private double initialBudgetBalance = 0.0;
    private List<Spending> currentDisplayedSpendings = new ArrayList<>();

    private static final String BALANCE_UPDATED = "balance_updated";
    private static final String PREFS_NAME = "HomeFragmentPrefs";
    private static final String LAST_PROMPTED_MONTH_KEY = "lastPromptedMonth";

    private static final int TAB_INDEX_LAST_MONTH = 0;
    private static final int TAB_INDEX_THIS_MONTH = 1;
    private static final int TAB_INDEX_NEXT_MONTH = 2;

    private Handler timeoutHandler = new Handler(Looper.getMainLooper());
    private Runnable timeoutRunnable;

    private FirebaseFirestore db;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance(); // Khởi tạo biến db
        currentUser = auth.getCurrentUser();
        if (currentUser == null) { navigateToLogin(); return; }
        initializeMonthsList();
        // Đăng ký BroadcastReceiver an toàn hơn trong onCreate hoặc onViewCreated
        if (getContext() != null) {
            LocalBroadcastManager.getInstance(requireContext()).registerReceiver(balanceUpdateReceiver, new IntentFilter(BALANCE_UPDATED));
        }
        loadInitialBudget(); // Tải ngân sách ban đầu
    }

    // ... (navigateToLogin, initializeMonthsList, onCreateView, onViewCreated giữ nguyên) ...
    private void navigateToLogin() {
        if (getActivity() == null || !isAdded()) return; // Thêm kiểm tra isAdded
        Intent loginIntent = new Intent(getActivity(), LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        getActivity().finish();
    }

    private void initializeMonthsList() {
        months.clear();
        Calendar calendar = Calendar.getInstance();
        // Tạo tháng hiện tại trước
        months.add(calendar.getTime()); // This Month (index 1 sau khi thêm tháng trước/sau)
        // Tạo tháng trước
        Calendar lastMonthCal = (Calendar) calendar.clone();
        lastMonthCal.add(Calendar.MONTH, -1);
        months.add(0, lastMonthCal.getTime()); // Add at index 0 for Last Month
        // Tạo tháng sau
        Calendar nextMonthCal = (Calendar) calendar.clone();
        nextMonthCal.add(Calendar.MONTH, 1);
        months.add(nextMonthCal.getTime()); // Add at the end for Next Month

        Log.d(TAG, "Initialized Months List: " + months.size() + " months.");
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Lấy lại currentUser phòng trường hợp bị null trong onCreate
        currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            Log.w(TAG, "User became null in onViewCreated.");
            navigateToLogin(); // Chuyển về login nếu user null
            return;
        }
        setupRecyclerView();
        setupTabLayout();
        // Chọn tab "Tháng này" làm mặc định một cách an toàn
        if (binding.tabLayout.getTabCount() > TAB_INDEX_THIS_MONTH) {
            TabLayout.Tab thisMonthTab = binding.tabLayout.getTabAt(TAB_INDEX_THIS_MONTH);
            if (thisMonthTab != null) {
                thisMonthTab.select();
                // Gọi loadDataForTab lần đầu ở đây sau khi tab được chọn
                loadDataForTab(TAB_INDEX_THIS_MONTH);
            } else {
                Log.e(TAG, "This month tab is null!");
            }
        } else {
            Log.e(TAG, "TabLayout does not have enough tabs!");
        }

    }


    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume called");
        // Tải lại ngân sách khi quay lại màn hình
        loadInitialBudget();
        checkAndPromptForNewMonthBudget();

        // Luôn tải lại dữ liệu cho tab hiện tại khi quay lại màn hình
        // để đảm bảo hiển thị dữ liệu mới nhất sau khi xóa hoặc sửa
        if (binding != null && binding.tabLayout.getSelectedTabPosition() >= 0) {
            int currentTab = binding.tabLayout.getSelectedTabPosition();
            Log.d(TAG, "onResume: Reloading data for current tab: " + currentTab);
            
            // Reset để buộc tải lại dữ liệu
            removeTransactionListener();
            removeTimeoutHandler();
            currentMonthKey = null;
            
            loadDataForTab(currentTab);
        }
    }

    // ... (checkAndPromptForNewMonthBudget, showBudgetUpdateDialog, saveLastPromptedMonth giữ nguyên) ...
    private void checkAndPromptForNewMonthBudget() {
        if (!isAdded() || getContext() == null) return;
        SharedPreferences prefs = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String lastPromptedMonth = prefs.getString(LAST_PROMPTED_MONTH_KEY, "");
        String currentMonthYear = firestoreDateFormat.format(new Date());
        Log.d(TAG, "Checking for new month budget prompt. Current: " + currentMonthYear + ", Last Prompted: " + lastPromptedMonth);
        if (!TextUtils.isEmpty(lastPromptedMonth) && !currentMonthYear.equals(lastPromptedMonth)) {
            Log.d(TAG, "New month detected. Showing budget update prompt.");
            showBudgetUpdateDialog(currentMonthYear);
        } else if (TextUtils.isEmpty(lastPromptedMonth)) {
            Log.d(TAG, "First time check. Saving current month without prompt.");
            saveLastPromptedMonth(currentMonthYear);
        } else {
            Log.d(TAG, "Not a new month. Skipping budget prompt.");
        }
    }
    private void showBudgetUpdateDialog(String currentMonthYear) {
        if (!isAdded() || getContext() == null) return; // Thêm kiểm tra
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.new_month_budget_title)
                .setMessage(R.string.ask_update_budget)
                .setPositiveButton(R.string.yes, (dialog, which) -> {
                    Log.d(TAG, "User chose YES to update budget.");
                    Intent intent = new Intent(getActivity(), EditProfileActivity.class);
                    // Không cần gửi isNewUser vì EditProfileActivity nên tự lấy user hiện tại
                    // intent.putExtra("isNewUser", false);
                    startActivityForResult(intent, REQUEST_EDIT_PROFILE_FOR_BUDGET);
                    saveLastPromptedMonth(currentMonthYear);
                })
                .setNegativeButton(R.string.no, (dialog, which) -> {
                    Log.d(TAG, "User chose NO to update budget.");
                    saveLastPromptedMonth(currentMonthYear);
                })
                .setCancelable(false)
                .show();
    }
    private void saveLastPromptedMonth(String monthYear) {
        if (!isAdded() || getContext() == null) return;
        SharedPreferences prefs = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(LAST_PROMPTED_MONTH_KEY, monthYear);
        editor.apply();
        Log.d(TAG, "Saved last prompted month: " + monthYear);
    }

    // ... (setupRecyclerView giữ nguyên) ...
    private void setupRecyclerView() {
        adapter = new SpendingAdapter(); // Khởi tạo adapter mới
        // Kiểm tra null binding trước khi truy cập
        if (binding != null) {
            binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
            binding.recyclerView.setAdapter(adapter);
            adapter.setOnSpendingClickListener(typeSpending -> {
                if (typeSpending == null || typeSpending.getSpendings() == null || typeSpending.getSpendings().isEmpty()) { return; }
                List<Spending> spendingsOfType = typeSpending.getSpendings();
                ArrayList<String> spendingIds = new ArrayList<>();
                for (Spending spending : spendingsOfType) { if (spending != null && spending.getId() != null && !spending.getId().isEmpty()) { spendingIds.add(spending.getId()); } }
                if (spendingIds.isEmpty()) {
                    if (getContext() != null) Toast.makeText(getContext(), R.string.error_no_valid_spendings_in_group, Toast.LENGTH_SHORT).show();
                    return;
                }
                // Thêm kiểm tra context trước khi tạo Intent
                if (getContext() != null) {
                    Intent intent = ViewListSpendingActivity.createIntent(requireContext(), spendingIds, typeSpending.getType(), typeSpending.getTypeName());
                    // Nên dùng startActivity thay vì startActivityForResult nếu không cần nhận kết quả
                    startActivity(intent);
                }
            });
        } else {
            Log.e(TAG, "Binding is null in setupRecyclerView!");
        }
    }

    // ... (setupTabLayout giữ nguyên) ...
    private void setupTabLayout() {
        if (binding == null) return; // Kiểm tra null
        binding.tabLayout.removeAllTabs();
        // Đảm bảo months đã được khởi tạo
        if (months == null || months.isEmpty()) {
            initializeMonthsList(); // Khởi tạo lại nếu rỗng
        }
        // Tạo tiêu đề tab động dựa trên danh sách months
        SimpleDateFormat tabFormat = new SimpleDateFormat("MM/yyyy", Locale.getDefault());
        String[] tabTitles = new String[months.size()];
        Calendar currentCal = Calendar.getInstance();
        int currentMonthIndex = currentCal.get(Calendar.MONTH);
        int currentYear = currentCal.get(Calendar.YEAR);

        for (int i = 0; i < months.size(); i++) {
            Calendar tabCal = Calendar.getInstance();
            tabCal.setTime(months.get(i));
            int tabMonthIndex = tabCal.get(Calendar.MONTH);
            int tabYear = tabCal.get(Calendar.YEAR);

            if (i == TAB_INDEX_THIS_MONTH && tabMonthIndex == currentMonthIndex && tabYear == currentYear) {
                tabTitles[i] = getString(R.string.this_month);
            } else if (i == TAB_INDEX_LAST_MONTH) {
                tabTitles[i] = getString(R.string.last_month); // Hoặc format tháng trước
                // tabTitles[i] = tabFormat.format(months.get(i));
            } else if (i == TAB_INDEX_NEXT_MONTH) {
                tabTitles[i] = getString(R.string.next_month); // Hoặc format tháng sau
                // tabTitles[i] = tabFormat.format(months.get(i));
            } else {
                // Xử lý các trường hợp khác nếu có nhiều tab hơn
                tabTitles[i] = tabFormat.format(months.get(i));
            }
        }

        // String[] tabTitles = { getString(R.string.last_month), getString(R.string.this_month), getString(R.string.next_month) }; // Cách cũ
        for (String title : tabTitles) {
            TabLayout.Tab tab = binding.tabLayout.newTab();
            tab.setText(title);
            binding.tabLayout.addTab(tab);
        }
        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override public void onTabSelected(TabLayout.Tab tab) { Log.d(TAG, "Tab selected: " + tab.getPosition()); loadDataForTab(tab.getPosition()); }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) { Log.d(TAG, "Tab reselected: " + tab.getPosition()); /* Có thể không cần load lại ở đây */ }
        });
    }


    // ... (loadDataForTab, updateInitialBalanceDisplay, loadBudgetForMonth, loadCurrentMonthBudgetFromUserDoc, saveBudgetForCurrentMonth giữ nguyên) ...
    private void loadDataForTab(int selectedPosition) {
        currentUser = auth.getCurrentUser(); // Lấy lại user mới nhất
        if (currentUser == null || !isAdded() || months == null || months.isEmpty() || selectedPosition < 0 || selectedPosition >= months.size() || binding == null) {
            Log.w(TAG, "loadDataForTab preconditions not met.");
            showLoading(false);
            if (adapter != null) adapter.setItems(new ArrayList<>());
            updateSummaryDisplay(0.0, 0.0); // Reset summary
            showNoData();
            return;
        }

        Date selectedDate = months.get(selectedPosition);
        String selectedMonthKey = firestoreDateFormat.format(selectedDate);

        // Luôn xóa listener cũ và tạo listener mới để đảm bảo dữ liệu mới nhất
        removeTransactionListener();
        removeTimeoutHandler();
        currentMonthKey = selectedMonthKey;
        Log.d(TAG, "Loading data for tab: " + selectedPosition + ", monthKey: " + currentMonthKey);
        showLoading(true);
        updateSpendingListTitle(selectedPosition);

        // Tải ngân sách cho tháng này
        loadBudgetForMonth(selectedMonthKey, selectedPosition);

        // Xóa dữ liệu cũ và hiển thị trạng thái loading/no data
        currentDisplayedSpendings.clear();
        if (adapter != null) adapter.setItems(new ArrayList<>());
        showNoData();

        // Thêm timeout handler để tránh treo UI nếu Firestore không phản hồi
        startTimeoutHandler(selectedMonthKey);

        // Đơn giản hóa truy vấn để tránh lỗi index
        Query query = FirebaseFirestore.getInstance().collection("spending")
                .whereEqualTo("userId", currentUser.getUid())
                .whereEqualTo("monthKey", selectedMonthKey);

        // Đăng ký listener mới
        transactionListener = query.addSnapshotListener((querySnapshot, error) -> {
            // Xử lý kết quả query
            if (!isAdded() || binding == null || !selectedMonthKey.equals(currentMonthKey)) {
                Log.w(TAG, "Listener callback ignored: state changed");
                return;
            }
            removeTimeoutHandler();

            if (error != null) {
                Log.e(TAG, "Firestore listener error for " + selectedMonthKey, error);
                
                // Xử lý lỗi index
                if (error instanceof FirebaseFirestoreException && 
                    ((FirebaseFirestoreException) error).getCode() == FirebaseFirestoreException.Code.FAILED_PRECONDITION &&
                    error.getMessage() != null && error.getMessage().contains("index")) {
                    
                    Log.e(TAG, "Firestore index error. Please create the required index using the link in the error message.", error);
                    
                    // Hiển thị thông báo lỗi index
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            showLoading(false);
                            showError(getString(R.string.error_firestore_index_required));
                            showNoData();
                            binding.textNoData.setText(R.string.error_firestore_index_required_short);
                        });
                    }
                } else {
                    // Xử lý các lỗi khác
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            showLoading(false);
                            showError(getString(R.string.error_loading_data) + ": " + error.getMessage());
                            showNoData();
                        });
                    }
                }
                return;
            }

            final List<Spending> newList = new ArrayList<>();
            final double[] totalExpenses = {0.0};
            if (querySnapshot != null) {
                for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                    try {
                        Spending spending = doc.toObject(Spending.class);
                        // Lọc isDeleted ở client side thay vì trong query
                        if (spending != null && !spending.isDeleted()) {
                            newList.add(spending);
                            totalExpenses[0] += Math.abs(spending.getMoney());
                        } else if (spending != null && spending.isDeleted()) {
                            Log.w(TAG, "Skipping deleted spending: " + doc.getId());
                        }
                    } catch (Exception e) { 
                        Log.e(TAG, "Error parsing doc " + doc.getId(), e); 
                    }
                }
            }

            // Cập nhật UI với dữ liệu mới
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (!isAdded() || binding == null || !selectedMonthKey.equals(currentMonthKey)) {
                        Log.w(TAG, "UI update ignored: state changed");
                        return;
                    }
                    
                    showLoading(false);
                    
                    currentDisplayedSpendings.clear();
                    currentDisplayedSpendings.addAll(newList);

                    if (newList.isEmpty()) {
                        if (adapter != null) adapter.setItems(new ArrayList<>());
                        showNoData();
                    } else {
                        if (adapter != null) adapter.setItems(newList);
                        hideNoData();
                    }

                    // Cập nhật summary
                    double budgetToUseForSummary = 0.0;
                    if (binding.tabLayout.getSelectedTabPosition() == TAB_INDEX_THIS_MONTH) {
                        budgetToUseForSummary = initialBudgetBalance;
                    }
                    updateSummaryDisplay(budgetToUseForSummary, totalExpenses[0]);
                });
            }
        });
    }
    private void updateInitialBalanceDisplay(double balance) {
        if (binding != null && binding.summarySpendingView != null && isAdded()) { // Thêm isAdded()
            binding.summarySpendingView.textFirstBalanceAmount.setText(numberFormat.format(balance));
            Log.d(TAG, "Updated Initial Balance Display: " + balance);
        }
    }
    private void loadBudgetForMonth(String monthKey, int tabPosition) {
        currentUser = auth.getCurrentUser(); // Lấy lại user
        if (currentUser == null || !isAdded()) return;

        double defaultValue = (tabPosition == TAB_INDEX_THIS_MONTH) ? initialBudgetBalance : 0.0;
        updateInitialBalanceDisplay(defaultValue); // Hiển thị giá trị mặc định/cũ trước
        updateSummaryBasedOnCurrentList(defaultValue); // Tính lại summary với giá trị mặc định/cũ

        Log.d(TAG, "Loading budget for month: " + monthKey);
        FirebaseFirestore.getInstance().collection("budgets").document(currentUser.getUid()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!isAdded() || binding == null) return; // Kiểm tra lại state
                    double budgetForMonth = 0.0;
                    boolean budgetLoaded = false; // Cờ để biết đã load được budget chưa

                    if (documentSnapshot.exists()) {
                        Double monthBudget = documentSnapshot.getDouble(monthKey);
                        if (monthBudget != null) {
                            budgetForMonth = monthBudget;
                            budgetLoaded = true;
                            Log.d(TAG, "Budget for month " + monthKey + " loaded from budgets: " + budgetForMonth);
                        }
                    }

                    // Nếu không load được từ budgets VÀ đang ở tab tháng này -> thử load từ users
                    if (!budgetLoaded && tabPosition == TAB_INDEX_THIS_MONTH) {
                        Log.d(TAG, "No budget in 'budgets' for current month " + monthKey + ". Trying 'users' doc.");
                        loadCurrentMonthBudgetFromUserDoc(); // Hàm này sẽ tự cập nhật UI và summary
                    } else {
                        // Nếu load được từ budgets hoặc không phải tháng hiện tại
                        Log.d(TAG, "Using budget value: " + budgetForMonth + " for month " + monthKey);
                        updateInitialBalanceDisplay(budgetForMonth);
                        updateSummaryBasedOnCurrentList(budgetForMonth); // Tính lại summary với budget mới
                    }
                })
                .addOnFailureListener(e -> {
                    if (!isAdded()) return;
                    Log.e(TAG, "Error loading budget for month " + monthKey, e);
                    // Nếu lỗi và là tháng hiện tại, thử tải từ user document
                    if (tabPosition == TAB_INDEX_THIS_MONTH) {
                        loadCurrentMonthBudgetFromUserDoc();
                    } else {
                        updateInitialBalanceDisplay(0.0);
                        updateSummaryBasedOnCurrentList(0.0);
                    }
                });
    }
    private void loadCurrentMonthBudgetFromUserDoc() {
        currentUser = auth.getCurrentUser(); // Lấy lại user
        if (currentUser == null || !isAdded()) return;
        FirebaseFirestore.getInstance().collection("users").document(currentUser.getUid()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!isAdded() || binding == null) return;
                    double newBudget = 0.0;
                    if (documentSnapshot.exists()) {
                        Double money = documentSnapshot.getDouble("money");
                        newBudget = (money != null) ? money : 0.0;
                        Log.d(TAG, "Current month budget loaded from user document: " + newBudget);
                        saveBudgetForCurrentMonth(newBudget); // Lưu vào budgets collection
                    } else {
                        Log.w(TAG, "User document doesn't exist for current budget");
                    }
                    initialBudgetBalance = newBudget; // Cập nhật biến instance
                    updateInitialBalanceDisplay(initialBudgetBalance);
                    updateSummaryBasedOnCurrentList(initialBudgetBalance);
                })
                .addOnFailureListener(e -> {
                    if (!isAdded()) return;
                    Log.e(TAG, "Error loading current month budget from user document", e);
                    showError(getString(R.string.error_loading_current_balance));
                    initialBudgetBalance = 0.0;
                    updateInitialBalanceDisplay(0.0);
                    updateSummaryBasedOnCurrentList(0.0);
                });
    }
    private void saveBudgetForCurrentMonth(double budget) {
        currentUser = auth.getCurrentUser(); // Lấy lại user
        if (currentUser == null || !isAdded()) return; // Thêm isAdded()
        String currentMonthKey = firestoreDateFormat.format(new Date());
        Map<String, Object> updates = new HashMap<>();
        updates.put(currentMonthKey, budget);
        FirebaseFirestore.getInstance().collection("budgets").document(currentUser.getUid())
                .set(updates, SetOptions.merge())
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Budget for current month saved: " + budget))
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error saving budget for current month", e);
                    if (e instanceof FirebaseFirestoreException && ((FirebaseFirestoreException) e).getCode() == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                        Log.w(TAG, "Permission denied when saving to budgets collection. Check Firestore rules.");
                    }
                    // Không cần báo lỗi cho người dùng ở đây
                });
    }


    // ... (setupTransactionListener đã cập nhật ở lần trước - giữ nguyên) ...
    private void setupTransactionListener(String monthKey) {
        FirebaseUser liveCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (liveCurrentUser == null || !isAdded() || binding == null) { // Thêm kiểm tra binding
            Log.w(TAG, "setupTransactionListener preconditions not met.");
            showLoading(false);
            if (adapter != null) adapter.setItems(new ArrayList<>());
            updateSummaryDisplay(0, 0);
            showNoData();
            return;
        }
        String liveUserId = liveCurrentUser.getUid();
        Log.d(TAG, "Attaching listener for monthKey: " + monthKey + " with User ID: " + liveUserId);

        Query query = FirebaseFirestore.getInstance().collection("spending")
                .whereEqualTo("userId", liveUserId)
                .whereEqualTo("monthKey", monthKey)
                .whereEqualTo("isDeleted", false);

        transactionListener = query.addSnapshotListener((value, error) -> {
            if (!isAdded() || binding == null || !monthKey.equals(currentMonthKey)) {
                Log.w(TAG, "Listener callback ignored: state changed");
                return;
            }
            removeTimeoutHandler();

            if (error != null) {
                Log.e(TAG, "Firestore listener error for " + monthKey, error);
                if (error.getCode() == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                    Log.w(TAG, "Permission denied during listen. Suppressing UI error.");
                    if (getActivity() != null) getActivity().runOnUiThread(() -> {
                        if (binding == null || !monthKey.equals(currentMonthKey)) return;
                        currentDisplayedSpendings.clear();
                        if (adapter != null) adapter.setItems(new ArrayList<>());
                        showNoData();
                        binding.textNoData.setText(R.string.error_permission_denied);
                        showLoading(false);
                        updateSummaryDisplay(0, 0);
                    });
                } else if (error.getCode() == FirebaseFirestoreException.Code.FAILED_PRECONDITION && error.getMessage() != null && error.getMessage().contains("index")) {
                    Log.e(TAG, " Firestore query requires an index.", error);
                    showError(getString(R.string.error_firestore_index_required));
                    if (getActivity() != null) getActivity().runOnUiThread(() -> {
                        if (binding == null || !monthKey.equals(currentMonthKey)) return;
                        currentDisplayedSpendings.clear();
                        if (adapter != null) adapter.setItems(new ArrayList<>());
                        showNoData();
                        binding.textNoData.setText(R.string.error_firestore_index_required_short);
                        showLoading(false);
                        updateSummaryDisplay(0, 0);
                    });
                } else {
                    showError(getString(R.string.error_loading_transactions) + ": " + error.getMessage());
                    if (getActivity() != null) getActivity().runOnUiThread(() -> {
                        if (binding == null || !monthKey.equals(currentMonthKey)) return;
                        currentDisplayedSpendings.clear();
                        if (adapter != null) adapter.setItems(new ArrayList<>());
                        showNoData();
                        binding.textNoData.setText(R.string.error_loading_data);
                        showLoading(false);
                        updateSummaryDisplay(0, 0);
                    });
                }
                return;
            }

            final List<Spending> newList = new ArrayList<>();
            final double[] totalExpenses = {0.0};
            if (value != null) {
                for (DocumentSnapshot doc : value.getDocuments()) {
                    try {
                        Spending spending = doc.toObject(Spending.class);
                        if (spending != null && !spending.isDeleted()) {
                            newList.add(spending);
                            totalExpenses[0] += Math.abs(spending.getMoney());
                        } else if (spending != null) {
                            Log.w(TAG, "Skipping deleted spending: " + doc.getId());
                        }
                    } catch (Exception e) { Log.e(TAG, "Error parsing doc " + doc.getId(), e); }
                }
            }

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (binding == null || !monthKey.equals(currentMonthKey)) return;
                    currentDisplayedSpendings.clear();
                    currentDisplayedSpendings.addAll(newList);
                    if (currentDisplayedSpendings.isEmpty()) {
                        if (adapter != null) adapter.setItems(new ArrayList<>());
                        showNoData();
                    } else {
                        hideNoData();
                        if (adapter != null) adapter.setItems(currentDisplayedSpendings);
                    }
                    // Lấy lại budget cho tháng hiện tại để tính summary
                    // Vì loadBudgetForMonth chạy bất đồng bộ, giá trị budget có thể chưa đúng ngay
                    // Cách tốt hơn là truyền budget vào đây hoặc lấy lại từ biến instance nếu chắc chắn nó đúng
                    double budgetToUseForSummary = 0.0;
                    if (binding.tabLayout.getSelectedTabPosition() == TAB_INDEX_THIS_MONTH) {
                        budgetToUseForSummary = initialBudgetBalance; // Dùng budget tháng này
                    } else {
                        // Cần cơ chế lấy budget của tháng khác nếu cần hiển thị summary đúng
                        // Tạm thời dùng 0 cho các tháng khác
                    }
                    updateSummaryDisplay(budgetToUseForSummary, totalExpenses[0]);
                    showLoading(false);
                });
            }
        });
    }


    // ... (updateSummaryDisplay, showNoData, hideNoData, showLoading, showError giữ nguyên) ...
    private void updateSummaryDisplay(double initialBalanceToUse, double totalExpenses) {
        if (binding != null && binding.summarySpendingView != null && isAdded()) { // Thêm isAdded()
            Log.d(TAG, String.format("Updating Summary Display - Initial Used: %.2f, Total Expenses: %.2f", initialBalanceToUse, totalExpenses));
            binding.summarySpendingView.textTotalAmount.setText(numberFormat.format(totalExpenses));
            binding.summarySpendingView.textTotalAmount.setTextColor(ContextCompat.getColor(requireContext(), R.color.expense_red));
            double finalBalance = initialBalanceToUse - totalExpenses;
            binding.summarySpendingView.textFinalBalanceAmount.setText(numberFormat.format(finalBalance));
            binding.summarySpendingView.textFinalBalanceAmount.setTextColor(ContextCompat.getColor(requireContext(), finalBalance < 0 ? R.color.expense_red : R.color.text_primary));
            Log.d(TAG, String.format("Updated Final Balance Display: %.2f", finalBalance));
        }
    }
    private void showNoData() {
        if (binding != null && isAdded()) { // Thêm isAdded()
            binding.recyclerView.setVisibility(View.GONE);
            binding.textNoData.setVisibility(View.VISIBLE);
            binding.textNoData.setText(R.string.no_data_available);
            Log.d(TAG, "Showing no data view");
        }
    }
    private void hideNoData() {
        if (binding != null && isAdded()) { // Thêm isAdded()
            binding.recyclerView.setVisibility(View.VISIBLE);
            binding.textNoData.setVisibility(View.GONE);
            Log.d(TAG, "Showing RecyclerView with data");
        }
    }
    private void showLoading(boolean show) {
        if (!isAdded() || binding == null) return;
        binding.progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        if (show) {
            binding.recyclerView.setVisibility(View.GONE);
            binding.textNoData.setVisibility(View.GONE);
        }
        // Không ẩn RecyclerView khi loading xong ở đây, để hideNoData/showNoData quyết định
        Log.d(TAG, "showLoading: " + show);
    }
    private void showError(String message) {
        if (getContext() != null && isAdded()) { // Thêm isAdded()
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
        }
        Log.e(TAG, "Error displayed: " + message);
    }

    // ... (updateSpendingListTitle, timeout handlers, removeTransactionListener, lifecycle methods giữ nguyên) ...
    private void updateSpendingListTitle(int selectedPosition) {
        if (binding == null || !isAdded() || months.isEmpty() || selectedPosition < 0 || selectedPosition >= months.size()) return; // Thêm kiểm tra
        // Lấy tiêu đề từ tab thay vì tính lại
        TabLayout.Tab selectedTab = binding.tabLayout.getTabAt(selectedPosition);
        if (selectedTab != null && selectedTab.getText() != null) {
            String monthText = selectedTab.getText().toString();
            binding.textSpendingList.setText(getString(R.string.spending_list, monthText.toLowerCase()));
            if (binding.summarySpendingView != null) { // Kiểm tra null
                binding.summarySpendingView.dateText.setText(getString(R.string.spending_report_for, monthText.toLowerCase())); // Thêm string resource mới
            }
            Log.d(TAG, "Updated titles for tab " + selectedPosition + " (" + monthText + ")");
        } else {
            Log.w(TAG, "Selected tab or its text is null at position: " + selectedPosition);
        }
    }
    private void startTimeoutHandler(String monthKeyForTimeout) {
        removeTimeoutHandler();
        timeoutRunnable = () -> {
            if (!isAdded() || binding == null || !monthKeyForTimeout.equals(currentMonthKey)) {
                Log.w(TAG, "Timeout occurred but state changed, ignoring timeout for " + monthKeyForTimeout);
                return;
            }
            Log.w(TAG, "Firestore listener timed out for monthKey: " + monthKeyForTimeout);
            showLoading(false);
            // Chỉ hiển thị lỗi timeout nếu adapter thực sự rỗng
            if (adapter == null || adapter.getItemCount() == 0) {
                showError(getString(R.string.error_loading_timeout));
                showNoData(); // Hiển thị trạng thái rỗng
                binding.textNoData.setText(R.string.error_loading_timeout);
            }
        };
        timeoutHandler.postDelayed(timeoutRunnable, 10000); // 10 giây timeout
        Log.d(TAG, "Timeout handler started for " + monthKeyForTimeout);
    }
    private void removeTimeoutHandler() { if (timeoutRunnable != null) { timeoutHandler.removeCallbacks(timeoutRunnable); timeoutRunnable = null; Log.d(TAG, "Timeout handler removed."); } }
    private void removeTransactionListener() { if (transactionListener != null) { transactionListener.remove(); transactionListener = null; Log.d(TAG, "Transaction listener removed."); } }
    @Override public void onPause() { super.onPause(); removeTransactionListener(); removeTimeoutHandler(); Log.d(TAG, "onPause: Listener and timeout removed."); }
    @Override public void onDestroyView() { super.onDestroyView(); Log.d(TAG, "onDestroyView called"); removeTransactionListener(); removeTimeoutHandler(); binding = null; } // Đặt binding về null
    private void openAddSpending() {
        if (getActivity() == null || !isAdded()) return; // Thêm kiểm tra
        Log.d(TAG, "Opening AddSpendingActivity...");
        Intent intent = new Intent(getActivity(), AddSpendingActivity.class);
        startActivityForResult(intent, REQUEST_ADD_SPENDING); // Dùng đúng request code
    }

    // *** SỬA LẠI onActivityResult ***
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: requestCode=" + requestCode + ", resultCode=" + resultCode);
        
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_ADD_SPENDING || 
                requestCode == REQUEST_EDIT_SPENDING || 
                requestCode == REQUEST_VIEW_SPENDING) {
                
                Log.d(TAG, "Received RESULT_OK from Spending activity. Forcing complete reload.");
                
                // Xóa tất cả dữ liệu hiện tại và buộc tải lại tất cả các tab
                removeTransactionListener();
                removeTimeoutHandler();
                currentMonthKey = null;
                
                // Xóa dữ liệu hiện tại
                currentDisplayedSpendings.clear();
                if (adapter != null) {
                    adapter.setItems(new ArrayList<>());
                }
                
                // Tải lại dữ liệu cho tất cả các tab
                for (int i = 0; i < months.size(); i++) {
                    String monthKey = firestoreDateFormat.format(months.get(i));
                    Log.d(TAG, "Invalidating cache for month: " + monthKey);
                    // Nếu có cache manager, hãy xóa cache cho tháng này
                    // cacheManager.invalidate("spending_" + monthKey);
                }
                
                // Tải lại dữ liệu cho tab hiện tại
                if (binding != null) {
                    int currentTabPosition = binding.tabLayout.getSelectedTabPosition();
                    if (currentTabPosition >= 0) {
                        Log.d(TAG, "Reloading data for current tab position: " + currentTabPosition);
                        loadDataForTab(currentTabPosition);
                    }
                }
                
            } else if (requestCode == REQUEST_EDIT_PROFILE_FOR_BUDGET) {
                Log.d(TAG, "Received RESULT_OK from EditProfileActivity (budget update).");
                loadInitialBudget();
                if (binding != null && binding.tabLayout.getSelectedTabPosition() == TAB_INDEX_THIS_MONTH) {
                    loadDataForTab(TAB_INDEX_THIS_MONTH);
                }
            }
        }
    }

    // ... (BroadcastReceiver, loadInitialBudget, updateSummaryBasedOnCurrentList giữ nguyên) ...
    private final BroadcastReceiver balanceUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (BALANCE_UPDATED.equals(intent.getAction())) {
                Log.d(TAG, "Received balance update broadcast");
                loadInitialBudget();
                // Cập nhật lại summary cho tab hiện tại sau khi budget thay đổi
                if (binding != null && binding.tabLayout.getSelectedTabPosition() >= 0) {
                    updateSummaryBasedOnCurrentList();
                }
            }
        }
    };
    private void loadInitialBudget() {
        currentUser = auth.getCurrentUser(); // Lấy lại user
        if (currentUser == null || !isAdded()) return;
        Log.d(TAG, "Loading initial budget balance...");
        FirebaseFirestore.getInstance().collection("users").document(currentUser.getUid()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!isAdded() || binding == null) return;
                    double newBudget = 0.0;
                    if (documentSnapshot.exists()) {
                        Double money = documentSnapshot.getDouble("money");
                        newBudget = (money != null) ? money : 0.0;
                        Log.d(TAG, "Initial budget balance loaded: " + newBudget);
                    } else {
                        Log.w(TAG, "User document doesn't exist for balance loading");
                    }
                    // Chỉ cập nhật nếu giá trị thay đổi đáng kể
                    if (Math.abs(initialBudgetBalance - newBudget) > 0.001) {
                        initialBudgetBalance = newBudget;
                        saveBudgetForCurrentMonth(initialBudgetBalance); // Lưu vào budgets
                        // Cập nhật UI nếu đang ở tab tháng này
                        if (binding.tabLayout.getSelectedTabPosition() == TAB_INDEX_THIS_MONTH) {
                            updateInitialBalanceDisplay(initialBudgetBalance);
                        }
                        // Luôn tính lại summary với budget mới nhất
                        updateSummaryBasedOnCurrentList();
                    }
                })
                .addOnFailureListener(e -> {
                    if (!isAdded()) return;
                    Log.e(TAG, "Error loading initial budget balance", e);
                    showError(getString(R.string.error_loading_current_balance));
                    initialBudgetBalance = 0.0; // Reset về 0 nếu lỗi
                    if (binding.tabLayout.getSelectedTabPosition() == TAB_INDEX_THIS_MONTH) {
                        updateInitialBalanceDisplay(0.0);
                    }
                    updateSummaryBasedOnCurrentList();
                });
    }
    private void updateSummaryBasedOnCurrentList(double budgetToUse) {
        if (binding == null || !isAdded()) return; // Thêm isAdded()
        double currentTotalExpenses = 0;
        // Tính tổng từ currentDisplayedSpendings để đảm bảo đồng bộ với adapter
        for (Spending s : currentDisplayedSpendings) {
            if (s != null && s.getMoney() < 0) {
                currentTotalExpenses += Math.abs(s.getMoney());
            }
        }
        Log.d(TAG, "Recalculating summary. Budget: " + budgetToUse + ", Total Expenses: " + currentTotalExpenses);
        updateSummaryDisplay(budgetToUse, currentTotalExpenses);
    }
    private void updateSummaryBasedOnCurrentList() {
        if (binding == null || !isAdded()) return; // Thêm isAdded()
        int selectedTabPos = binding.tabLayout.getSelectedTabPosition();
        // Lấy budget phù hợp cho tab hiện tại
        double budgetForCurrentTab = 0.0;
        if (selectedTabPos == TAB_INDEX_THIS_MONTH) {
            budgetForCurrentTab = initialBudgetBalance; // Dùng budget chung cho tháng này
        } else {
            // TODO: Cần lấy budget của tháng trước/sau từ collection "budgets" nếu muốn summary chính xác
            // Tạm thời vẫn dùng 0 cho các tháng khác
        }
        updateSummaryBasedOnCurrentList(budgetForCurrentTab);
    }


    // ... (onDestroy giữ nguyên) ...
    @Override
    public void onDestroy() {
        super.onDestroy();
        // Hủy đăng ký broadcast receiver an toàn
        if (getContext() != null) {
            try {
                LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(balanceUpdateReceiver);
            } catch (Exception e) {
                Log.e(TAG, "Error unregistering receiver", e);
            }
        }
        Log.d(TAG, "onDestroy called");
    }

}
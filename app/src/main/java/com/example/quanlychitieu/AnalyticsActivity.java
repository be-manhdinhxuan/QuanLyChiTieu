package com.example.quanlychitieu;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.example.quanlychitieu.views.PieChartView;
import com.example.quanlychitieu.views.ColumnChartView;
import com.example.quanlychitieu.models.Spending;
import com.example.quanlychitieu.adapters.ChartPagerAdapter;
import com.example.quanlychitieu.utils.DateUtils;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class AnalyticsActivity extends AppCompatActivity {
    private TabLayout periodTabLayout;
    private TabLayout chartTypeTabLayout;
    private TabLayout spendingTypeTabLayout;
    private ViewPager2 chartViewPager;
    private TextView dateText;
    private View searchButton;
    private PieChartView pieChartView;
    private ColumnChartView columnChartView;
    private TextView noDataText;
    private View loadingIndicator;

    private Calendar currentDate;
    private int currentPeriodTab = 0;
    private boolean isPieChartView = false;
    private int spendingTypeTab = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analytics);

        initializeViews();
        setupTabLayouts();
        setupListeners();
        loadInitialData();
    }

    private void initializeViews() {
        periodTabLayout = findViewById(R.id.periodTabLayout);
        chartTypeTabLayout = findViewById(R.id.chartTypeTabLayout);
        spendingTypeTabLayout = findViewById(R.id.spendingTypeTabLayout);
        dateText = findViewById(R.id.dateText);
        searchButton = findViewById(R.id.searchButton);
        pieChartView = findViewById(R.id.pieChartView);
        columnChartView = findViewById(R.id.columnChartView);
        noDataText = findViewById(R.id.noDataText);
        loadingIndicator = findViewById(R.id.loadingIndicator);
        
        currentDate = Calendar.getInstance();
    }

    private void setupTabLayouts() {
        // Period tabs (Week, Month, Year)
        String[] periodTabs = new String[]{"Week", "Month", "Year"};
        for (String tab : periodTabs) {
            periodTabLayout.addTab(periodTabLayout.newTab().setText(tab));
        }

        // Chart type tabs (Column, Pie)
        String[] chartTabs = new String[]{"Column", "Pie"};
        for (String tab : chartTabs) {
            chartTypeTabLayout.addTab(chartTypeTabLayout.newTab().setText(tab));
        }

        // Spending type tabs (Income, Expense)
        String[] spendingTabs = new String[]{"Income", "Expense"};
        for (String tab : spendingTabs) {
            spendingTypeTabLayout.addTab(spendingTypeTabLayout.newTab().setText(tab));
        }
    }

    private void setupListeners() {
        periodTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                currentPeriodTab = tab.getPosition();
                updateDateDisplay();
                loadData();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        chartTypeTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                isPieChartView = tab.getPosition() == 1;
                updateChartVisibility();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        searchButton.setOnClickListener(v -> {
            // Launch search activity
            startActivity(new Intent(this, SearchActivity.class));
        });
    }

    private void updateDateDisplay() {
        String dateStr = "";
        switch (currentPeriodTab) {
            case 0: // Week
                dateStr = DateUtils.getWeekDisplay(currentDate);
                break;
            case 1: // Month
                dateStr = DateUtils.getMonthDisplay(currentDate);
                break;
            case 2: // Year
                dateStr = DateUtils.getYearDisplay(currentDate);
                break;
        }
        dateText.setText(dateStr);
    }

    private void loadData() {
        showLoading(true);
        
        FirebaseFirestore.getInstance()
            .collection("data")
            .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    List<String> spendingIds = GetDataSpending.getDataSpending(
                        documentSnapshot.getData(),
                        currentPeriodTab,
                        currentDate.getTime()
                    );
                    
                    loadSpendingData(spendingIds);
                } else {
                    showNoData();
                }
            })
            .addOnFailureListener(e -> showError(e.getMessage()));
    }

    private void loadSpendingData(List<String> spendingIds) {
        FirebaseFirestore.getInstance()
            .collection("spending")
            .whereIn("id", spendingIds)
            .get()
            .addOnSuccessListener(querySnapshot -> {
                List<Spending> spendings = querySnapshot.getDocuments().stream()
                    .map(doc -> Spending.fromFirestore(doc))
                    .filter(spending -> checkDate(spending.getDateTime()))
                    .collect(Collectors.toList());

                updateCharts(spendings);
                showLoading(false);
            })
            .addOnFailureListener(e -> showError(e.getMessage()));
    }

    private void updateCharts(List<Spending> spendings) {
        if (spendings.isEmpty()) {
            showNoData();
            return;
        }

        List<Spending> filteredSpendings = filterSpendingsByType(spendings);
        
        if (isPieChartView) {
            pieChartView.setData(filteredSpendings);
            columnChartView.setVisibility(View.GONE);
            pieChartView.setVisibility(View.VISIBLE);
        } else {
            columnChartView.setData(currentPeriodTab, filteredSpendings, currentDate.getTime());
            pieChartView.setVisibility(View.GONE);
            columnChartView.setVisibility(View.VISIBLE);
        }
    }

    private List<Spending> filterSpendingsByType(List<Spending> spendings) {
        return spendings.stream()
            .filter(spending -> {
                if (spendingTypeTab == 0 && spending.getMoney() > 0) return true;
                return spendingTypeTab == 1 && spending.getMoney() < 0;
            })
            .collect(Collectors.toList());
    }

    private void showLoading(boolean show) {
        loadingIndicator.setVisibility(show ? View.VISIBLE : View.GONE);
        pieChartView.setVisibility(show ? View.GONE : View.VISIBLE);
        columnChartView.setVisibility(show ? View.GONE : View.VISIBLE);
        noDataText.setVisibility(View.GONE);
    }

    private void showNoData() {
        loadingIndicator.setVisibility(View.GONE);
        pieChartView.setVisibility(View.GONE);
        columnChartView.setVisibility(View.GONE);
        noDataText.setVisibility(View.VISIBLE);
    }
}
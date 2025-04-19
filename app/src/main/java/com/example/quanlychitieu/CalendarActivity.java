package com.example.quanlychitieu;

import android.os.Bundle;
import android.view.View;
import android.widget.ScrollView;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import com.example.quanlychitieu.models.Spending;
import com.example.quanlychitieu.views.CustomTableCalendarView;
import com.example.quanlychitieu.views.TotalSpendingView;
import com.example.quanlychitieu.views.BuildSpendingView;
import com.example.quanlychitieu.domain.SpendingFirebase;
import com.example.quanlychitieu.utils.DateUtils;

public class CalendarActivity extends AppCompatActivity {
    private CustomTableCalendarView calendarView;
    private TotalSpendingView totalSpendingView;
    private BuildSpendingView buildSpendingView;
    private ScrollView scrollView;
    private LinearLayout loadingView;
    
    private List<Spending> currentSpendingList;
    private Calendar focusedDay;
    private Calendar selectedDay;
    private SimpleDateFormat monthYearFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);
        
        initializeViews();
        setupInitialDates();
        setupListeners();
        startDataListener();
    }

    private void initializeViews() {
        calendarView = findViewById(R.id.calendarView);
        totalSpendingView = findViewById(R.id.totalSpendingView);
        buildSpendingView = findViewById(R.id.buildSpendingView);
        scrollView = findViewById(R.id.scrollView);
        loadingView = findViewById(R.id.loadingView);
        
        monthYearFormat = new SimpleDateFormat("MM_yyyy", Locale.getDefault());
    }

    private void setupInitialDates() {
        focusedDay = Calendar.getInstance();
        selectedDay = Calendar.getInstance();
        currentSpendingList = new ArrayList<>();
        
        calendarView.setFocusedDay(focusedDay.getTime());
        calendarView.setSelectedDay(selectedDay.getTime());
    }

    private void setupListeners() {
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            Calendar newDate = Calendar.getInstance();
            newDate.set(year, month, dayOfMonth);
            
            selectedDay = newDate;
            focusedDay = newDate;
            
            updateUI();
        });

        calendarView.setOnMonthChangeListener(newDate -> {
            focusedDay.setTime(newDate);
            startDataListener();
        });
    }

    private void startDataListener() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore.getInstance()
            .collection("data")
            .document(userId)
            .addSnapshotListener((snapshot, error) -> {
                if (error != null) {
                    showError(error.getMessage());
                    return;
                }
                
                if (snapshot != null && snapshot.exists()) {
                    processFirestoreData(snapshot);
                } else {
                    showLoadingState();
                }
            });
    }

    private void processFirestoreData(DocumentSnapshot snapshot) {
        Map<String, Object> data = snapshot.getData();
        if (data == null) {
            showLoadingState();
            return;
        }

        String monthKey = monthYearFormat.format(focusedDay.getTime());
        List<String> spendingIds = new ArrayList<>();
        
        if (data.containsKey(monthKey)) {
            spendingIds = (List<String>) data.get(monthKey);
        }

        SpendingFirebase.getSpendingList(spendingIds, new SpendingFirebase.OnSpendingListLoadedListener() {
            @Override
            public void onSuccess(List<Spending> spendingList) {
                if (DateUtils.isSameMonth(focusedDay.getTime(), selectedDay.getTime())) {
                    currentSpendingList = spendingList.stream()
                        .filter(spending -> DateUtils.isSameDay(spending.getDateTime(), selectedDay.getTime()))
                        .collect(Collectors.toList());
                }
                updateUI();
            }

            @Override
            public void onFailure(Exception e) {
                showError(e.getMessage());
            }
        });
    }

    private void updateUI() {
        showContent();
        
        calendarView.setFocusedDay(focusedDay.getTime());
        calendarView.setSelectedDay(selectedDay.getTime());
        
        if (!currentSpendingList.isEmpty()) {
            totalSpendingView.setVisibility(View.VISIBLE);
            totalSpendingView.setData(currentSpendingList);
        } else {
            totalSpendingView.setVisibility(View.GONE);
        }
        
        buildSpendingView.setData(currentSpendingList, selectedDay.getTime());
    }

    private void showLoadingState() {
        loadingView.setVisibility(View.VISIBLE);
        scrollView.setVisibility(View.GONE);
    }

    private void showContent() {
        loadingView.setVisibility(View.GONE);
        scrollView.setVisibility(View.VISIBLE);
    }

    private void showError(String message) {
        // Show error message using Toast or Snackbar
    }
}
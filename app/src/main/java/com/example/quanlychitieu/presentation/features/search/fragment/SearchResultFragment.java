package com.example.quanlychitieu.presentation.features.search.fragment;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.quanlychitieu.R;
import com.example.quanlychitieu.databinding.FragmentSearchResultBinding;
import com.example.quanlychitieu.domain.model.filter.Filter;
import com.example.quanlychitieu.domain.model.spending.Spending;
import com.example.quanlychitieu.domain.adapter.search.TransactionAdapter;
import com.google.android.material.chip.Chip;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SearchResultFragment extends Fragment {
    private FragmentSearchResultBinding binding;
    private TransactionAdapter adapter;
    private List<Spending> spendingList = new ArrayList<>();
    private Filter filter = new Filter(new ArrayList<>());
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    // Biến lưu trạng thái lọc hiện tại
    private int currentFilterType = 0; // 0: All, 1: Expense, 2: Income
    private Date startDate = null;
    private Date endDate = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentSearchResultBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupRecyclerView();
        setupChips();
        setupDatePickers();
    }

    private void setupRecyclerView() {
        adapter = new TransactionAdapter(getContext());
        binding.recyclerTransactions.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerTransactions.setAdapter(adapter);
    }

    private void setupChips() {
        binding.chipAll.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                currentFilterType = 0;
                binding.chipExpense.setChecked(false);
                binding.chipIncome.setChecked(false);
                filterResults();
            }
        });

        binding.chipExpense.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                currentFilterType = 1;
                binding.chipAll.setChecked(false);
                binding.chipIncome.setChecked(false);
                filterResults();
            }
        });

        binding.chipIncome.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                currentFilterType = 2;
                binding.chipAll.setChecked(false);
                binding.chipExpense.setChecked(false);
                filterResults();
            }
        });
    }

    private void setupDatePickers() {
        binding.cardStartDate.setOnClickListener(v -> showDatePicker(true));
        binding.cardEndDate.setOnClickListener(v -> showDatePicker(false));
    }

    private void showDatePicker(boolean isStartDate) {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    Calendar selectedCalendar = Calendar.getInstance();
                    selectedCalendar.set(year, month, dayOfMonth);

                    if (isStartDate) {
                        startDate = selectedCalendar.getTime();
                        binding.textStartDate.setText(dateFormat.format(startDate));
                    } else {
                        endDate = selectedCalendar.getTime();
                        binding.textEndDate.setText(dateFormat.format(endDate));
                    }

                    filterResults();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    public void updateSpendingList(List<Spending> spendingList) {
        this.spendingList = spendingList;
        filterResults();
    }

    public void updateFilter(Filter filter) {
        this.filter = filter;
    }

    private void filterResults() {
        if (spendingList == null || spendingList.isEmpty()) {
            adapter.setTransactions(new ArrayList<>());
            return;
        }

        List<Spending> filteredList = new ArrayList<>();

        for (Spending spending : spendingList) {
            // Lọc theo loại (chi tiêu/thu nhập)
            if (currentFilterType == 1 && spending.getMoney() >= 0) {
                continue; // Bỏ qua nếu đang lọc Expense nhưng là khoản thu nhập
            } else if (currentFilterType == 2 && spending.getMoney() < 0) {
                continue; // Bỏ qua nếu đang lọc Income nhưng là khoản chi tiêu
            }

            // Lọc theo ngày bắt đầu
            if (startDate != null && spending.getDateTime().before(startDate)) {
                continue;
            }

            // Lọc theo ngày kết thúc
            if (endDate != null && spending.getDateTime().after(endDate)) {
                continue;
            }

            // Nếu qua được tất cả điều kiện lọc, thêm vào danh sách kết quả
            filteredList.add(spending);
        }

        adapter.setTransactions(filteredList);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
package com.example.quanlychitieu.domain.adapter.spending;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.quanlychitieu.R;
import com.example.quanlychitieu.databinding.ItemSpendingDayBinding;
import com.example.quanlychitieu.domain.constants.SpendingType; // Import Constants
import com.example.quanlychitieu.domain.model.spending.Spending;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class SpendingDayListAdapter extends RecyclerView.Adapter<SpendingDayListAdapter.ViewHolder> {
    private static final String TAG = "SpendingDayListAdapter";
    private List<Map.Entry<Date, List<Spending>>> groupedSpendings = new ArrayList<>();
    private final NumberFormat numberFormat;
    private final SimpleDateFormat dayFormat;
    private final SimpleDateFormat dayOfWeekFormat;
    private final SimpleDateFormat monthYearFormat;
    private OnSpendingClickListener listener;

    public interface OnSpendingClickListener {
        void onSpendingClick(Spending spending);
    }

    public SpendingDayListAdapter() {
        this(new ArrayList<>(), NumberFormat.getCurrencyInstance(new Locale("vi", "VN")));
        Log.d(TAG, "Default constructor called.");
    }

    public SpendingDayListAdapter(List<Spending> initialSpendings, NumberFormat numberFormat) {
        this.numberFormat = (numberFormat != null) ? numberFormat : NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        this.dayFormat = new SimpleDateFormat("dd", Locale.getDefault());
        this.dayOfWeekFormat = new SimpleDateFormat("EEEE", new Locale("vi", "VN"));
        this.monthYearFormat = new SimpleDateFormat("MMMM, yyyy", new Locale("vi", "VN")); // Sửa lại pattern yyyy
        setItems(initialSpendings);
        Log.d(TAG, "Constructor with initial list and NumberFormat called. Initial items: " + (initialSpendings != null ? initialSpendings.size() : 0));
    }

    public void setOnSpendingClickListener(OnSpendingClickListener listener) {
        this.listener = listener;
    }

    public void setItems(List<Spending> spendings) {
        // ... (Giữ nguyên logic setItems và groupSpendingsByType) ...
        if (spendings == null) { spendings = new ArrayList<>(); Log.d(TAG, "setItems called with null list, using empty list."); }
        else { Log.d(TAG, "setItems called with " + spendings.size() + " items."); }
        Map<Date, List<Spending>> grouped = new TreeMap<>(Collections.reverseOrder());
        for (Spending spending : spendings) {
            if (spending == null || spending.getDateTime() == null) { Log.w(TAG, "Skipping null spending or spending with null date."); continue; }
            Calendar cal = Calendar.getInstance();
            cal.setTime(spending.getDateTime());
            cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0);
            Date dateOnly = cal.getTime();
            grouped.computeIfAbsent(dateOnly, k -> new ArrayList<>()).add(spending);
        }
        this.groupedSpendings = new ArrayList<>(grouped.entrySet());
        Log.d(TAG, "Grouping complete. Number of groups (days): " + this.groupedSpendings.size());
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder called");
        ItemSpendingDayBinding binding = ItemSpendingDayBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding, numberFormat, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Map.Entry<Date, List<Spending>> entry = groupedSpendings.get(position);
        Log.d(TAG, "onBindViewHolder called for position: " + position + ", Date: " + entry.getKey());
        holder.bind(entry);
    }

    @Override
    public int getItemCount() {
        int count = groupedSpendings != null ? groupedSpendings.size() : 0;
        Log.v(TAG, "getItemCount: " + count);
        return count;
    }

    // --- ViewHolder Class ---
    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemSpendingDayBinding binding;
        private final SpendingItemAdapter spendingAdapter;
        private final NumberFormat numberFormat;
        private final SimpleDateFormat dayFormat;
        private final SimpleDateFormat dayOfWeekFormat;
        private final SimpleDateFormat monthYearFormat;

        ViewHolder(ItemSpendingDayBinding binding, NumberFormat numberFormat, OnSpendingClickListener outerListener) {
            super(binding.getRoot());
            this.binding = binding;
            this.numberFormat = numberFormat;
            this.dayFormat = new SimpleDateFormat("dd", Locale.getDefault());
            this.dayOfWeekFormat = new SimpleDateFormat("EEEE", new Locale("vi", "VN"));
            this.monthYearFormat = new SimpleDateFormat("MMMM, yyyy", new Locale("vi", "VN")); // Sửa lại pattern yyyy

            spendingAdapter = new SpendingItemAdapter(new ArrayList<>(), numberFormat);
            binding.recyclerViewDaySpending.setLayoutManager(new LinearLayoutManager(binding.getRoot().getContext()));
            binding.recyclerViewDaySpending.setAdapter(spendingAdapter);
            spendingAdapter.setOnSpendingClickListener(spending -> {
                if (outerListener != null) { outerListener.onSpendingClick(spending); }
            });
        }

        void bind(Map.Entry<Date, List<Spending>> entry) {
            Date date = entry.getKey();
            List<Spending> daySpendings = entry.getValue();

            if (date == null || daySpendings == null) {
                Log.e(TAG, "Binding with null date or spending list.");
                binding.getRoot().setVisibility(View.GONE); return;
            }
            binding.getRoot().setVisibility(View.VISIBLE);

            binding.textDay.setText(dayFormat.format(date));
            binding.textDayOfWeek.setText(dayOfWeekFormat.format(date));
            binding.textMonthYear.setText(monthYearFormat.format(date));

            // *** SỬA LOGIC TÍNH VÀ HIỂN THỊ TỔNG NGÀY ***
            double totalExpenses = 0; // Chỉ tính tổng chi tiêu (dạng số dương)

            if (daySpendings != null) {
                Log.d(TAG, "Calculating total expenses for date: " + date);
                for (Spending s : daySpendings) {
                    if (s != null && !isIncomeType(s.getType())) { // Chỉ cộng nếu là chi tiêu
                        totalExpenses += Math.abs(s.getMoney()); // Cộng dồn giá trị tuyệt đối
                        Log.d(TAG, "  -> Expense Added: " + Math.abs(s.getMoney()) + " (Type: " + s.getTypeName() + ")");
                    }
                }
            }
            Log.d(TAG, "Calculated total expenses for date " + date + ": " + totalExpenses);

            // Hiển thị tổng chi tiêu của ngày
            String formattedTotalExpenses = numberFormat.format(totalExpenses); // Định dạng tổng chi tiêu
            Context context = itemView.getContext();

            // Luôn hiển thị màu đỏ và dấu trừ (trừ khi bằng 0)
            binding.textDayTotal.setTextColor(ContextCompat.getColor(context, R.color.expense_red));
            if (totalExpenses == 0) {
                binding.textDayTotal.setText(formattedTotalExpenses); // Hiển thị 0 không có dấu
                // Có thể đặt màu khác cho số 0 nếu muốn
                // binding.textDayTotal.setTextColor(ContextCompat.getColor(context, R.color.neutral_color));
            } else {
                binding.textDayTotal.setText("-" + formattedTotalExpenses); // Luôn hiển thị dấu trừ
            }
            // *** KẾT THÚC SỬA ĐỔI ***

            // Cập nhật danh sách cho RecyclerView con
            spendingAdapter.setItems(daySpendings != null ? daySpendings : new ArrayList<>());
            Log.d(TAG, "Bound data for date: " + date + ", " + (daySpendings != null ? daySpendings.size() : 0) + " items. Total Expenses: " + totalExpenses);
        }

        // Hàm helper kiểm tra loại thu nhập
        private boolean isIncomeType(int typeId) {
            return typeId == SpendingType.SALARY || typeId == SpendingType.OTHER_INCOME;
            // || typeId == SpendingType.BONUS ...
        }
    }
}
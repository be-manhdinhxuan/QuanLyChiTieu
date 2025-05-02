package com.example.quanlychitieu.domain.adapter.spending;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView; // Import ImageView
import android.widget.TextView; // Import TextView
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.example.quanlychitieu.R;
import com.example.quanlychitieu.databinding.ItemCurrencyBinding; // Đảm bảo đúng tên Binding
import com.example.quanlychitieu.domain.constants.SpendingType; // Import SpendingType constants
import com.example.quanlychitieu.domain.model.spending.Spending;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class SpendingItemAdapter extends RecyclerView.Adapter<SpendingItemAdapter.ViewHolder> {
    private static final String TAG = "SpendingItemAdapter";
    private List<Spending> spendings = new ArrayList<>();
    private final NumberFormat numberFormat;
    private OnSpendingClickListener listener;

    public interface OnSpendingClickListener {
        void onSpendingClick(Spending spending);
    }

    public SpendingItemAdapter() {
        this(new ArrayList<>(), NumberFormat.getCurrencyInstance(new Locale("vi", "VN")));
    }

    public SpendingItemAdapter(List<Spending> initialSpendings, NumberFormat numberFormat) {
        this.spendings = (initialSpendings != null) ? new ArrayList<>(initialSpendings) : new ArrayList<>();
        this.numberFormat = (numberFormat != null) ? numberFormat : NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
    }

    public void setOnSpendingClickListener(OnSpendingClickListener listener) {
        this.listener = listener;
    }

    public void setItems(List<Spending> items) {
        this.spendings = (items != null) ? new ArrayList<>(items) : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCurrencyBinding binding = ItemCurrencyBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding, numberFormat);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(spendings.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return spendings != null ? spendings.size() : 0;
    }

    // --- ViewHolder Class ---
    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemCurrencyBinding binding;
        private final NumberFormat numberFormat;

        ViewHolder(ItemCurrencyBinding binding, NumberFormat numberFormat) {
            super(binding.getRoot());
            this.binding = binding;
            this.numberFormat = numberFormat;
        }

        void bind(final Spending spending, final OnSpendingClickListener listener) {
            if (spending == null) {
                Log.w(TAG, "bind: Attempting to bind null spending object.");
                itemView.setVisibility(View.GONE);
                return;
            }
            itemView.setVisibility(View.VISIBLE);

            Context context = itemView.getContext();

            binding.ivIcon.setImageResource(getTypeIcon(spending.getType()));

            // Chỉ hiển thị typeName
            String displayText = spending.getTypeName() != null ? spending.getTypeName() : "";
            binding.tvCode.setText(displayText);

            // *** Logic hiển thị số tiền và màu sắc ***
            double amount = spending.getMoney();
            String formattedAmount = numberFormat.format(Math.abs(amount));

            if (isIncomeType(spending.getType())) {
                Log.d(TAG, "Binding item as income (Green, +): Type=" + spending.getType() + ", Amount=" + formattedAmount);
                binding.tvAmount.setTextColor(ContextCompat.getColor(context, R.color.income_green));
                binding.tvAmount.setText("+" + formattedAmount);
            } else {
                Log.d(TAG, "Binding item as expense (Red, -): Type=" + spending.getType() + ", Amount=" + formattedAmount);
                binding.tvAmount.setTextColor(ContextCompat.getColor(context, R.color.expense_red));
                binding.tvAmount.setText("-" + formattedAmount);
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onSpendingClick(spending);
                }
            });
        }

        // Hàm helper để lấy icon dựa trên type
        private int getTypeIcon(int type) {
            switch (type) {
                case SpendingType.EATING:           return R.drawable.ic_eat;
                case SpendingType.TRANSPORTATION:   return R.drawable.ic_taxi;
                case SpendingType.HOUSING:          return R.drawable.ic_house;
                case SpendingType.UTILITIES:        return R.drawable.ic_water;
                case SpendingType.PHONE:            return R.drawable.ic_phone;
                case SpendingType.ELECTRICITY:      return R.drawable.ic_electricity;
                case SpendingType.GAS:              return R.drawable.ic_gas;
                case SpendingType.TV:               return R.drawable.ic_tv;
                case SpendingType.INTERNET:         return R.drawable.ic_internet;
                case SpendingType.FAMILY:           return R.drawable.ic_family;
                case SpendingType.HOME_REPAIR:      return R.drawable.ic_house_2;
                case SpendingType.VEHICLE:          return R.drawable.ic_tools;
                case SpendingType.HEALTHCARE:       return R.drawable.ic_doctor;
                case SpendingType.INSURANCE:        return R.drawable.ic_health_insurance;
                case SpendingType.EDUCATION:        return R.drawable.ic_education;
                case SpendingType.HOUSEWARES:       return R.drawable.ic_armchair;
                case SpendingType.PERSONAL:         return R.drawable.ic_toothbrush;
                case SpendingType.PET:              return R.drawable.ic_pet;
                case SpendingType.SPORTS:           return R.drawable.ic_sports;
                case SpendingType.BEAUTY:           return R.drawable.ic_diamond;
                case SpendingType.GIFTS:            return R.drawable.ic_give_love;
                case SpendingType.ENTERTAINMENT:    return R.drawable.ic_game_pad;
                // case SpendingType.SHOPPING:      return R.drawable.ic_shopping;
                case SpendingType.SALARY:           return R.drawable.ic_money; // Thu nhập
                case SpendingType.OTHER_INCOME:     return R.drawable.ic_money_bag; // Thu nhập
                // case SpendingType.BONUS:         return R.drawable.ic_money_bag; // Thu nhập
                // case SpendingType.INVESTMENT_RETURN: return R.drawable.ic_money; // Thu nhập
                case SpendingType.OTHER:            return R.drawable.ic_box;
                default:
                    Log.w(TAG, "No specific icon found for type: " + type + ". Using default icon.");
                    return R.drawable.ic_other;
            }
        }

        // *** HÀM HELPER KIỂM TRA LOẠI THU NHẬP ***
        private boolean isIncomeType(int typeId) {
            // Chỉ trả về true nếu typeId khớp với các loại thu nhập đã định nghĩa
            return typeId == SpendingType.SALARY || typeId == SpendingType.OTHER_INCOME;
            // || typeId == SpendingType.BONUS
            // || typeId == SpendingType.INVESTMENT_RETURN;
            // Thêm các loại thu nhập khác vào đây nếu có
        }
    }
}
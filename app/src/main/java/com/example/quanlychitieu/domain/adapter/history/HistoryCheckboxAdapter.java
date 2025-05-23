package com.example.quanlychitieu.domain.adapter.history;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quanlychitieu.R;
import com.example.quanlychitieu.domain.constants.SpendingType;
import com.example.quanlychitieu.domain.model.spending.Spending;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class HistoryCheckboxAdapter extends RecyclerView.Adapter<HistoryCheckboxAdapter.ViewHolder> {
    private final Context context;
    private final List<Spending> spendings;
    private final NumberFormat currencyFormat;
    private final Set<String> selectedItems;
    private OnHistoryItemClickListener listener;

    public interface OnHistoryItemClickListener {
        void onItemClick(Spending spending);

        void onCheckboxChanged(Spending spending, boolean isChecked);
    }

    public HistoryCheckboxAdapter(Context context) {
        this.context = context;
        this.spendings = new ArrayList<>();
        this.selectedItems = new HashSet<>();
        this.currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
    }

    public void setOnHistoryItemClickListener(OnHistoryItemClickListener listener) {
        this.listener = listener;
    }

    public void updateData(List<Spending> newSpendings) {
        this.spendings.clear();
        this.spendings.addAll(newSpendings);
        notifyDataSetChanged();
    }

    public Set<String> getSelectedItems() {
        return new HashSet<>(selectedItems);
    }

    public void clearSelections() {
        selectedItems.clear();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_checkbox_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Spending spending = spendings.get(position);
        holder.bind(spending);
    }

    @Override
    public int getItemCount() {
        return spendings.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final CheckBox checkbox;
        private final ImageView ivIcon;
        private final TextView tvCode;
        private final TextView tvAmount;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            checkbox = itemView.findViewById(R.id.checkbox);
            ivIcon = itemView.findViewById(R.id.ivIcon);
            tvCode = itemView.findViewById(R.id.tvCode);
            tvAmount = itemView.findViewById(R.id.tvAmount);
        }

        void bind(Spending spending) {
            // Set checkbox state
            checkbox.setChecked(selectedItems.contains(spending.getId()));
            checkbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    selectedItems.add(spending.getId());
                } else {
                    selectedItems.remove(spending.getId());
                }
                if (listener != null) {
                    listener.onCheckboxChanged(spending, isChecked);
                }
            });

            // Set icon based on spending type
            ivIcon.setImageResource(getTypeIcon(Integer.parseInt(String.valueOf(spending.getType()))));

            // Set spending name/code
            tvCode.setText(spending.getTypeName() != null ? spending.getTypeName() : "");

            // Set amount with currency format
            String amount = currencyFormat.format(Math.abs(spending.getMoney()));
            tvAmount.setText(amount);
            tvAmount.setTextColor(ContextCompat.getColor(context, R.color.expense_red));

            // Set click listener for the whole item
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(spending);
                }
            });
        }

        private @DrawableRes int getTypeIcon(int type) {

            switch (type) {
                // Chi tiêu hàng tháng
                case SpendingType.EATING:
                    return R.drawable.ic_eat;
                case SpendingType.TRANSPORTATION:
                    return R.drawable.ic_taxi;
                case SpendingType.HOUSING:
                    return R.drawable.ic_house;
                case SpendingType.UTILITIES:
                    return R.drawable.ic_water;
                case SpendingType.PHONE:
                    return R.drawable.ic_phone;
                case SpendingType.ELECTRICITY:
                    return R.drawable.ic_electricity;
                case SpendingType.GAS:
                    return R.drawable.ic_gas;
                case SpendingType.TV:
                    return R.drawable.ic_tv;
                case SpendingType.INTERNET:
                    return R.drawable.ic_internet;

                // Chi tiêu cần thiết
                case SpendingType.FAMILY:
                    return R.drawable.ic_family;
                case SpendingType.HOME_REPAIR:
                    return R.drawable.ic_house_2;
                case SpendingType.VEHICLE:
                    return R.drawable.ic_tools;
                case SpendingType.HEALTHCARE:
                    return R.drawable.ic_doctor;
                case SpendingType.INSURANCE:
                    return R.drawable.ic_health_insurance;
                case SpendingType.EDUCATION:
                    return R.drawable.ic_education;
                case SpendingType.HOUSEWARES:
                    return R.drawable.ic_armchair;
                case SpendingType.PERSONAL:
                    return R.drawable.ic_toothbrush;
                case SpendingType.PET:
                    return R.drawable.ic_pet;

                // Giải trí
                case SpendingType.SPORTS:
                    return R.drawable.ic_sports;
                case SpendingType.BEAUTY:
                    return R.drawable.ic_diamond;
                case SpendingType.GIFTS:
                    return R.drawable.ic_give_love;
                case SpendingType.ENTERTAINMENT:
                    return R.drawable.ic_game_pad;
                // case SpendingType.SHOPPING: return R.drawable.ic_shopping; // Thêm nếu có

                // Khác
                case SpendingType.OTHER:
                    return R.drawable.ic_box;
                default:
                    return R.drawable.ic_tag; // Icon mặc định
            }
        }
    }
}
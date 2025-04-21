package com.example.quanlychitieu.domain.adapter.spending;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quanlychitieu.R;
import com.example.quanlychitieu.domain.model.spending.Spending;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SpendingAdapter extends RecyclerView.Adapter<SpendingAdapter.SpendingViewHolder> {

    private final List<Spending> spendingList;
    private final NumberFormat numberFormat;
    private final SimpleDateFormat dateFormat;

    public SpendingAdapter(List<Spending> spendingList) {
        this.spendingList = spendingList;
        this.numberFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        this.dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    }

    @NonNull
    @Override
    public SpendingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_spending, parent, false);
        return new SpendingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SpendingViewHolder holder, int position) {
        Spending spending = spendingList.get(position);
        holder.bind(spending);
    }

    @Override
    public int getItemCount() {
        return spendingList.size();
    }

    static class SpendingViewHolder extends RecyclerView.ViewHolder {
        private final ImageView iconView;
        private final TextView titleView;
        private final TextView dateView;
        private final TextView amountView;
        private final NumberFormat numberFormat;
        private final SimpleDateFormat dateFormat;

        public SpendingViewHolder(@NonNull View itemView) {
            super(itemView);
            iconView = itemView.findViewById(R.id.spendingIcon);
            titleView = itemView.findViewById(R.id.spendingTitle);
            dateView = itemView.findViewById(R.id.spendingDate);
            amountView = itemView.findViewById(R.id.spendingAmount);

            numberFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        }

        public void bind(Spending spending) {
            // Đặt tiêu đề - sử dụng typeName thay vì type (vì type là int)
            titleView.setText(spending.getTypeName());

            // Đặt biểu tượng dựa vào loại chi tiêu (type là int)
            setIconBasedOnType(spending.getType());

            // Đặt ngày giờ - sử dụng dateTime
            if (spending.getDateTime() != null) {
                dateView.setText(dateFormat.format(spending.getDateTime()));
            }

            // Đặt số tiền và màu sắc
            int amount = spending.getMoney();
            amountView.setText(numberFormat.format(amount));

            if (amount < 0) {
                amountView.setTextColor(Color.RED);
            } else {
                amountView.setTextColor(Color.GREEN);
            }
        }

        private void setIconBasedOnType(int type) {
            // Mặc định icon
            int iconResId = R.drawable.ic_monetization_on;

            // Phân loại theo từng loại chi tiêu dựa vào mã type
            switch (type) {
                case 1: // Ăn uống
                    iconResId = R.drawable.ic_restaurant;
                    break;
                case 2: // Mua sắm
                    iconResId = R.drawable.ic_shopping_cart;
                    break;
                case 3: // Giao thông
                    iconResId = R.drawable.ic_directions_car;
                    break;
                case 4: // Giải trí
                    iconResId = R.drawable.ic_movie;
                    break;
                case 5: // Sức khỏe
                    iconResId = R.drawable.ic_local_hospital;
                    break;
                case 6: // Giáo dục
                    iconResId = R.drawable.ic_school;
                    break;
                case 7: // Hóa đơn
                    iconResId = R.drawable.ic_receipt;
                    break;
                case 0: // Khác
                default:
                    iconResId = R.drawable.ic_more_horiz;
                    break;
            }

            iconView.setImageResource(iconResId);
        }
    }
}
package com.example.quanlychitieu.views.analytics.list;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.example.quanlychitieu.R;
import com.example.quanlychitieu.databinding.ViewAnalyticsSpendingPieItemBinding;
import com.example.quanlychitieu.presentation.features.spending.view.screen.ViewSpendingActivity;
import com.example.quanlychitieu.domain.model.spending.Spending;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

public class AnalyticsSpendingPieItemView extends LinearLayout {
    private ViewAnalyticsSpendingPieItemBinding binding;
    private NumberFormat numberFormat;
    private int spendingType;
    private String typeName;

    public AnalyticsSpendingPieItemView(Context context) {
        super(context);
        init();
    }

    public AnalyticsSpendingPieItemView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AnalyticsSpendingPieItemView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        binding = ViewAnalyticsSpendingPieItemBinding.inflate(LayoutInflater.from(getContext()), this, true);
        numberFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
    }

    public void setData(int type, String typeName, int amount, double total) {
        this.spendingType = type;
        this.typeName = typeName;

        // Lấy icon dựa trên loại chi tiêu
        int iconResId = getIconResourceByType(type);
        binding.imageType.setImageResource(iconResId);

        binding.textTitle.setText(typeName != null ? typeName : getDefaultTypeName(type));
        binding.textAmount.setText(numberFormat.format(amount));
        binding.textPercent.setText(String.format("%.1f%%", (amount / total) * 100));

        setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), ViewSpendingActivity.class);
            intent.putExtra("spending_type", type);
            intent.putExtra("type_name", typeName);
            getContext().startActivity(intent);
        });
    }

    private int getIconResourceByType(int type) {
        // Map loại chi tiêu với icon tương ứng
        switch (type) {
            case 1:
                return R.drawable.ic_food; // Đồ ăn
            case 2:
                return R.drawable.ic_transport; // Di chuyển
            case 3:
                return R.drawable.ic_shopping; // Mua sắm
            case 4:
                return R.drawable.ic_entertainment; // Giải trí
            case 5:
                return R.drawable.ic_bill; // Hóa đơn
            // Thêm các loại khác nếu cần
            default:
                return R.drawable.ic_money; // Mặc định
        }
    }

    private String getDefaultTypeName(int type) {
        // Cung cấp tên mặc định nếu typeName là null
        switch (type) {
            case 1:
                return "Đồ ăn";
            case 2:
                return "Di chuyển";
            case 3:
                return "Mua sắm";
            case 4:
                return "Giải trí";
            case 5:
                return "Hóa đơn";
            default:
                return "Khác";
        }
    }
}
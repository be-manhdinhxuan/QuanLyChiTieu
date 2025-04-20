package com.example.quanlychitieu.views.analytics.list;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.example.quanlychitieu.R;
import com.example.quanlychitieu.data.model.SpendingType;
import com.example.quanlychitieu.databinding.ViewAnalyticsSpendingPieItemBinding;
import com.example.quanlychitieu.presentation.view_list_spending.ViewListSpendingActivity;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

public class AnalyticsSpendingPieItemView extends LinearLayout {
    private ViewAnalyticsSpendingPieItemBinding binding;
    private NumberFormat numberFormat;
    private SpendingType spendingType;

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

    public void setData(SpendingType type, double amount, double total) {
        this.spendingType = type;
        binding.imageType.setImageResource(type.getIconResId());
        binding.textTitle.setText(type.getTitle());
        binding.textAmount.setText(numberFormat.format(amount));
        binding.textPercent.setText(String.format("%.1f%%", (amount / total) * 100));

        setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), ViewListSpendingActivity.class);
            intent.putExtra("spending_type", type);
            getContext().startActivity(intent);
        });
    }
}
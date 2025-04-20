package com.example.quanlychitieu.views.analytics.summary;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.example.quanlychitieu.R;
import com.example.quanlychitieu.databinding.ViewAnalyticsTotalReportBinding;

import java.text.NumberFormat;
import java.util.Locale;

public class AnalyticsTotalReportView extends LinearLayout {
    private ViewAnalyticsTotalReportBinding binding;
    private NumberFormat numberFormat;

    public AnalyticsTotalReportView(Context context) {
        super(context);
        init();
    }

    public AnalyticsTotalReportView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AnalyticsTotalReportView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        binding = ViewAnalyticsTotalReportBinding.inflate(LayoutInflater.from(getContext()), this, true);
        numberFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
    }

    public void setData(double spending, double income) {
        binding.spendingBox.setTitle(R.string.spending);
        binding.spendingBox.setValue(numberFormat.format(spending));
        binding.spendingBox.setValueColor(R.color.spending);

        binding.incomeBox.setTitle(R.string.income);
        binding.incomeBox.setValue(numberFormat.format(income));
        binding.incomeBox.setValueColor(R.color.income);

        binding.totalBox.setTitle(R.string.total);
        binding.totalBox.setValue(numberFormat.format(income - spending));
        binding.totalBox.setValueColor(income >= spending ? R.color.income : R.color.spending);
    }
}
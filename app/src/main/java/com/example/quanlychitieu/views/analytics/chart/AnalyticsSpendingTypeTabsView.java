package com.example.quanlychitieu.views.analytics.chart;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager2.widget.ViewPager2;

import com.example.quanlychitieu.R;
import com.example.quanlychitieu.databinding.ViewAnalyticsSpendingTypeTabsBinding;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class AnalyticsSpendingTypeTabsView extends LinearLayout {
    private ViewAnalyticsSpendingTypeTabsBinding binding;
    private ViewPager2 viewPager;
    private String[] tabTitles = { "Day", "Week", "Month" };
    private OnSpendingTypeSelectedListener listener;

    // Interface để xử lý sự kiện khi loại chi tiêu được chọn
    public interface OnSpendingTypeSelectedListener {
        void onSpendingTypeSelected(String spendingType);
    }

    public AnalyticsSpendingTypeTabsView(Context context) {
        super(context);
        init();
    }

    public AnalyticsSpendingTypeTabsView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AnalyticsSpendingTypeTabsView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        binding = ViewAnalyticsSpendingTypeTabsBinding.inflate(LayoutInflater.from(getContext()), this, true);

        // Thêm sự kiện cho các tab
        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (listener != null) {
                    // Gửi loại chi tiêu đã chọn dưới dạng chuỗi
                    String spendingType = tab.getPosition() == 0 ? "expense" : "income";
                    listener.onSpendingTypeSelected(spendingType);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                // Không cần xử lý
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // Không cần xử lý
            }
        });
    }

    // Phương thức để đăng ký listener
    public void setOnSpendingTypeSelectedListener(OnSpendingTypeSelectedListener listener) {
        this.listener = listener;
    }

    public void setupWithViewPager(@NonNull ViewPager2 viewPager) {
        this.viewPager = viewPager;
        new TabLayoutMediator(binding.tabLayout, viewPager,
                (tab, position) -> tab.setText(tabTitles[position])).attach();
    }

    public TabLayout getTabLayout() {
        return binding.tabLayout;
    }
}
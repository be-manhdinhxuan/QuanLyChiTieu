package com.example.quanlychitieu.views.analytics.chart;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager2.widget.ViewPager2;

import com.example.quanlychitieu.R;
import com.example.quanlychitieu.databinding.ViewAnalyticsChartTabsBinding;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class AnalyticsChartTabsView extends LinearLayout {
    private ViewAnalyticsChartTabsBinding binding;
    private ViewPager2 viewPager;
    private String[] tabTitles = { "Column", "Pie" };

    public AnalyticsChartTabsView(Context context) {
        super(context);
        init();
    }

    public AnalyticsChartTabsView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AnalyticsChartTabsView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        binding = ViewAnalyticsChartTabsBinding.inflate(LayoutInflater.from(getContext()), this, true);
    }

    public void setupWithViewPager(@NonNull ViewPager2 viewPager) {
        this.viewPager = viewPager;
        TabLayoutMediator mediator = new TabLayoutMediator(binding.tabLayout, viewPager,
                (tab, position) -> tab.setText(tabTitles[position]));
        mediator.attach();
    }

    public TabLayout getTabLayout() {
        return binding.tabLayout;
    }
}
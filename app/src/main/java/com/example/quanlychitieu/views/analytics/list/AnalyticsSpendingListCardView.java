package com.example.quanlychitieu.views.analytics.list;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quanlychitieu.R;
import com.example.quanlychitieu.core.function.DateUtils;
import com.example.quanlychitieu.data.model.Spending;
import com.example.quanlychitieu.databinding.ItemSpendingTypeBinding;
import com.example.quanlychitieu.databinding.ViewAnalyticsSpendingListCardBinding;
import com.example.quanlychitieu.presentation.view_list_spending.ViewListSpendingActivity;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AnalyticsSpendingListCardView extends LinearLayout {
  private ViewAnalyticsSpendingListCardBinding binding;
  private int index;
  private List<Spending> spendingList;
  private NumberFormat numberFormat;
  private String language;

  // ... rest of the code remains the same, just update the binding reference
  private void init() {
    binding = ViewAnalyticsSpendingListCardBinding.inflate(LayoutInflater.from(getContext()), this, true);
    numberFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
  }

  // ... rest of the code remains the same
}
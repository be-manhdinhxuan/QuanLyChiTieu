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
import com.example.quanlychitieu.domain.model.spending.Spending;
import com.example.quanlychitieu.databinding.ViewAnalyticsSpendingListCardBinding;
import com.example.quanlychitieu.databinding.ViewItemSpendingBinding;
import com.example.quanlychitieu.presentation.features.spending.view.screen.ViewSpendingActivity;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AnalyticsSpendingListCardView extends LinearLayout {
  private ViewAnalyticsSpendingListCardBinding binding;
  private int index;
  private List<Spending> spendingList = new ArrayList<>();
  private NumberFormat numberFormat;
  private SimpleDateFormat dayFormat;
  private SimpleDateFormat monthYearFormat;
  private SimpleDateFormat dayOfWeekFormat;

  public AnalyticsSpendingListCardView(Context context) {
    super(context);
    init();
  }

  public AnalyticsSpendingListCardView(Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  public AnalyticsSpendingListCardView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init();
  }

  private void init() {
    binding = ViewAnalyticsSpendingListCardBinding.inflate(LayoutInflater.from(getContext()), this, true);
    numberFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
    dayFormat = new SimpleDateFormat("dd", Locale.getDefault());
    monthYearFormat = new SimpleDateFormat("MMMM, yyyy", Locale.getDefault());
    dayOfWeekFormat = new SimpleDateFormat("EEEE", Locale.getDefault());
    spendingList = new ArrayList<>();

    binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    binding.recyclerView.setAdapter(new SpendingAdapter());
  }

  public void setData(List<Spending> spendingList, int index) {
    this.spendingList.clear();
    if (spendingList != null) {
      this.spendingList.addAll(spendingList);
    }
    this.index = index;

    // Xử lý hiển thị ngày tháng
    Date currentDate = new Date();
    String day = dayFormat.format(currentDate);
    String dayOfWeek = dayOfWeekFormat.format(currentDate);
    String monthYear = monthYearFormat.format(currentDate);

    binding.textDay.setText(day);
    binding.textTitle.setText(dayOfWeek);
    binding.textDate.setText(monthYear);

    // Tính tổng chi tiêu
    int total = 0;
    for (Spending spending : this.spendingList) {
      total += spending.getMoney();
    }
    binding.textTotal.setText(numberFormat.format(total));

    if (this.spendingList.isEmpty()) {
      // Nếu không có dữ liệu, hiển thị gì đó hoặc ẩn RecyclerView
      binding.recyclerView.setVisibility(View.GONE);
    } else {
      binding.recyclerView.setVisibility(View.VISIBLE);
      binding.recyclerView.getAdapter().notifyDataSetChanged();
    }
  }

  private class SpendingAdapter extends RecyclerView.Adapter<SpendingViewHolder> {
    @NonNull
    @Override
    public SpendingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
      ViewItemSpendingBinding itemBinding = ViewItemSpendingBinding.inflate(
          LayoutInflater.from(parent.getContext()), parent, false);
      return new SpendingViewHolder(itemBinding);
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
  }

  private class SpendingViewHolder extends RecyclerView.ViewHolder {
    private ViewItemSpendingBinding binding;

    public SpendingViewHolder(ViewItemSpendingBinding binding) {
      super(binding.getRoot());
      this.binding = binding;
    }

    public void bind(Spending spending) {
      // Sử dụng ID đúng theo view_item_spending.xml
      // Hiển thị tên và số tiền trong cùng một TextView
      String displayText = spending.getTypeName() + " - " + numberFormat.format(spending.getMoney());
      binding.text.setText(displayText);

      // Thiết lập icon dựa vào loại chi tiêu
      int iconResource = getIconResourceByType(spending.getType());
      binding.icon.setImageResource(iconResource);

      // Set click listener
      itemView.setOnClickListener(v -> {
        // Navigate to ViewSpendingActivity
        Intent intent = new Intent(getContext(), ViewSpendingActivity.class);
        intent.putExtra("spending_id", spending.getId());
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
  }
}
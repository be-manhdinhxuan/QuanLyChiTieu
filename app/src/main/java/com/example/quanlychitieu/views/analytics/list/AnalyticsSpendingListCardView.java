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
import com.example.quanlychitieu.domain.model.spending.Spending;
import com.example.quanlychitieu.databinding.ViewAnalyticsSpendingListCardBinding;
import com.example.quanlychitieu.databinding.ViewItemSpendingBinding;
import com.example.quanlychitieu.presentation.features.spending.view.screen.ViewSpendingActivity;
import com.example.quanlychitieu.domain.constants.SpendingType;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
      switch (type) {
        // Monthly spending types
        case SpendingType.EATING:
          return R.drawable.ic_eat;
        case SpendingType.TRANSPORTATION:
          return R.drawable.ic_taxi;
        case SpendingType.RENT:
          return R.drawable.ic_house;
        case SpendingType.WATER:
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

        // Essential spending types
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
        case SpendingType.FAMILY:
          return R.drawable.ic_family;
        case SpendingType.HOUSING:
          return R.drawable.ic_house;
        case SpendingType.UTILITIES:
          return R.drawable.ic_water;

        // Entertainment and lifestyle types
        case SpendingType.SPORTS:
          return R.drawable.ic_sports;
        case SpendingType.BEAUTY:
          return R.drawable.ic_diamond;
        case SpendingType.GIFTS:
          return R.drawable.ic_give_love;
        case SpendingType.ENTERTAINMENT:
          return R.drawable.ic_game_pad;
        case SpendingType.SHOPPING:
          return R.drawable.ic_shopping;

        // Income types
        case SpendingType.SALARY:
          return R.drawable.ic_money;
        case SpendingType.BONUS:
          return R.drawable.ic_money_bag;
        case SpendingType.INVESTMENT_RETURN:
          return R.drawable.ic_money;
        case SpendingType.OTHER_INCOME:
          return R.drawable.ic_money_bag;

        // Other types
        case SpendingType.OTHER:
          return R.drawable.ic_box;
            
        default:
          return R.drawable.ic_other;
      }
    }
  }
}
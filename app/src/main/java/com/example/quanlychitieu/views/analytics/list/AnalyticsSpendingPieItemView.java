package com.example.quanlychitieu.views.analytics.list;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.example.quanlychitieu.R;
import com.example.quanlychitieu.databinding.ViewAnalyticsSpendingPieItemBinding;
import com.example.quanlychitieu.domain.constants.SpendingType;
import com.example.quanlychitieu.presentation.features.spending.view.screen.ViewSpendingActivity;

import java.text.NumberFormat;
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
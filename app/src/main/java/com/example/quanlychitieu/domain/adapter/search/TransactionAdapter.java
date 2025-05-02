package com.example.quanlychitieu.domain.adapter.search;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quanlychitieu.R;
import com.example.quanlychitieu.domain.constants.SpendingType;
import com.example.quanlychitieu.domain.model.spending.Spending;
import com.google.android.material.card.MaterialCardView;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {

    private List<Spending> transactions = new ArrayList<>();
    private Context context;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy - HH:mm", Locale.getDefault());
    private NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

    public TransactionAdapter(Context context) {
        this.context = context;
    }

    public void setTransactions(List<Spending> transactions) {
        this.transactions = transactions;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_transaction, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        Spending spending = transactions.get(position);

        // Thiết lập thông tin danh mục (sử dụng typeName thay vì type)
        String categoryName = spending.getTypeName() != null ? spending.getTypeName()
                : "Danh mục " + spending.getType();
        holder.textCategory.setText(categoryName);

        // Thiết lập mô tả
        if (spending.getNote() != null && !spending.getNote().isEmpty()) {
            holder.textDescription.setText(spending.getNote());
            holder.textDescription.setVisibility(View.VISIBLE);
        } else {
            holder.textDescription.setVisibility(View.GONE);
        }

        // Thiết lập ngày giờ
        holder.textDate.setText(dateFormat.format(spending.getDateTime()));

        // Thiết lập số tiền và màu sắc
        String formattedAmount;
        if (spending.getMoney() < 0) {
            formattedAmount = "-" + currencyFormat.format(Math.abs(spending.getMoney()));
            holder.textAmount.setTextColor(ContextCompat.getColor(context, R.color.expense_red));
            holder.cardIcon.setCardBackgroundColor(ContextCompat.getColor(context, R.color.expense_red));
        } else {
            formattedAmount = "+" + currencyFormat.format(spending.getMoney());
            holder.textAmount.setTextColor(ContextCompat.getColor(context, R.color.income_green));
            holder.cardIcon.setCardBackgroundColor(ContextCompat.getColor(context, R.color.income_green));
        }
        holder.textAmount.setText(formattedAmount);

        // Thiết lập icon danh mục dựa trên type (int)
        setIconForCategory(holder.imageCategory, spending.getType());
    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }

    static class TransactionViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardIcon;
        ImageView imageCategory;
        TextView textCategory;
        TextView textDescription;
        TextView textDate;
        TextView textAmount;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            cardIcon = itemView.findViewById(R.id.cardIcon);
            imageCategory = itemView.findViewById(R.id.imageCategory);
            textCategory = itemView.findViewById(R.id.textCategory);
            textDescription = itemView.findViewById(R.id.textDescription);
            textDate = itemView.findViewById(R.id.textDate);
            textAmount = itemView.findViewById(R.id.textAmount);
        }
    }

    // Sửa phương thức này để nhận type là int thay vì String
    private void setIconForCategory(ImageView imageView, int type) {
        switch (type) {
            // Monthly spending types
            case SpendingType.EATING:
                imageView.setImageResource(R.drawable.ic_eat);
                break;
            case SpendingType.TRANSPORTATION:
                imageView.setImageResource(R.drawable.ic_taxi);
                break;
            case SpendingType.RENT:
                imageView.setImageResource(R.drawable.ic_house);
                break;
            case SpendingType.WATER:
                imageView.setImageResource(R.drawable.ic_water);
                break;
            case SpendingType.PHONE:
                imageView.setImageResource(R.drawable.ic_phone);
                break;
            case SpendingType.ELECTRICITY:
                imageView.setImageResource(R.drawable.ic_electricity);
                break;
            case SpendingType.GAS:
                imageView.setImageResource(R.drawable.ic_gas);
                break;
            case SpendingType.TV:
                imageView.setImageResource(R.drawable.ic_tv);
                break;
            case SpendingType.INTERNET:
                imageView.setImageResource(R.drawable.ic_internet);
                break;

            // Essential spending types
            case SpendingType.HOME_REPAIR:
                imageView.setImageResource(R.drawable.ic_house_2);
                break;
            case SpendingType.VEHICLE:
                imageView.setImageResource(R.drawable.ic_tools);
                break;
            case SpendingType.HEALTHCARE:
                imageView.setImageResource(R.drawable.ic_doctor);
                break;
            case SpendingType.INSURANCE:
                imageView.setImageResource(R.drawable.ic_health_insurance);
                break;
            case SpendingType.EDUCATION:
                imageView.setImageResource(R.drawable.ic_education);
                break;
            case SpendingType.HOUSEWARES:
                imageView.setImageResource(R.drawable.ic_armchair);
                break;
            case SpendingType.PERSONAL:
                imageView.setImageResource(R.drawable.ic_toothbrush);
                break;
            case SpendingType.PET:
                imageView.setImageResource(R.drawable.ic_pet);
                break;
            case SpendingType.FAMILY:
                imageView.setImageResource(R.drawable.ic_family);
                break;
            case SpendingType.HOUSING:
                imageView.setImageResource(R.drawable.ic_house);
                break;
            case SpendingType.UTILITIES:
                imageView.setImageResource(R.drawable.ic_water);
                break;

            // Entertainment and lifestyle types
            case SpendingType.SPORTS:
                imageView.setImageResource(R.drawable.ic_sports);
                break;
            case SpendingType.BEAUTY:
                imageView.setImageResource(R.drawable.ic_diamond);
                break;
            case SpendingType.GIFTS:
                imageView.setImageResource(R.drawable.ic_give_love);
                break;
            case SpendingType.ENTERTAINMENT:
                imageView.setImageResource(R.drawable.ic_game_pad);
                break;
            case SpendingType.SHOPPING:
                imageView.setImageResource(R.drawable.ic_shopping);
                break;

            // Income types
            case SpendingType.SALARY:
                imageView.setImageResource(R.drawable.ic_money);
                break;
            case SpendingType.BONUS:
                imageView.setImageResource(R.drawable.ic_money_bag);
                break;
            case SpendingType.INVESTMENT_RETURN:
                imageView.setImageResource(R.drawable.ic_money);
                break;
            case SpendingType.OTHER_INCOME:
                imageView.setImageResource(R.drawable.ic_money_bag);
                break;

            // Other types
            case SpendingType.OTHER:
                imageView.setImageResource(R.drawable.ic_box);
                break;
            
            default:
                imageView.setImageResource(R.drawable.ic_other);
                break;
        }
    }
}
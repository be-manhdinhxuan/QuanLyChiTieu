package com.example.quanlychitieu.domain.adapter.search;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quanlychitieu.R;
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
        // Sử dụng type (int) để xác định icon
        switch (type) {
            case 1: // Ăn uống
                imageView.setImageResource(R.drawable.ic_food);
                break;
            case 2: // Di chuyển
                imageView.setImageResource(R.drawable.ic_transport);
                break;
            case 3: // Mua sắm
                imageView.setImageResource(R.drawable.ic_shopping);
                break;
            case 4: // Thu nhập
                imageView.setImageResource(R.drawable.ic_money);
                break;
            default:
                imageView.setImageResource(R.drawable.ic_money);
                break;
        }
    }
}
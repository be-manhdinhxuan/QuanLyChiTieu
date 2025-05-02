package com.example.quanlychitieu.domain.adapter.item; // *** Đảm bảo package đúng ***

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.quanlychitieu.R; // *** Đảm bảo import R đúng ***
import com.example.quanlychitieu.domain.model.spending.Spending; // *** Đảm bảo import Spending đúng ***
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class TransactionListAdapter extends RecyclerView.Adapter<TransactionListAdapter.TransactionViewHolder> {

    private List<Spending> transactionList;
    private SimpleDateFormat dateTimeFormatter;
    private NumberFormat currencyFormatter;

    public TransactionListAdapter(Context context, List<Spending> transactionList) {
        this.transactionList = transactionList;
        this.dateTimeFormatter = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        this.currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_transaction_detail, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        Spending spending = transactionList.get(position);

        holder.noteTextView.setText(spending.getNote() != null && !spending.getNote().isEmpty() ? spending.getNote() : "Chi tiêu"); // Hiển thị "Chi tiêu" nếu note trống

        if (spending.getDateTime() != null) {
            holder.dateTimeTextView.setText(dateTimeFormatter.format(spending.getDateTime()));
            holder.dateTimeTextView.setVisibility(View.VISIBLE);
        } else {
            holder.dateTimeTextView.setVisibility(View.GONE);
        }

        holder.amountTextView.setText(currencyFormatter.format(spending.getMoney()));
        // Set màu chữ dựa vào số tiền (tùy chọn)
        // if (spending.getMoney() < 0) {
        //     holder.amountTextView.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.red));
        // } else {
        //     holder.amountTextView.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.green)); // Ví dụ cho thu nhập
        // }
    }

    @Override
    public int getItemCount() {
        return transactionList.size();
    }

    static class TransactionViewHolder extends RecyclerView.ViewHolder {
        TextView noteTextView;
        TextView dateTimeTextView;
        TextView amountTextView;

        TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            noteTextView = itemView.findViewById(R.id.transactionNoteTextView);
            dateTimeTextView = itemView.findViewById(R.id.transactionDateTimeTextView);
            amountTextView = itemView.findViewById(R.id.transactionAmountTextView);
        }
    }
}
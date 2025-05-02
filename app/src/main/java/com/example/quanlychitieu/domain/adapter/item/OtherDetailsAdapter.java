package com.example.quanlychitieu.domain.adapter.item; // *** THAY THẾ BẰNG PACKAGE CỦA BẠN ***

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quanlychitieu.R; // *** Đảm bảo import R đúng ***
import com.example.quanlychitieu.domain.model.item.LegendItem; // *** Đảm bảo import LegendItem đúng ***

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class OtherDetailsAdapter extends RecyclerView.Adapter<OtherDetailsAdapter.OtherDetailViewHolder> {

    private List<LegendItem> detailItems;
    private NumberFormat currencyFormatter;

    public OtherDetailsAdapter(Context context, List<LegendItem> detailItems) {
        this.detailItems = detailItems;
        // Khởi tạo định dạng tiền tệ Việt Nam
        this.currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
    }

    @NonNull
    @Override
    public OtherDetailViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Sử dụng lại layout item_legend.xml
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_legend, parent, false);
        return new OtherDetailViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OtherDetailViewHolder holder, int position) {
        LegendItem item = detailItems.get(position);

        // Set màu sắc (có thể ẩn đi nếu không muốn)
        holder.colorView.setBackgroundColor(item.getColor());
        // holder.colorView.setVisibility(View.GONE); // Bỏ comment nếu muốn ẩn màu

        // Set icon
        holder.iconImageView.setImageResource(item.getIconResId());

        // Set tên loại
        holder.nameTextView.setText(item.getName());

        // *** Set số tiền thực tế (thay vì phần trăm) ***
        holder.valueTextView.setText(currencyFormatter.format(item.getActualAmount()));
        // Có thể thêm màu cho số tiền nếu muốn (ví dụ: màu đỏ cho chi tiêu)
        // holder.valueTextView.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.red)); // Ví dụ
    }

    @Override
    public int getItemCount() {
        return detailItems.size();
    }

    // --- ViewHolder ---
    static class OtherDetailViewHolder extends RecyclerView.ViewHolder {
        View colorView;
        ImageView iconImageView;
        TextView nameTextView;
        TextView valueTextView;

        OtherDetailViewHolder(@NonNull View itemView) {
            super(itemView);
            // Ánh xạ các view từ layout item_legend.xml
            colorView = itemView.findViewById(R.id.legendColorView);
            iconImageView = itemView.findViewById(R.id.legendIconImageView);
            nameTextView = itemView.findViewById(R.id.legendNameTextView);
            valueTextView = itemView.findViewById(R.id.legendValueTextView);
        }
    }
}
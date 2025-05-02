package com.example.quanlychitieu.domain.adapter.item;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.quanlychitieu.R;
import com.example.quanlychitieu.domain.model.item.LegendItem;

import java.util.List;

public class LegendAdapter extends RecyclerView.Adapter<LegendAdapter.LegendViewHolder> {

    private List<LegendItem> legendItems;

    public LegendAdapter(List<LegendItem> legendItems) {
        this.legendItems = legendItems;
    }

    @NonNull
    @Override
    public LegendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_legend, parent, false);
        return new LegendViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LegendViewHolder holder, int position) {
        LegendItem item = legendItems.get(position);
        holder.colorView.setBackgroundColor(item.getColor());
        holder.iconImageView.setImageResource(item.getIconResId());
        holder.nameTextView.setText(item.getName());
        holder.valueTextView.setText(item.getValueText());
    }

    @Override
    public int getItemCount() {
        return legendItems.size();
    }

    // Phương thức để cập nhật dữ liệu (nếu cần)
    public void updateData(List<LegendItem> newItems) {
        this.legendItems = newItems;
        notifyDataSetChanged(); // Thông báo cho RecyclerView cập nhật
    }


    static class LegendViewHolder extends RecyclerView.ViewHolder {
        View colorView;
        ImageView iconImageView;
        TextView nameTextView;
        TextView valueTextView;

        LegendViewHolder(@NonNull View itemView) {
            super(itemView);
            colorView = itemView.findViewById(R.id.legendColorView);
            iconImageView = itemView.findViewById(R.id.legendIconImageView);
            nameTextView = itemView.findViewById(R.id.legendNameTextView);
            valueTextView = itemView.findViewById(R.id.legendValueTextView);
        }
    }
}
package com.example.quanlychitieu.presentation.onboarding.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quanlychitieu.R;
import com.example.quanlychitieu.presentation.onboarding.model.OnBoardingItem;

import java.util.List;

public class OnBoardingAdapter extends RecyclerView.Adapter<OnBoardingAdapter.ViewHolder> {
  private List<OnBoardingItem> items;

  public OnBoardingAdapter(List<OnBoardingItem> items) {
    this.items = items;
  }

  @NonNull
  @Override
  public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(parent.getContext())
        .inflate(R.layout.item_onboarding, parent, false);
    return new ViewHolder(view);
  }

  @Override
  public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
    OnBoardingItem item = items.get(position);
    holder.imageView.setImageResource(item.getImageResId());
    holder.textTitle.setText(item.getTitle());
    holder.textContent.setText(item.getContent());
  }

  @Override
  public int getItemCount() {
    return items.size();
  }

  static class ViewHolder extends RecyclerView.ViewHolder {
    ImageView imageView;
    TextView textTitle;
    TextView textContent;

    ViewHolder(View itemView) {
      super(itemView);
      imageView = itemView.findViewById(R.id.imageView);
      textTitle = itemView.findViewById(R.id.textTitle);
      textContent = itemView.findViewById(R.id.textContent);
    }
  }
}
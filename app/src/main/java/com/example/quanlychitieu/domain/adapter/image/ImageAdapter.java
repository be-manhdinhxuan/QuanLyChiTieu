package com.example.quanlychitieu.domain.adapter.image;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.quanlychitieu.R;

import java.util.ArrayList;
import java.util.List;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {
    private List<String> images = new ArrayList<>();
    
    public void setImages(List<Uri> imageUris) {
        this.images.clear();
        for (Uri uri : imageUris) {
            this.images.add(uri.toString());
        }
        notifyDataSetChanged();
    }

    public void addImage(String imageUri) {
        images.add(imageUri);
        notifyItemInserted(images.size() - 1);
    }

    public void removeImage(int position) {
        if (position >= 0 && position < images.size()) {
            images.remove(position);
            notifyItemRemoved(position);
        }
    }

    public List<String> getImages() {
        return new ArrayList<>(images);
    }

    @Override
    public ImageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_image, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ImageViewHolder holder, int position) {
        String imageUri = images.get(position);
        Glide.with(holder.itemView.getContext())
            .load(imageUri)
            .into(holder.imageView);
            
        holder.deleteButton.setOnClickListener(v -> removeImage(position));
    }

    @Override
    public int getItemCount() {
        return images.size();
    }

    static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        ImageButton deleteButton;

        ImageViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image);
            deleteButton = itemView.findViewById(R.id.buttonDelete);
        }
    }
}
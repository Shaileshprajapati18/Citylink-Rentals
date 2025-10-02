package com.example.citylinkrentals.Adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.citylinkrentals.R;

import java.util.ArrayList;
import java.util.List;

public class PropertyImagesAdapter extends RecyclerView.Adapter<PropertyImagesAdapter.ImageViewHolder> {

    private List<Object> images;
    private boolean showDeleteButton;
    private OnImageActionListener listener;
    private Context context;

    public interface OnImageActionListener {
        void onImageRemoved(Object image, int position);
    }

    public PropertyImagesAdapter(List<Object> images, boolean showDeleteButton, OnImageActionListener listener) {
        this.images = images;
        this.showDeleteButton = showDeleteButton;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_image_preview, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        Object image = images.get(position);

        // Load image based on type
        if (image instanceof String) {
            // Load from URL
            String imageUrl = (String) image;
            if (imageUrl != null && !imageUrl.isEmpty()) {
                // Handle localhost replacement for development
                if (imageUrl.contains("localhost")) {
                    imageUrl = imageUrl.replace("localhost", "192.168.153.1");
                }
                Glide.with(context)
                        .load(imageUrl)
                        .placeholder(R.drawable.ic_property_placeholder)
                        .error(R.drawable.ic_property_placeholder)
                        .centerCrop()
                        .into(holder.imageView);
            } else {
                // Show placeholder if URL is null or empty
                holder.imageView.setImageResource(R.drawable.ic_property_placeholder);
            }
        } else if (image instanceof Uri) {
            // Load from local URI
            Glide.with(context)
                    .load((Uri) image)
                    .placeholder(R.drawable.ic_property_placeholder)
                    .error(R.drawable.ic_property_placeholder)
                    .centerCrop()
                    .into(holder.imageView);
        } else {
            // Show placeholder for any other type
            holder.imageView.setImageResource(R.drawable.ic_property_placeholder);
        }

        // Show/hide delete button if it exists
        if (holder.deleteButton != null) {
            holder.deleteButton.setVisibility(showDeleteButton ? View.VISIBLE : View.GONE);

            // Set click listener for delete button
            holder.deleteButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onImageRemoved(image, position);
                }
            });
        }

        // Set click listener for the whole item (optional: for full-screen view)
        holder.itemView.setOnClickListener(v -> {
            // Optional: Implement full-screen image viewing here
        });
    }

    @Override
    public int getItemCount() {
        return images != null ? images.size() : 0;
    }

    // Improved update method with diffing for better performance
    public void updateImages(List<Object> newImages) {
        if (newImages == null) {
            newImages = new ArrayList<>();
        }

        // Simple implementation - for large lists, consider using DiffUtil
        this.images = newImages;
        notifyDataSetChanged();
    }

    // Add method to get image at position
    public Object getImageAt(int position) {
        if (images != null && position >= 0 && position < images.size()) {
            return images.get(position);
        }
        return null;
    }

    // Add method to get all images
    public List<Object> getAllImages() {
        return images;
    }

    static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        ImageView deleteButton;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image_preview);
            deleteButton = itemView.findViewById(R.id.delete_button);
        }
    }
}
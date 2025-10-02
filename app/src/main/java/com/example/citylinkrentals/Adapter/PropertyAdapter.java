package com.example.citylinkrentals.Adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.citylinkrentals.Activities.PropertyDetailsActivity;
import com.example.citylinkrentals.R;
import com.example.citylinkrentals.model.Property;
import com.example.citylinkrentals.model.TimeUtil;
import com.example.citylinkrentals.model.FavoriteManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.List;

public class PropertyAdapter extends RecyclerView.Adapter<PropertyAdapter.PropertyViewHolder> {
    private List<Property> properties;
    private List<Property> originalProperties;
    private Context context;
    private FavoriteManager favoriteManager;
    private OnFavoriteChangeListener favoriteChangeListener;

    public interface OnFavoriteChangeListener {
        void onFavoriteChanged();
    }

    public PropertyAdapter(Context context, List<Property> properties) {
        this.context = context;
        this.properties = properties != null ? new ArrayList<>(properties) : new ArrayList<>();
        this.originalProperties = properties != null ? new ArrayList<>(properties) : new ArrayList<>();
        this.favoriteManager = FavoriteManager.getInstance(context);
    }

    public void setOnFavoriteChangeListener(OnFavoriteChangeListener listener) {
        this.favoriteChangeListener = listener;
    }

    public void updateList(List<Property> newList) {
        this.originalProperties.clear();

        if (newList != null) {
            // Filter out properties with status "pending"
            for (Property property : newList) {
                if (property.getPropertyStatus() == null ||
                        !property.getPropertyStatus().equalsIgnoreCase("pending")) {
                    this.originalProperties.add(property);
                }
            }
            this.properties.clear();
            this.properties.addAll(originalProperties);
        } else {
            this.properties.clear();
        }

        notifyDataSetChanged();
    }

    public void updateFilteredList(List<Property> filteredList) {
        this.properties.clear();
        if (filteredList != null) {
            // Filter out properties with status "pending"
            for (Property property : filteredList) {
                if (property.getPropertyStatus() == null ||
                        !property.getPropertyStatus().equalsIgnoreCase("pending")) {
                    this.properties.add(property);
                }
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PropertyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.property_item, parent, false);
        return new PropertyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PropertyViewHolder holder, int position) {
        if (position < properties.size()) {
            Property property = properties.get(position);
            holder.bind(property);
        }
    }

    @Override
    public int getItemCount() {
        return properties.size();
    }

    class PropertyViewHolder extends RecyclerView.ViewHolder {
        ImageView propertyImage, favoriteIcon;
        TextView locationText, furnishingText, priceText, timeText, category, statusText,pgTarget;
        MaterialButton whatsappButton, callButton, viewNumberButton;

        public PropertyViewHolder(@NonNull View itemView) {
            super(itemView);
            propertyImage = itemView.findViewById(R.id.property_image);
            locationText = itemView.findViewById(R.id.location_text);
            furnishingText = itemView.findViewById(R.id.furnishing_text);
            priceText = itemView.findViewById(R.id.price_text);
            category = itemView.findViewById(R.id.category);
            timeText = itemView.findViewById(R.id.time_text);
            viewNumberButton = itemView.findViewById(R.id.view_number_button);
            whatsappButton = itemView.findViewById(R.id.whatsapp_button);
            callButton = itemView.findViewById(R.id.call_button);
            statusText = itemView.findViewById(R.id.status_text);
            favoriteIcon = itemView.findViewById(R.id.favorite_icon);
            pgTarget = itemView.findViewById(R.id.txtPgTarget);

            favoriteIcon.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && position < properties.size()) {
                    Property property = properties.get(position);
                    toggleFavorite(property, position);
                }
            });

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && position < properties.size()) {
                    Property property = properties.get(position);
                    Intent intent = new Intent(context, PropertyDetailsActivity.class);
                    intent.putExtra("PROPERTY", property);
                    context.startActivity(intent);
                }
            });
        }

        public void bind(Property property) {
            if (property == null) return;

            // Load property image
            String imageUrl = null;
            if (property.getImagePaths() != null && !property.getImagePaths().isEmpty()) {
                imageUrl = property.getImagePaths().get(0).replace("localhost", "192.168.153.1");
                Glide.with(context)
                        .load(imageUrl)
                        .placeholder(R.drawable.ic_property_placeholder)
                        .error(R.drawable.ic_property_placeholder)
                        .into(propertyImage);
            } else {
                Glide.with(context)
                        .load(R.drawable.ic_property_placeholder)
                        .into(propertyImage);
            }

            locationText.setText((property.getLocality() != null && property.getCity() != null) ?
                    property.getLocality() + ", " + property.getCity() : "Location not available");

            furnishingText.setText(property.getFurnishing() != null ? property.getFurnishing() : "N/A");
            statusText.setText(property.getAvailabilityStatus() != null ? property.getAvailabilityStatus() : "N/A");
            if (property.getPgTarget() != null && !property.getPgTarget().isEmpty()) {
                pgTarget.setVisibility(View.VISIBLE);

                pgTarget.setText(property.getPgTarget());

            } else {
                pgTarget.setVisibility(View.GONE);
            }
            double price = property.getExpectedPrice() != null ? property.getExpectedPrice() : 0.0;
            priceText.setText(String.format("₹%.0f", price));

            category.setText(property.getPropertyType() != null ? property.getPropertyType() :
                    (property.getCategory() != null ? property.getCategory() : "N/A"));

            TimeUtil.setRelativeTime(timeText, property);

            updateFavoriteIcon(property);

            // Button click listeners
            setupButtonListeners(property);
        }

        private void updateFavoriteIcon(Property property) {
            boolean isFavorite = favoriteManager.isFavorite(property);
            favoriteIcon.setImageResource(isFavorite ?
                    R.drawable.ic_heart : R.drawable.favorite_24px);
            favoriteIcon.setColorFilter(isFavorite ?
                    context.getResources().getColor(R.color.red_500) :
                    context.getResources().getColor(R.color.gray_400));
        }

        private void toggleFavorite(Property property, int position) {
            boolean wasAdded = favoriteManager.toggleFavorite(property);
            boolean isNowFavorite = favoriteManager.isFavorite(property);

            // Update UI
            updateFavoriteIcon(property);

            // Show message
            String message = isNowFavorite ? "Added to favorites" : "Removed from favorites";
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();

            // Notify listener
            if (favoriteChangeListener != null) {
                favoriteChangeListener.onFavoriteChanged();
            }
        }

        private void setupButtonListeners(Property property) {
            viewNumberButton.setOnClickListener(v -> {
                String phone = property.getPhoneNumber();
                if (phone != null && !phone.isEmpty()) {
                    Toast.makeText(context, phone, Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(context, "Phone number not available", Toast.LENGTH_SHORT).show();
                }
            });

            whatsappButton.setOnClickListener(v -> {
                String phone = property.getPhoneNumber();
                if (phone != null && !phone.isEmpty()) {
                    try {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse("https://wa.me/" + phone));
                        context.startActivity(intent);
                    } catch (Exception e) {
                        Toast.makeText(context, "WhatsApp not installed", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(context, "Phone number not available", Toast.LENGTH_SHORT).show();
                }
            });

            callButton.setOnClickListener(v -> {
                String phone = property.getPhoneNumber();
                if (phone != null && !phone.isEmpty()) {
                    String validPhone = phone.replaceAll("[^\\d+]", "");
                    if (!validPhone.isEmpty()) {
                        try {
                            Intent intent = new Intent(Intent.ACTION_DIAL);
                            intent.setData(Uri.parse("tel:" + validPhone));
                            context.startActivity(intent);
                        } catch (Exception e) {
                            Toast.makeText(context, "Cannot make phone call", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(context, "Invalid phone number", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(context, "Phone number not available", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}

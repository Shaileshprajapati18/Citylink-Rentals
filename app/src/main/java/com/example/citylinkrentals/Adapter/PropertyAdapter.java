package com.example.citylinkrentals.Adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
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

import java.util.ArrayList;
import java.util.List;

public class PropertyAdapter extends RecyclerView.Adapter<PropertyAdapter.PropertyViewHolder> {
    private List<Property> properties;
    private Context context;

    public PropertyAdapter(Context context, List<Property> properties) {
        this.context = context;
        this.properties = properties != null ? properties : new ArrayList<>();
    }

    public void updateList(List<Property> newList) {
        this.properties = newList != null ? newList : new ArrayList<>();
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
        Property property = properties.get(position);
        holder.bind(property);
    }

    @Override
    public int getItemCount() {
        return properties.size();
    }

    class PropertyViewHolder extends RecyclerView.ViewHolder {
        ImageView propertyImage;
        TextView locationText, furnishingText, priceText, timeText, viewNumberButton, category, statusText;
        ImageButton whatsappButton, callButton;

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

            itemView.setOnClickListener(v -> {
                Property property = properties.get(getAdapterPosition());
                Intent intent = new Intent(context, PropertyDetailsActivity.class);
                intent.putExtra("PROPERTY", property);
                context.startActivity(intent);
            });
        }

        public void bind(Property property) {
            // Load image with localhost replacement
            String imageUrl = null;
            if (property.getImagePaths() != null && !property.getImagePaths().isEmpty()) {
                imageUrl = property.getImagePaths().get(0).replace("localhost", "192.168.153.1");
                Glide.with(context)
                        .load(imageUrl)
                        .placeholder(R.drawable.ic_property_placeholder)
                        .into(propertyImage);
            } else {
                Glide.with(context)
                        .load(R.drawable.ic_property_placeholder)
                        .into(propertyImage);
            }

            // Set text fields with null checks
            locationText.setText((property.getLocality() != null && property.getCity() != null) ?
                    property.getLocality() + ", " + property.getCity() : "N/A");
            furnishingText.setText(property.getFurnishing() != null ? property.getFurnishing() : "N/A");
            statusText.setText(property.getAvailabilityStatus() != null ? property.getAvailabilityStatus() : "N/A");
            double price = property.getExpectedPrice() != null ? property.getExpectedPrice() : 0.0;
            priceText.setText(String.format("₹%.0f", price));
            category.setText(property.getCategory() != null ? property.getCategory() : "N/A");

            TimeUtil.setRelativeTime(timeText, property);

            // Contact buttons
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
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse("https://wa.me/" + phone));
                    context.startActivity(intent);
                } else {
                    Toast.makeText(context, "Phone number not available", Toast.LENGTH_SHORT).show();
                }
            });

            callButton.setOnClickListener(v -> {
                String phone = property.getPhoneNumber();
                if (phone != null && !phone.isEmpty()) {
                    String validPhone = phone.replaceAll("[^\\d+]", "");
                    if (!validPhone.isEmpty()) {
                        Intent intent = new Intent(Intent.ACTION_DIAL);
                        intent.setData(Uri.parse("tel:" + validPhone));
                        context.startActivity(intent);
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
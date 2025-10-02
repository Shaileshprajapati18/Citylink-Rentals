package com.example.citylinkrentals.Adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.citylinkrentals.R;
import com.example.citylinkrentals.model.Property;
import com.google.android.material.button.MaterialButton;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class MyPropertiesAdapter extends RecyclerView.Adapter<MyPropertiesAdapter.PropertyViewHolder> {

    private List<Property> properties;
    private OnPropertyActionListener listener;
    private Context context;

    public interface OnPropertyActionListener {
        void onViewClicked(Property property);
        void onEditClicked(Property property);
        void onDeleteClicked(Property property, int position);
    }

    public MyPropertiesAdapter(List<Property> properties, OnPropertyActionListener listener) {
        this.properties = properties;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PropertyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_my_property, parent, false);
        return new PropertyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PropertyViewHolder holder, int position) {
        Property property = properties.get(position);
        holder.bind(property, position);
    }

    @Override
    public int getItemCount() {
        return properties != null ? properties.size() : 0;
    }

    class PropertyViewHolder extends RecyclerView.ViewHolder {

        private ImageView propertyImage, favoriteBadge;
        private TextView statusBadge, imageCount, propertyTitle, propertyLocation;
        private TextView propertyPrice, bedroomsCount, bathroomsCount, areaSize;
        private TextView postedDate, categoryBadge,pgTarget;
        private MaterialButton viewButton, editButton, deleteButton;

        public PropertyViewHolder(@NonNull View itemView) {
            super(itemView);

            propertyImage = itemView.findViewById(R.id.property_image);
            favoriteBadge = itemView.findViewById(R.id.favorite_badge);
            statusBadge = itemView.findViewById(R.id.status_badge);
            imageCount = itemView.findViewById(R.id.image_count);

            propertyTitle = itemView.findViewById(R.id.property_title);
            propertyLocation = itemView.findViewById(R.id.property_location);
            propertyPrice = itemView.findViewById(R.id.property_price);

            bedroomsCount = itemView.findViewById(R.id.bedrooms_count);
            bathroomsCount = itemView.findViewById(R.id.bathrooms_count);
            areaSize = itemView.findViewById(R.id.area_size);

            postedDate = itemView.findViewById(R.id.posted_date);
            categoryBadge = itemView.findViewById(R.id.category_badge);

            viewButton = itemView.findViewById(R.id.view_button);
            editButton = itemView.findViewById(R.id.edit_button);
            deleteButton = itemView.findViewById(R.id.delete_button);
            pgTarget = itemView.findViewById(R.id.txtPgTarget);
        }

        public void bind(Property property, int position) {

            loadPropertyImage(property);

            setStatusBadge(property);

            setImageCount(property);

            setPropertyDetails(property);

            setPropertyFeatures(property);

            setPostedDate(property);

            setCategoryBadge(property);

            setupClickListeners(property, position);
        }

        private void loadPropertyImage(Property property) {
            if (property.getImagePaths() != null && !property.getImagePaths().isEmpty()) {
                String imageUrl = property.getImagePaths().get(0);

                if (imageUrl.contains("localhost")) {
                    imageUrl = imageUrl.replace("localhost", "192.168.153.1");
                }

                Glide.with(context)
                        .load(imageUrl)
                        .placeholder(R.drawable.ic_property_placeholder)
                        .error(R.drawable.ic_property_placeholder)
                        .centerCrop()
                        .into(propertyImage);
            } else {
                propertyImage.setImageResource(R.drawable.ic_property_placeholder);
            }
        }

        private void setStatusBadge(Property property) {

            if (property.getPropertyStatus() != null) {
                if ("PENDING".equalsIgnoreCase(property.getPropertyStatus())) {
                    statusBadge.setText("PENDING");
                    statusBadge.setBackground(context.getResources().getDrawable(R.drawable.pending_badge_bg));
                } else if ("ACTIVE".equalsIgnoreCase(property.getPropertyStatus())) {
                    statusBadge.setText("ACTIVE");
                    statusBadge.setBackground(context.getResources().getDrawable(R.drawable.verified_badge_bg));
                } else {
                    statusBadge.setText("PENDING");
                    statusBadge.setBackground(context.getResources().getDrawable(R.drawable.pending_badge_bg));
                }
            } else {
                // Handle null status by setting default to PENDING
                statusBadge.setText("PENDING");
                statusBadge.setBackground(context.getResources().getDrawable(R.drawable.pending_badge_bg));
            }
        }

        private void setImageCount(Property property) {
            int count = property.getImagePaths() != null ? property.getImagePaths().size() : 0;
            imageCount.setText(String.valueOf(count));
        }

        private void setPropertyDetails(Property property) {

            String title = "";
            if (property.getBhkType() != null) {
                title = property.getBhkType() + " ";
            }
            if (property.getPropertyType() != null) {
                title += property.getPropertyType();
            } else {
                title += "Property";
            }
            propertyTitle.setText(title.trim());

            String location = "";
            if (property.getLocality() != null && !property.getLocality().trim().isEmpty()) {
                location = property.getLocality();
            }
            if (property.getCity() != null && !property.getCity().trim().isEmpty()) {
                if (!location.isEmpty()) location += ", ";
                location += property.getCity();
            }
            if (location.isEmpty()) {
                location = "Location not specified";
            }
            propertyLocation.setText(location);

            if (property.getPgTarget() != null && !property.getPgTarget().isEmpty()) {
                pgTarget.setVisibility(View.VISIBLE);

                pgTarget.setText(property.getPgTarget());

            } else {
                pgTarget.setVisibility(View.GONE);
            }

            if (property.getExpectedPrice() != null && property.getExpectedPrice() > 0) {
                propertyPrice.setText(String.format("₹%.0f", property.getExpectedPrice()));
            } else {
                propertyPrice.setText("Price on request");
            }
        }

        private void setPropertyFeatures(Property property) {

            if (property.getBedrooms() != null && property.getBedrooms() >= 0) {
                bedroomsCount.setText(String.valueOf(property.getBedrooms()));
            } else {
                bedroomsCount.setText("0");
            }

            if (property.getBathrooms() != null && property.getBathrooms() >= 0) {
                bathroomsCount.setText(String.valueOf(property.getBathrooms()));
            } else {
                bathroomsCount.setText("0");
            }

            String area = "N/A";
            if (property.getAreaUnit() != null && !property.getAreaUnit().trim().isEmpty()) {

                String[] areaParts = property.getAreaUnit().trim().split("\\s+");
                if (areaParts.length >= 1) {
                    try {
                        double areaValue = Double.parseDouble(areaParts[0]);
                        area = String.format("%.0f", areaValue);
                    } catch (NumberFormatException e) {
                        area = areaParts[0];
                    }
                }
            }
            areaSize.setText(area);
        }

        private void setPostedDate(Property property) {
            String dateText = "Posted recently";

            if (property.getCreatedAt() != null) {
                try {
                    SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                    Date createdDate = inputFormat.parse(property.getCreatedAt());

                    if (createdDate != null) {
                        long diffInMillies = System.currentTimeMillis() - createdDate.getTime();
                        long diffInDays = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);
                        long diffInHours = TimeUnit.HOURS.convert(diffInMillies, TimeUnit.MILLISECONDS);
                        long diffInMinutes = TimeUnit.MINUTES.convert(diffInMillies, TimeUnit.MILLISECONDS);

                        if (diffInDays > 0) {
                            dateText = "Posted " + diffInDays + " day" + (diffInDays > 1 ? "s" : "") + " ago";
                        } else if (diffInHours > 0) {
                            dateText = "Posted " + diffInHours + " hour" + (diffInHours > 1 ? "s" : "") + " ago";
                        } else if (diffInMinutes > 0) {
                            dateText = "Posted " + diffInMinutes + " minute" + (diffInMinutes > 1 ? "s" : "") + " ago";
                        } else {
                            dateText = "Posted just now";
                        }
                    }
                } catch (ParseException e) {
                    dateText = "Posted recently";
                }
            }

            postedDate.setText(dateText);
        }

        private void setCategoryBadge(Property property) {
            String category = "FOR RENT";
            if (property.getCategory() != null) {
                category = "FOR " + property.getCategory().toUpperCase();
            }
            categoryBadge.setText(category);
        }

        private void setupClickListeners(Property property, int position) {
            viewButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onViewClicked(property);
                }
            });

            editButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditClicked(property);
                }
            });

            deleteButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteClicked(property, position);
                }
            });

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onViewClicked(property);
                }
            });
        }
    }
}
package com.example.citylinkrentals.Activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.example.citylinkrentals.R;
import com.example.citylinkrentals.model.Property;

public class PropertyDetailsActivity extends AppCompatActivity {

    private ImageView propertyMainImage, arrowBack, headerHeartIcon, arrowBackCenter, arrowForwardCenter;
    private TextView tvPrice, tvFurnishStatus, tvCarpetArea, tvProperty, tvPropertyType, tvBhkType,tvCategory,
            tvFloorNumber, tvFlooring, tvStatus, tvOwnership, tvParking, tvPowerBackup,
            tvPropertyFacing, tvAddress, tvDescription, tvImageCount,
            tvBathrooms, tvBedrooms, tvBalconies, tvHole, tvKitchen, tvDealerName,tv_society_name,
            whatsappButtonBottom, viewNumberButtonBottom, callButtonBottom;
    private Property property;
    private int currentImageIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_property_details);

        getWindow().setStatusBarColor(getResources().getColor(R.color.white));

        propertyMainImage = findViewById(R.id.property_main_image);
        arrowBack = findViewById(R.id.arrow_back);
        headerHeartIcon = findViewById(R.id.header_heart_icon);
        arrowBackCenter = findViewById(R.id.arrow_back_center);
        arrowForwardCenter = findViewById(R.id.arrow_forward_center);
        tvPrice = findViewById(R.id.tv_price);
        tvFurnishStatus = findViewById(R.id.tv_furnish_status);
        tvCarpetArea = findViewById(R.id.tv_carpet_area);
        tvProperty = findViewById(R.id.tv_property);
        tvPropertyType = findViewById(R.id.tv_property_type);
        tvCategory = findViewById(R.id.category);
        tvBhkType = findViewById(R.id.tv_bhk_type);
        tv_society_name = findViewById(R.id.tv_society_name);
        tvFloorNumber = findViewById(R.id.tv_floor_number);
        tvFlooring = findViewById(R.id.tv_flooring);
        tvStatus = findViewById(R.id.tv_status);
        tvOwnership = findViewById(R.id.tv_ownership);
        tvParking = findViewById(R.id.tv_parking);
        tvPowerBackup = findViewById(R.id.tv_power_backup);
        tvPropertyFacing = findViewById(R.id.tv_property_facing);
        tvAddress = findViewById(R.id.tv_address);
        tvDescription = findViewById(R.id.tv_description);
        tvImageCount = findViewById(R.id.tv_image_count);
        tvBathrooms = findViewById(R.id.tv_bathrooms);
        tvBedrooms = findViewById(R.id.tv_bedrooms);
        tvBalconies = findViewById(R.id.tv_balconies);
        tvHole = findViewById(R.id.tv_hole);
        tvKitchen = findViewById(R.id.tv_kitchen);
        tvDealerName = findViewById(R.id.tv_dealer_name);
        whatsappButtonBottom = findViewById(R.id.whatsapp_button_bottom);
        viewNumberButtonBottom = findViewById(R.id.view_number_button_bottom);
        callButtonBottom = findViewById(R.id.call_button_bottom);

        property = (Property) getIntent().getSerializableExtra("PROPERTY");
        if (property != null) {
            displayPropertyDetails(property);
        } else {
            Toast.makeText(this, "Property data not available", Toast.LENGTH_SHORT).show();
            finish();
        }

        arrowBack.setOnClickListener(v -> finish());

        headerHeartIcon.setOnClickListener(v -> {
            headerHeartIcon.setImageResource(
                    headerHeartIcon.getDrawable().getConstantState().equals(
                            getResources().getDrawable(R.drawable.favorite_24px).getConstantState()
                    ) ? R.drawable.ic_heart : R.drawable.favorite_24px
            );
        });

        arrowBackCenter.setOnClickListener(v -> {
            if (property.getImagePaths() != null && currentImageIndex > 0) {
                currentImageIndex--;
                updateImage();
            }
        });

        arrowForwardCenter.setOnClickListener(v -> {
            if (property.getImagePaths() != null && currentImageIndex < property.getImagePaths().size() - 1) {
                currentImageIndex++;
                updateImage();
            }
        });

        whatsappButtonBottom.setOnClickListener(v -> {
            String phone = property.getPhoneNumber();
            if (phone != null && !phone.isEmpty()) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("https://wa.me/" + phone));
                startActivity(intent);
            } else {
                Toast.makeText(this, "Phone number not available", Toast.LENGTH_SHORT).show();
            }
        });

        viewNumberButtonBottom.setOnClickListener(v -> {
            String phone = property.getPhoneNumber();
            if (phone != null && !phone.isEmpty()) {
                Toast.makeText(this, phone, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Phone number not available", Toast.LENGTH_SHORT).show();
            }
        });

        callButtonBottom.setOnClickListener(v -> {
            String phone = property.getPhoneNumber();
            if (phone != null && !phone.isEmpty()) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + phone));
                startActivity(intent);
            } else {
                Toast.makeText(this, "Phone number not available", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayPropertyDetails(Property property) {
        updateImage();

        if (property.getExpectedPrice() != null) {
            tvPrice.setText(String.format("₹%.0f", property.getExpectedPrice()));
        } else {
            tvPrice.setText("N/A");
        }
        tvFurnishStatus.setText(property.getFurnishing() != null ? property.getFurnishing() : "N/A");
        String areaUnit = property.getAreaUnit();
        if (areaUnit != null) {
            areaUnit = areaUnit.replaceAll("(?<=\\d)(?=\\D)", " ");
            tvCarpetArea.setText(areaUnit);
        } else {
            tvCarpetArea.setText("N/A");
        }
        tvProperty.setText(property.getPropertyKind() != null ? property.getPropertyKind() : "N/A");
        tvCategory.setText(property.getCategory() != null ? property.getCategory() : "N/A");
        tvPropertyType.setText(property.getPropertyType() != null ? property.getPropertyType() : "N/A");
        tvBhkType.setText(property.getBhkType() != null ? property.getBhkType() : "N/A");
        tv_society_name.setText(property.getSocietyName() != null ? property.getSocietyName() : "N/A");
        tvFloorNumber.setText(property.getTotalFloor() != null ? String.valueOf(property.getTotalFloor()) : "N/A");
        tvFlooring.setText(property.getFlooringType() != null ? property.getFlooringType() : "N/A");
        tvStatus.setText(property.getAvailabilityStatus() != null ? property.getAvailabilityStatus() : "N/A");
        tvOwnership.setText(property.getOwnership() != null ? property.getOwnership() : "N/A");
        tvParking.setText(property.getParking() != null ? (property.getParking() ? "Yes" : "No") : "N/A");
        tvPowerBackup.setText(property.getPowerBackup() != null ? property.getPowerBackup() : "N/A");
        tvPropertyFacing.setText(property.getPropertyFacing() != null ? property.getPropertyFacing() : "N/A");
        tvAddress.setText((property.getLocality() != null && property.getCity() != null) ?
                property.getLocality() + ", " + property.getCity() : "N/A");
        tvDescription.setText(property.getDescription() != null ? property.getDescription() : "N/A");
        tvBathrooms.setText(property.getBathrooms() != null ? property.getBathrooms() + " Bathrooms" : "N/A");
        tvBedrooms.setText(property.getBedrooms() != null ? property.getBedrooms() + " Bedrooms" : "N/A");
        tvBalconies.setText(property.getBalcony() != null ? property.getBalcony() + " Balconies" : "N/A");
        tvHole.setText(property.getHole() != null ? property.getHole() + " Hole" : "N/A");
        tvKitchen.setText(property.getKitchen() != null ? property.getKitchen() + " Kitchen" : "N/A");
        tvDealerName.setText(property.getUser() != null && property.getUser().getUsername() != null ?
                property.getUser().getUsername() : "Unknown Dealer");
    }

    private void updateImage() {
        if (property.getImagePaths() != null && !property.getImagePaths().isEmpty()) {

            String imageUrl = property.getImagePaths().get(currentImageIndex).replace("localhost", "192.168.153.1");

            Glide.with(this)
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_property_placeholder)
                    .into(propertyMainImage);
            tvImageCount.setText((currentImageIndex + 1) + "/" + property.getImagePaths().size());
        } else {
            Glide.with(this)
                    .load(R.drawable.ic_property_placeholder)
                    .into(propertyMainImage);
            tvImageCount.setText("1/1");
        }
    }
}
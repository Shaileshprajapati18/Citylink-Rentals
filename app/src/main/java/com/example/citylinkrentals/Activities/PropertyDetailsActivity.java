package com.example.citylinkrentals.Activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.citylinkrentals.R;
import com.example.citylinkrentals.Adapter.ImagePagerAdapter;
import com.example.citylinkrentals.model.Property;
import com.example.citylinkrentals.model.FavoriteManager;
import com.google.android.material.button.MaterialButton;

import de.hdodenhof.circleimageview.CircleImageView;

public class PropertyDetailsActivity extends AppCompatActivity {

    // UI Components
    private ViewPager2 imageViewPager;
    private ImageView back_icon;
    private MaterialButton favoriteButton, whatsappButton, callButton;
    private CircleImageView dealerAvatar;

    // TextViews for property information
    private TextView priceText, furnishStatusBadge, categoryBadge, imageCounter;
    private TextView bedroomsCount, bathroomsCount, balconiesCount, kitchenCount, hallCount;
    private TextView parkingStatus;

    // Property details TextViews
    private TextView propertyTypeValue, propertyKindValue, bhkTypeValue, carpetAreaValue, totalFloorsValue;
    private TextView propertyFacingValue, ownershipValue, powerBackupValue, flooringTypeValue, statusValue;

    // About property TextViews
    private TextView addressText, descriptionText, dealerName, phoneNumberDisplay;

    // Layouts for conditional visibility
    private LinearLayout featuresRow1, featuresRow2;
    private LinearLayout propertyTypeRow, propertyKindRow, bhkTypeRow;
    private LinearLayout totalFloorsRow, propertyFacingRow, powerBackupRow, flooringTypeRow;

    private Property property;
    private int currentImageIndex = 0;
    private ImagePagerAdapter imagePagerAdapter;
    private FavoriteManager favoriteManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_property_details);

        favoriteManager = FavoriteManager.getInstance(this);

        getWindow().setStatusBarColor(getResources().getColor(R.color.main_background_color));
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        );

        initializeViews();

        property = (Property) getIntent().getSerializableExtra("PROPERTY");
        if (property != null) {
            displayPropertyDetails(property);
            setupImageGallery();
            updateFavoriteButton();
            setupViewsBasedOnPropertyType();
        } else {
            showError("Property data not available");
            return;
        }

        setupClickListeners();
    }

    private void initializeViews() {
        // Image gallery
        imageViewPager = findViewById(R.id.image_viewpager);

        // Toolbar and buttons
        back_icon = findViewById(R.id.backIcon);
        favoriteButton = findViewById(R.id.favorite_button);
        whatsappButton = findViewById(R.id.whatsapp_button);
        callButton = findViewById(R.id.call_button);

        // Property info TextViews
        priceText = findViewById(R.id.price_text);
        furnishStatusBadge = findViewById(R.id.furnish_status_badge);
        categoryBadge = findViewById(R.id.category_badge);
        imageCounter = findViewById(R.id.image_counter);

        // Feature counts
        bedroomsCount = findViewById(R.id.bedrooms_count);
        bathroomsCount = findViewById(R.id.bathrooms_count);
        balconiesCount = findViewById(R.id.balconies_count);
        kitchenCount = findViewById(R.id.kitchen_count);
        hallCount = findViewById(R.id.hall_count);
        parkingStatus = findViewById(R.id.parking_status);

        propertyTypeValue = findViewById(R.id.property_type_value);
        propertyKindValue = findViewById(R.id.property_kind_value);
        bhkTypeValue = findViewById(R.id.bhk_type_value);
        carpetAreaValue = findViewById(R.id.carpet_area_value);
        totalFloorsValue = findViewById(R.id.total_floors_value);
        propertyFacingValue = findViewById(R.id.property_facing_value);
        ownershipValue = findViewById(R.id.ownership_value);
        powerBackupValue = findViewById(R.id.power_backup_value);
        flooringTypeValue = findViewById(R.id.flooring_type_value);
        statusValue = findViewById(R.id.status_value);

        addressText = findViewById(R.id.address_text);
        descriptionText = findViewById(R.id.description_text);
        dealerName = findViewById(R.id.dealer_name);
        phoneNumberDisplay = findViewById(R.id.phone_number_display);
        dealerAvatar = findViewById(R.id.dealer_avatar);

        featuresRow1 = findViewById(R.id.features_row1);
        featuresRow2 = findViewById(R.id.features_row2);
        propertyTypeRow = findViewById(R.id.property_type_row);
        propertyKindRow = findViewById(R.id.property_kind_row);
        bhkTypeRow = findViewById(R.id.bhk_type_row);
        totalFloorsRow = findViewById(R.id.total_floors_row);
        propertyFacingRow = findViewById(R.id.property_facing_row);
        powerBackupRow = findViewById(R.id.power_backup_row);
        flooringTypeRow = findViewById(R.id.flooring_type_row);

        back_icon.setOnClickListener(v -> finish());
    }

    private void setupViewsBasedOnPropertyType() {
        String propertyType = property.getPropertyType();
        if (propertyType == null) return;

        propertyType = propertyType.toLowerCase();

        // Determine property category
        String category;
        if (propertyType.contains("apartment") || propertyType.contains("flat") ||
                propertyType.contains("independent house") || propertyType.contains("room") ||
                propertyType.contains("p.g") || propertyType.contains("paying guest")) {
            category = "RESIDENTIAL";
        } else if (propertyType.contains("shop") || propertyType.contains("office") ||
                propertyType.contains("godown")) {
            category = "COMMERCIAL";
        } else if (propertyType.contains("plot") || propertyType.contains("land")) {
            category = "PLOT";
        } else {
            category = "RESIDENTIAL"; // Default
        }

        // Configure views based on category
        switch (category) {
            case "RESIDENTIAL":
                // Show all features
                featuresRow1.setVisibility(View.VISIBLE);
                featuresRow2.setVisibility(View.VISIBLE);

                // Show all property details
                propertyTypeRow.setVisibility(View.VISIBLE);
                propertyKindRow.setVisibility(View.VISIBLE);
                bhkTypeRow.setVisibility(View.VISIBLE);
                totalFloorsRow.setVisibility(View.VISIBLE);
                propertyFacingRow.setVisibility(View.VISIBLE);
                powerBackupRow.setVisibility(View.VISIBLE);
                flooringTypeRow.setVisibility(View.VISIBLE);
                break;

            case "COMMERCIAL":
                // Show only relevant features (bathrooms and parking)
                featuresRow1.setVisibility(View.VISIBLE);
                featuresRow2.setVisibility(View.GONE);

                // Hide bedrooms, balconies, kitchen, hall
                findViewById(R.id.bedrooms_layout).setVisibility(View.GONE);
                findViewById(R.id.balconies_layout).setVisibility(View.GONE);
                findViewById(R.id.kitchen_layout).setVisibility(View.GONE);
                findViewById(R.id.hall_layout).setVisibility(View.GONE);

                // Show only relevant property details
                propertyTypeRow.setVisibility(View.VISIBLE);
                propertyKindRow.setVisibility(View.VISIBLE);
                bhkTypeRow.setVisibility(View.GONE); // BHK not relevant for commercial
                totalFloorsRow.setVisibility(View.VISIBLE);
                propertyFacingRow.setVisibility(View.GONE); // Facing not relevant for commercial
                powerBackupRow.setVisibility(View.VISIBLE);
                flooringTypeRow.setVisibility(View.VISIBLE);
                break;

            case "PLOT":
                // Hide all features
                featuresRow1.setVisibility(View.GONE);
                featuresRow2.setVisibility(View.GONE);

                // Show only relevant property details
                propertyTypeRow.setVisibility(View.VISIBLE);
                propertyKindRow.setVisibility(View.VISIBLE);
                bhkTypeRow.setVisibility(View.GONE); // BHK not relevant for plots
                totalFloorsRow.setVisibility(View.GONE); // Total floors not relevant for plots
                propertyFacingRow.setVisibility(View.GONE); // Facing not relevant for plots
                powerBackupRow.setVisibility(View.GONE); // Power backup not relevant for plots
                flooringTypeRow.setVisibility(View.GONE); // Flooring not relevant for plots
                break;
        }
    }

    private void updateFavoriteButton() {
        boolean isFavorite = favoriteManager.isFavorite(property);
        int iconRes = isFavorite ? R.drawable.ic_heart : R.drawable.favorite_24px;
        favoriteButton.setIconResource(iconRes);

        // Update button color
        int colorRes = isFavorite ? R.color.red_500 : R.color.gray_400;
        favoriteButton.setIconTint(getResources().getColorStateList(colorRes));
    }

    private void setupImageGallery() {
        if (property.getImagePaths() != null && !property.getImagePaths().isEmpty()) {
            // Process image URLs - replace localhost with actual IP
            for (int i = 0; i < property.getImagePaths().size(); i++) {
                String imageUrl = property.getImagePaths().get(i);
                if (imageUrl.contains("localhost")) {
                    imageUrl = imageUrl.replace("localhost", "192.168.153.1");
                    property.getImagePaths().set(i, imageUrl);
                }
            }

            // Setup ViewPager2 with adapter
            imagePagerAdapter = new ImagePagerAdapter(property.getImagePaths());
            imageViewPager.setAdapter(imagePagerAdapter);

            // Update counter on page change
            imageViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                @Override
                public void onPageSelected(int position) {
                    super.onPageSelected(position);
                    currentImageIndex = position;
                    updateImageCounter();
                }
            });

            updateImageCounter();
        } else {
            // Show placeholder if no images
            imagePagerAdapter = new ImagePagerAdapter(null);
            imageViewPager.setAdapter(imagePagerAdapter);
            imageCounter.setText("1/1");
        }
    }

    private void displayPropertyDetails(Property property) {
        // Price and basic info
        if (property.getExpectedPrice() != null && property.getExpectedPrice() > 0) {
            priceText.setText(String.format("₹%.0f", property.getExpectedPrice()));
        } else {
            priceText.setText("Price on request");
        }

        // Furnish status
        String furnishStatus = getValueOrNA(property.getFurnishing());
        furnishStatusBadge.setText(furnishStatus);

        // Category
        String category = property.getCategory() != null ?
                "FOR " + property.getCategory().toUpperCase() : "FOR RENT";
        categoryBadge.setText(category);

        // Display all feature counts
        displayFeatureCounts();

        // Property details
        propertyTypeValue.setText(getValueOrNA(property.getPropertyType()));
        propertyKindValue.setText(getValueOrNA(property.getPropertyKind()));
        bhkTypeValue.setText(getValueOrNA(property.getBhkType()));

        // Carpet area with unit
        String areaUnit = getValueOrNA(property.getAreaUnit());
        carpetAreaValue.setText(areaUnit);

        // Total floors
        if (property.getTotalFloor() != null && property.getTotalFloor() > 0) {
            totalFloorsValue.setText(String.valueOf(property.getTotalFloor()));
        } else {
            totalFloorsValue.setText("N/A");
        }

        // Property Facing, Ownership, Power Backup
        propertyFacingValue.setText(getValueOrNA(property.getPropertyFacing()));
        ownershipValue.setText(getValueOrNA(property.getOwnership()));
        powerBackupValue.setText(getValueOrNA(property.getPowerBackup()));

        // Flooring type
        flooringTypeValue.setText(getValueOrNA(property.getFlooringType()));

        // Status with color coding
        String status = getValueOrNA(property.getAvailabilityStatus());
        statusValue.setText(status);
        if ("Ready to move".equalsIgnoreCase(status)) {
            statusValue.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        } else if ("Under construction".equalsIgnoreCase(status)) {
            statusValue.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
        }

        // Address
        displayAddress();

        // Description
        String description = property.getDescription() != null && !property.getDescription().trim().isEmpty() ?
                property.getDescription() : "No description available for this property.";
        descriptionText.setText(description);

        // Dealer info
        displayDealerInfo();
    }

    private void displayFeatureCounts() {
        // Bedrooms
        if (property.getBedrooms() != null && property.getBedrooms() >= 0) {
            bedroomsCount.setText(String.valueOf(property.getBedrooms()));
        } else {
            bedroomsCount.setText("0");
        }

        // Bathrooms
        if (property.getBathrooms() != null && property.getBathrooms() >= 0) {
            bathroomsCount.setText(String.valueOf(property.getBathrooms()));
        } else {
            bathroomsCount.setText("0");
        }

        // Balconies
        if (property.getBalcony() != null && property.getBalcony() >= 0) {
            balconiesCount.setText(String.valueOf(property.getBalcony()));
        } else {
            balconiesCount.setText("0");
        }

        // Kitchen and Hall
        if (property.getKitchen() != null && property.getKitchen() >= 0) {
            kitchenCount.setText(String.valueOf(property.getKitchen()));
        } else {
            kitchenCount.setText("0");
        }

        if (property.getHole() != null && property.getHole() >= 0) {
            hallCount.setText(String.valueOf(property.getHole()));
        } else {
            hallCount.setText("0");
        }

        // Parking status
        if (property.getParking()) {
            parkingStatus.setText("Yes");
            parkingStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        } else {
            parkingStatus.setText("No");
            parkingStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        }
    }

    private void displayAddress() {
        String address = "";

        // Build complete address
        if (property.getSocietyName() != null && !property.getSocietyName().trim().isEmpty()) {
            address = property.getSocietyName();
        }

        if (property.getLocality() != null && !property.getLocality().trim().isEmpty()) {
            if (!address.isEmpty()) address += ", ";
            address += property.getLocality();
        }

        if (property.getCity() != null && !property.getCity().trim().isEmpty()) {
            if (!address.isEmpty()) address += ", ";
            address += property.getCity();
        }

        if (address.isEmpty()) {
            address = "Location not specified";
        }

        addressText.setText(address);
    }

    private void displayDealerInfo() {
        String dealerDisplayName = "Unknown Dealer";

        // Try to get dealer name from user object or phone number
        if (property.getUser() != null && property.getUser().getUsername() != null &&
                !property.getUser().getUsername().trim().isEmpty()) {
            dealerDisplayName = property.getUser().getUsername();
        } else if (property.getPhoneNumber() != null && !property.getPhoneNumber().trim().isEmpty()) {
            // Use first part of phone number as fallback
            String phone = property.getPhoneNumber();
            if (phone.length() > 4) {
                dealerDisplayName = "User " + phone.substring(phone.length() - 4);
            }
        }

        dealerName.setText(dealerDisplayName);

        // Display phone number
        String phoneDisplay = getValueOrNA(property.getPhoneNumber());
        if (!"N/A".equals(phoneDisplay) && phoneDisplay.length() > 6) {
            // Format phone number for display (hide middle digits)
            String formatted = phoneDisplay.substring(0, 3) + "****" +
                    phoneDisplay.substring(phoneDisplay.length() - 3);
            phoneNumberDisplay.setText(formatted);
        } else {
            phoneNumberDisplay.setText(phoneDisplay);
        }
    }

    private void setupClickListeners() {
        // Favorite button
        favoriteButton.setOnClickListener(v -> toggleFavorite());

        // WhatsApp button
        whatsappButton.setOnClickListener(v -> openWhatsApp());

        // Call button
        callButton.setOnClickListener(v -> makePhoneCall());
    }

    private void toggleFavorite() {
        boolean wasToggled = favoriteManager.toggleFavorite(property);
        if (wasToggled) {
            updateFavoriteButton();
            boolean isFavorite = favoriteManager.isFavorite(property);
            String message = isFavorite ? "Added to favorites" : "Removed from favorites";
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Unable to update favorites", Toast.LENGTH_SHORT).show();
        }
    }

    private void openWhatsApp() {
        String phone = getCleanPhoneNumber();
        if (phone != null && !phone.isEmpty()) {
            try {
                String propertyInfo = buildPropertyInfoMessage();
                String message = "Hi, I'm interested in your property:\n\n" + propertyInfo;

                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("https://wa.me/" + phone + "?text=" + Uri.encode(message)));
                startActivity(intent);
            } catch (Exception e) {
                showError("WhatsApp not installed or unable to open");
            }
        } else {
            showError("Phone number not available");
        }
    }

    private void makePhoneCall() {
        String phone = getCleanPhoneNumber();
        if (phone != null && !phone.isEmpty()) {
            try {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + phone));
                startActivity(intent);
            } catch (Exception e) {
                showError("Unable to make call");
            }
        } else {
            showError("Phone number not available");
        }
    }

    private String buildPropertyInfoMessage() {
        StringBuilder info = new StringBuilder();

        info.append("🏠 ").append(getValueOrNA(property.getPropertyType()));
        if (property.getBhkType() != null) {
            info.append(" - ").append(property.getBhkType());
        }

        if (property.getExpectedPrice() != null && property.getExpectedPrice() > 0) {
            info.append("\n💰 Price: ₹").append(String.format("%.0f", property.getExpectedPrice()));
        }

        if (property.getCity() != null) {
            info.append("\n📍 Location: ").append(property.getCity());
            if (property.getLocality() != null) {
                info.append(", ").append(property.getLocality());
            }
        }

        return info.toString();
    }

    private void updateImageCounter() {
        if (property.getImagePaths() != null && !property.getImagePaths().isEmpty()) {
            imageCounter.setText((currentImageIndex + 1) + "/" + property.getImagePaths().size());
        } else {
            imageCounter.setText("1/1");
        }
    }

    // Helper methods
    private String getCleanPhoneNumber() {
        String phone = property.getPhoneNumber();
        if (phone == null || phone.trim().isEmpty()) {
            return null;
        }

        // Remove any spaces, hyphens, or other formatting
        phone = phone.replaceAll("[\\s\\-\\(\\)]", "");

        // Remove country code if present
        if (phone.startsWith("+91")) {
            phone = phone.substring(3);
        } else if (phone.startsWith("91") && phone.length() > 10) {
            phone = phone.substring(2);
        }

        return phone;
    }

    private String getValueOrNA(String value) {
        return value != null && !value.trim().isEmpty() ? value : "N/A";
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        if (property == null) {
            finish();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
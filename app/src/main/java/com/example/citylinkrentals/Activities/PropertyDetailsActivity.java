package com.example.citylinkrentals.Activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.example.citylinkrentals.R;
import com.example.citylinkrentals.Adapter.ImagePagerAdapter;
import com.example.citylinkrentals.model.Property;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

public class PropertyDetailsActivity extends AppCompatActivity {

    // UI Components
    private ViewPager2 imageViewPager;
    private MaterialToolbar toolbar;
    private MaterialButton favoriteButton, whatsappButton, callButton;

    // TextViews for property information
    private TextView priceText, furnishStatusBadge, categoryBadge, imageCounter;
    private TextView bedroomsCount, bathroomsCount, balconiesCount;
    private TextView propertyTypeValue, bhkTypeValue, floorNumberValue, statusValue;
    private TextView addressText, descriptionText, dealerName;

    private Property property;
    private int currentImageIndex = 0;
    private ImagePagerAdapter imagePagerAdapter;
    private boolean isFavorite = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_property_details);

        // Set status bar color for modern look
        getWindow().setStatusBarColor(getResources().getColor(android.R.color.transparent));
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        );

        initializeViews();
        setupToolbar();

        // Get property data from intent
        property = (Property) getIntent().getSerializableExtra("PROPERTY");
        if (property != null) {
            displayPropertyDetails(property);
            setupImageGallery();
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
        toolbar = findViewById(R.id.toolbar);
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

        // Property details
        propertyTypeValue = findViewById(R.id.property_type_value);
        bhkTypeValue = findViewById(R.id.bhk_type_value);
        floorNumberValue = findViewById(R.id.floor_number_value);
        statusValue = findViewById(R.id.status_value);

        // About property
        addressText = findViewById(R.id.address_text);
        descriptionText = findViewById(R.id.description_text);
        dealerName = findViewById(R.id.dealer_name);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupImageGallery() {
        if (property.getImagePaths() != null && !property.getImagePaths().isEmpty()) {
            // Process image URLs
            for (int i = 0; i < property.getImagePaths().size(); i++) {
                String imageUrl = property.getImagePaths().get(i).replace("localhost", "192.168.153.1");
                property.getImagePaths().set(i, imageUrl);
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
        if (property.getExpectedPrice() != null) {
            priceText.setText(String.format("₹%.0f", property.getExpectedPrice()));
        } else {
            priceText.setText("Price on request");
        }

        // Furnish status
        String furnishStatus = property.getFurnishing() != null ? property.getFurnishing() : "Not specified";
        furnishStatusBadge.setText(furnishStatus);

        // Category
        String category = property.getCategory() != null ? property.getCategory().toUpperCase() : "FOR RENT";
        categoryBadge.setText(category);

        // Feature counts
        displayFeatureCounts();

        // Property details
        propertyTypeValue.setText(getValueOrNA(property.getPropertyType()));
        bhkTypeValue.setText(getValueOrNA(property.getBhkType()));

        // Floor number with proper formatting
        String floorInfo = property.getTotalFloor() != null ?
                getOrdinalNumber(property.getTotalFloor()) + " Floor" : "N/A";
        floorNumberValue.setText(floorInfo);

        // Status with color coding
        String status = getValueOrNA(property.getAvailabilityStatus());
        statusValue.setText(status);
        if ("Ready to move".equalsIgnoreCase(status)) {
            statusValue.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        }

        // Address
        String address = "";
        if (property.getLocality() != null && property.getCity() != null) {
            address = property.getLocality() + ", " + property.getCity();
        } else if (property.getCity() != null) {
            address = property.getCity();
        } else {
            address = "Location not specified";
        }
        addressText.setText(address);

        // Description
        String description = property.getDescription() != null ?
                property.getDescription() : "No description available";
        descriptionText.setText(description);

        // Dealer info
        String dealerDisplayName = "Unknown Dealer";
        if (property.getUser() != null && property.getUser().getUsername() != null) {
            dealerDisplayName = property.getUser().getUsername();
        }
        dealerName.setText(dealerDisplayName);
    }

    private void displayFeatureCounts() {
        // Bedrooms
        if (property.getBedrooms() != null && property.getBedrooms() > 0) {
            bedroomsCount.setText(String.valueOf(property.getBedrooms()));
        } else {
            bedroomsCount.setText("0");
        }

        // Bathrooms
        if (property.getBathrooms() != null && property.getBathrooms() > 0) {
            bathroomsCount.setText(String.valueOf(property.getBathrooms()));
        } else {
            bathroomsCount.setText("0");
        }

        // Balconies
        if (property.getBalcony() != null && property.getBalcony() > 0) {
            balconiesCount.setText(String.valueOf(property.getBalcony()));
        } else {
            balconiesCount.setText("0");
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
        isFavorite = !isFavorite;
        int iconRes = isFavorite ? R.drawable.ic_heart : R.drawable.favorite_24px;
        favoriteButton.setIconResource(iconRes);

        // You can add database operations here to save/remove favorite
        String message = isFavorite ? "Added to favorites" : "Removed from favorites";
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void openWhatsApp() {
        String phone = getPhoneNumber();
        if (phone != null && !phone.isEmpty()) {
            try {
                String message = "Hi, I'm interested in your property: " +
                        (property.getPropertyType() != null ? property.getPropertyType() : "Property") +
                        " in " + (property.getCity() != null ? property.getCity() : "");

                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("https://wa.me/" + phone + "?text=" +
                        Uri.encode(message)));
                startActivity(intent);
            } catch (Exception e) {
                showError("WhatsApp not installed");
            }
        } else {
            showError("Phone number not available");
        }
    }

    private void makePhoneCall() {
        String phone = getPhoneNumber();
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

    private void updateImageCounter() {
        if (property.getImagePaths() != null && !property.getImagePaths().isEmpty()) {
            imageCounter.setText((currentImageIndex + 1) + "/" + property.getImagePaths().size());
        } else {
            imageCounter.setText("1/1");
        }
    }

    // Helper methods
    private String getPhoneNumber() {
        return property.getPhoneNumber();
    }

    private String getValueOrNA(String value) {
        return value != null && !value.trim().isEmpty() ? value : "N/A";
    }

    private String getOrdinalNumber(Integer number) {
        if (number == null) return "N/A";

        String[] suffixes = {"th", "st", "nd", "rd", "th", "th", "th", "th", "th", "th"};
        int value = number % 100;

        if (value >= 11 && value <= 13) {
            return number + "th";
        }

        return number + suffixes[number % 10];
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
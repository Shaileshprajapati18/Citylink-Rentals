package com.example.citylinkrentals.Activities;

import static android.content.ContentValues.TAG;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.citylinkrentals.R;
import com.example.citylinkrentals.model.PropertyListRequest;
import com.example.citylinkrentals.model.PropertyListResponse;
import com.example.citylinkrentals.network.ApiService;
import com.example.citylinkrentals.network.RetrofitClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PostPropertyActivity extends AppCompatActivity {

    private EditText phoneNumberInput, cityInput, localityInput, apartmentNameInput, totalFloorsInput, expectedPriceInput, descriptionInput,input_carpet_area;
    private Spinner areaUnitSpinner, flooringTypeSpinner;
    private LinearLayout categoryLayout, propertyKindLayout, propertyTypeLayout, bhkTypeLayout, bedroomsLayout, bathroomsLayout, balconiesLayout, furnishingLayout, availabilityLayout, ownershipLayout, parkingLayout, powerBackupLayout, propertyFacingLayout,kitchen_layout,holes_layout;
    private static final int MAX_IMAGES = 10;
    private static final int MIN_IMAGES = 1;
    private static final int REQUEST_CODE_PERMISSIONS = 100;
    private ImageView btnSelectImages,back_arrow;
    private List<Uri> imageUris = new ArrayList<>();
    private List<ImageView> imagePreviews = new ArrayList<>();
    private List<ImageView> closeIcons = new ArrayList<>();
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private String selectedCategory, selectedPropertyKind, selectedPropertyType, selectedBhkType, selectedBedrooms, selectedBathrooms, selectedBalconies, selectedFurnishing, selectedAvailability, selectedOwnership, selectedParking, selectedPowerBackup, selectedPropertyFacing,selectedHole,selectedKicthen;
    private Button saveAndSubmitButton;
    TextView showMoreButton;
    LinearLayout hiddenOptionsLayout1,hiddenOptionsLayout2,hiddenOptionsLayout3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_post_property);

        getWindow().setStatusBarColor(getResources().getColor(R.color.white));
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initializeViews();
        setupClickListeners();
        setupImagePicker();

        for (int i = 1; i <= MAX_IMAGES; i++) {
            int imageId = getResources().getIdentifier("image_preview_" + i, "id", this.getPackageName());
            int closeId = getResources().getIdentifier("close_icon_" + i, "id", this.getPackageName());

            ImageView imageView = findViewById(imageId);
            ImageView closeIcon = findViewById(closeId);

            if (imageView != null && closeIcon != null) {
                imagePreviews.add(imageView);
                closeIcons.add(closeIcon);
                imageView.setVisibility(GONE);
                closeIcon.setVisibility(GONE);
            }
        }
    }

    private void initializeViews() {
        phoneNumberInput = findViewById(R.id.phone_number_value);
        cityInput = findViewById(R.id.input_city);
        localityInput = findViewById(R.id.input_locality);
        apartmentNameInput = findViewById(R.id.input_apartment_name);
        totalFloorsInput = findViewById(R.id.input_total_floors);
        expectedPriceInput = findViewById(R.id.input_expected_price);
        input_carpet_area = findViewById(R.id.input_carpet_area);
        descriptionInput = findViewById(R.id.input_description);
        areaUnitSpinner = findViewById(R.id.area_unit_spinner);
        flooringTypeSpinner = findViewById(R.id.flooring_type_spinner);
        categoryLayout = findViewById(R.id.category_layout);
        btnSelectImages = findViewById(R.id.btnSelectImages);
        back_arrow = findViewById(R.id.back_arrow);
        propertyKindLayout = findViewById(R.id.property_kind_layout);
        propertyTypeLayout = findViewById(R.id.property_type_layout);
        bhkTypeLayout = findViewById(R.id.bhk_type_layout);
        bedroomsLayout = findViewById(R.id.bedrooms_layout);
        bathroomsLayout = findViewById(R.id.bathrooms_layout);
        balconiesLayout = findViewById(R.id.balconies_layout);
        furnishingLayout = findViewById(R.id.furnishing_layout);
        availabilityLayout = findViewById(R.id.availability_layout);
        ownershipLayout = findViewById(R.id.ownership_layout);
        parkingLayout = findViewById(R.id.parking_layout);
        powerBackupLayout = findViewById(R.id.power_backup_layout);
        propertyFacingLayout = findViewById(R.id.property_facing_layout);
        saveAndSubmitButton = findViewById(R.id.save_and_submit_button);
        showMoreButton = findViewById(R.id.btn_show_more);
        kitchen_layout = findViewById(R.id.kitchen_layout);
        holes_layout = findViewById(R.id.holes_layout);
        hiddenOptionsLayout1 = findViewById(R.id.hidden_options_layout1);
        hiddenOptionsLayout2 = findViewById(R.id.hidden_options_layout2);
        hiddenOptionsLayout3 = findViewById(R.id.hidden_options_layout3);

    }
        private void setupClickListeners() {

        setupSelection(categoryLayout, new String[]{"Sell", "Rent", "Paying Guest"}, value -> selectedCategory = value);

        setupSelection(propertyKindLayout, new String[]{"Residential", "Commercial"}, value -> selectedPropertyKind = value);

        setupSelection(propertyTypeLayout, new String[]{"Apartment", "Flat","Room","Shop","P.G","Godown", "Independent / Builder Floor", "Plot / Land", "1 RK / Studio Apartment", "Office", "Farmhouse", "Independent House / Villa"}, value -> selectedPropertyType = value);

        setupSelection(bhkTypeLayout, new String[]{"0 BHK","1 BHK", "2 BHK", "3 BHK", "4 BHK", "5 BHK", "6 BHK", "7 BHK", "8 BHK", "9 BHK", "10 BHK"}, value -> selectedBhkType = value);

        setupSelection(bedroomsLayout, new String[]{"0","1", "2", "3", "4", "5", "6", "7", "8", "9", "10"}, value -> selectedBedrooms = value);

        setupSelection(bathroomsLayout, new String[]{"0","1", "2", "3", "4", "5", "6", "7", "8", "9", "10"}, value -> selectedBathrooms = value);

        setupSelection(balconiesLayout, new String[]{"0","1", "2", "3", "4", "5"}, value -> selectedBalconies = value);

        setupSelection(holes_layout, new String[]{"0","1", "2", "3", "4", "5"}, value -> selectedHole = value);

        setupSelection(kitchen_layout, new String[]{"0","1", "2", "3", "4", "5"}, value -> selectedKicthen = value);

        setupSelection(furnishingLayout, new String[]{"Unfurnished", "Semi-Furnished", "Furnished"}, value -> selectedFurnishing = value);

        setupSelection(availabilityLayout, new String[]{"Ready to move", "Under construction"}, value -> selectedAvailability = value);

        setupSelection(ownershipLayout, new String[]{"Freehold", "Leasehold", "Co-operative society", "Power of Attorney"}, value -> selectedOwnership = value);

        setupSelection(parkingLayout, new String[]{"Yes", "No"}, value -> selectedParking = value);

        setupSelection(powerBackupLayout, new String[]{"None", "Partial", "Full"}, value -> selectedPowerBackup = value);

        setupSelection(propertyFacingLayout, new String[]{"North", "South", "East", "West", "North-East", "North-West", "South-East", "South-West"}, value -> selectedPropertyFacing = value);

        btnSelectImages.setOnClickListener(v -> checkPermissionsAndOpenGallery());

        saveAndSubmitButton.setOnClickListener(v -> submitProperty());
        back_arrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
            showMoreButton.setOnClickListener(v -> {
                boolean isHidden = hiddenOptionsLayout1.getVisibility() == View.GONE || hiddenOptionsLayout2.getVisibility() == GONE || hiddenOptionsLayout3.getVisibility() == GONE  ;
                hiddenOptionsLayout1.setVisibility(isHidden ? View.VISIBLE : View.GONE);
                hiddenOptionsLayout2.setVisibility(isHidden ? View.VISIBLE : View.GONE);
                hiddenOptionsLayout3.setVisibility(isHidden ? View.VISIBLE : View.GONE);
                showMoreButton.setText(isHidden ? "Show Less" : "Show More");
            });
    }

    private void setupSelection(LinearLayout layout, String[] options, OnSelectionListener listener) {
        List<TextView> textViews = new ArrayList<>();

        for (int i = 0; i < layout.getChildCount(); i++) {
            View child = layout.getChildAt(i);
            if (child instanceof TextView) {
                textViews.add((TextView) child);
            } else if (child instanceof LinearLayout) {
                LinearLayout nestedLayout = (LinearLayout) child;
                for (int j = 0; j < nestedLayout.getChildCount(); j++) {
                    View nestedChild = nestedLayout.getChildAt(j);
                    if (nestedChild instanceof TextView) {
                        textViews.add((TextView) nestedChild);
                    }
                }
            }
        }

        for (TextView textView : textViews) {
            textView.setOnClickListener(v -> {

                for (TextView tv : textViews) {
                    tv.setBackgroundResource(R.drawable.option_button_unselected_bg);
                    tv.setTextColor(getResources().getColor(R.color.light_gray));
                }

                textView.setBackgroundResource(R.drawable.option_button_selected_bg);
                textView.setTextColor(getResources().getColor(android.R.color.white));
                listener.onSelected(textView.getText().toString());
            });
        }
    }

    private void setupImagePicker() {
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == this.RESULT_OK && result.getData() != null) {
                        if (result.getData().getClipData() != null) {
                            int count = result.getData().getClipData().getItemCount();
                            if (count + imageUris.size() > MAX_IMAGES) {
                                Toast.makeText(this, "Maximum 10 images allowed", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            for (int i = 0; i < count && imageUris.size() < MAX_IMAGES; i++) {
                                imageUris.add(result.getData().getClipData().getItemAt(i).getUri());
                            }
                        } else if (result.getData().getData() != null && imageUris.size() < MAX_IMAGES) {
                            imageUris.add(result.getData().getData());
                        }
                        updateImageViews();
                    }
                }
        );
    }

    private void submitProperty() {

        if (!validateInputs()) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!validateImages()) {
            Toast.makeText(this, "Please fill all required images", Toast.LENGTH_SHORT).show();
            return;
        }

        PropertyListRequest property = new PropertyListRequest();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            property.setFirebaseUid(user.getUid());
        }
        property.setCategory(selectedCategory);
        property.setPropertyKind(selectedPropertyKind);
        property.setPropertyType(selectedPropertyType);
        property.setPhoneNumber(phoneNumberInput.getText().toString());
        property.setCity(cityInput.getText().toString());
        property.setLocality(localityInput.getText().toString());
        property.setSocietyName(apartmentNameInput.getText().toString());
        property.setBhkType(selectedBhkType);
        property.setBedrooms(parseInteger(selectedBedrooms));
        property.setBathrooms(parseInteger(selectedBathrooms));
        property.setBalcony(parseInteger(selectedBalconies));
        property.setHole(parseInteger(selectedHole));
        property.setKitchen(parseInteger(selectedKicthen));
        property.setAreaUnit(input_carpet_area.getText().toString()+areaUnitSpinner.getSelectedItem().toString());
        property.setFurnishing(selectedFurnishing);
        property.setTotalFloor(parseInteger(totalFloorsInput.getText().toString()));
        property.setOwnership(selectedOwnership);
        property.setAvailabilityStatus(selectedAvailability);
        property.setExpectedPrice(parseDouble(expectedPriceInput.getText().toString()));
        property.setParking("Yes".equals(selectedParking));
        property.setPowerBackup(selectedPowerBackup);
        property.setPropertyFacing(selectedPropertyFacing);
        property.setFlooringType(flooringTypeSpinner.getSelectedItem().toString());
        property.setDescription(descriptionInput.getText().toString());
        String createdAt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(new Date());
        property.setCreateAt(createdAt);

        List<MultipartBody.Part> imageParts = new ArrayList<>();
        for (Uri uri : imageUris) {
            try {
                InputStream inputStream = this.getContentResolver().openInputStream(uri);
                if (inputStream != null) {
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        byteArrayOutputStream.write(buffer, 0, bytesRead);
                    }
                    inputStream.close();
                    byte[] imageBytes = byteArrayOutputStream.toByteArray();
                    byteArrayOutputStream.close();

                    RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), imageBytes);
                    String fileName = "image_" + System.currentTimeMillis() + ".jpg";
                    imageParts.add(MultipartBody.Part.createFormData("images", fileName, requestFile));
                } else {
                    Toast.makeText(this, "Failed to read image: " + uri, Toast.LENGTH_SHORT).show();
                    return;
                }
            } catch (IOException e) {
                Log.e(TAG, "Error reading image: " + e.getMessage());
                Toast.makeText(this, "Error reading image: " + e.getMessage(), Toast.LENGTH_LONG).show();
                return;
            }
        }

        RequestBody propertyBody = RequestBody.create(MediaType.parse("application/json"), new com.google.gson.Gson().toJson(property));

        ApiService apiService = RetrofitClient.getApiService();
        Call<PropertyListResponse> call = apiService.createProperty(propertyBody, imageParts);
        call.enqueue(new Callback<PropertyListResponse>() {
            @Override
            public void onResponse(Call<PropertyListResponse> call, Response<PropertyListResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(PostPropertyActivity.this, "Property posted successfully", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(PostPropertyActivity.this, "Failed to post property", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<PropertyListResponse> call, Throwable t) {
                Toast.makeText(PostPropertyActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean validateInputs() {
        return selectedCategory != null &&
                selectedPropertyKind != null &&
                selectedPropertyType != null &&
                !phoneNumberInput.getText().toString().isEmpty() &&
                !cityInput.getText().toString().isEmpty() &&
                !localityInput.getText().toString().isEmpty() &&
                selectedBhkType != null &&
                selectedBedrooms != null &&
                selectedBathrooms != null &&
                selectedBalconies != null &&
                areaUnitSpinner.getSelectedItem() != null &&
                selectedFurnishing != null &&
                !totalFloorsInput.getText().toString().isEmpty() &&
                !expectedPriceInput.getText().toString().isEmpty() &&
                selectedParking != null &&
                selectedPowerBackup != null &&
                selectedPropertyFacing != null &&
                flooringTypeSpinner.getSelectedItem() != null &&
                !descriptionInput.getText().toString().isEmpty() &&
                !imageUris.isEmpty();
    }

    private void checkPermissionsAndOpenGallery() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_MEDIA_IMAGES)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.READ_MEDIA_IMAGES}, REQUEST_CODE_PERMISSIONS);
            } else {
                openGallery();
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE_PERMISSIONS);
            } else {
                openGallery();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            } else {
                new AlertDialog.Builder(this)
                        .setTitle("Permission Required")
                        .setMessage("This app needs storage access to open the gallery.")
                        .setPositiveButton("Grant", (dialog, which) -> checkPermissionsAndOpenGallery())
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        imagePickerLauncher.launch(Intent.createChooser(intent, "Select Images"));
    }

    private void updateImageViews() {
        for (int i = 0; i < imagePreviews.size(); i++) {
            ImageView imageView = imagePreviews.get(i);
            ImageView closeIcon = closeIcons.get(i);

            if (i < imageUris.size()) {
                Uri imageUri = imageUris.get(i);
                Glide.with(this).load(imageUri).into(imageView);
                imageView.setVisibility(VISIBLE);
                closeIcon.setVisibility(VISIBLE);

                int finalIndex = i;
                closeIcon.setOnClickListener(v -> {
                    imageUris.remove(finalIndex);
                    updateImageViews();
                });
            } else {
                imageView.setVisibility(GONE);
                closeIcon.setVisibility(GONE);
                imageView.setImageDrawable(null);
                closeIcon.setOnClickListener(null);
            }
        }
    }

    private void loadImages(List<String> images) {
        int imageCount = Math.min(images.size(), Math.min(imagePreviews.size(), MAX_IMAGES));
        for (int i = 0; i < imageCount; i++) {
            String imageUrl = images.get(i).replace("localhost", "192.168.153.1");
            Glide.with(this).load(imageUrl).into(imagePreviews.get(i));
            imagePreviews.get(i).setVisibility(VISIBLE);
            closeIcons.get(i).setVisibility(VISIBLE);
        }
        for (int i = imageCount; i < Math.min(imagePreviews.size(), MAX_IMAGES); i++) {
            imagePreviews.get(i).setVisibility(GONE);
            closeIcons.get(i).setVisibility(GONE);
        }
    }

    private boolean validateImages() {
        if (imageUris.isEmpty() || imageUris.size() < MIN_IMAGES) {
            Toast.makeText(this, "Please select at least 1 image", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (imageUris.size() > MAX_IMAGES) {
            Toast.makeText(this, "Maximum 10 images allowed", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
    private Integer parseInteger(String value) {
        try {
            return Integer.parseInt(value.replace("+", ""));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Double parseDouble(String value) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private interface OnSelectionListener {
        void onSelected(String value);
    }
}
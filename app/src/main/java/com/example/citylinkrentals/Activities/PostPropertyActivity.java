package com.example.citylinkrentals.Activities;

import static android.content.ContentValues.TAG;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.citylinkrentals.Adapter.PropertyImagesAdapter;
import com.example.citylinkrentals.R;
import com.example.citylinkrentals.model.Property;
import com.example.citylinkrentals.model.PropertyListRequest;
import com.example.citylinkrentals.model.PropertyListResponse;
import com.example.citylinkrentals.model.ResponseDTO;
import com.example.citylinkrentals.network.ApiService;
import com.example.citylinkrentals.network.RetrofitClient;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.gson.Gson;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PostPropertyActivity extends AppCompatActivity implements PropertyImagesAdapter.OnImageActionListener {

    private static final int MAX_IMAGES = 10;
    private static final int MIN_IMAGES = 1;
    private static final int REQUEST_CODE_PERMISSIONS = 100;
    private static final int EDIT_PROPERTY_REQUEST_CODE = 1001;
    private static final int PICK_IMAGES_REQUEST_CODE = 2001;

    // UI Components
    private TextInputEditText phoneNumberInput, cityInput, localityInput, apartmentNameInput;
    private TextInputEditText bedroomsInput, bathroomsInput, balconiesInput, kitchenInput, hallInput;
    private TextInputEditText carpetAreaInput, expectedPriceInput, totalFloorsInput, descriptionInput;
    private AutoCompleteTextView areaUnitSpinner, flooringTypeSpinner;
    private ChipGroup categoryChipGroup, propertyKindChipGroup, propertyTypeChipGroup;
    private ChipGroup bhkTypeChipGroup, furnishingChipGroup, parkingChipGroup, availabilityChipGroup;
    private ChipGroup propertyFacingChipGroup, ownershipChipGroup, powerBackupChipGroup;
    private ChipGroup hiddenPropertyTypes, pgTargetChipGroup; // Added P.G target chip group
    private ImageView backArrow;
    private LinearLayout btnSelectImages, helpWhatsappLayout, pgTargetSection; // Added P.G target section
    private MaterialButton saveAndSubmitButton;
    private Chip chipMoreOptions;
    private RecyclerView imagesRecyclerView;
    private Toolbar toolbar;
    private TextView toolbarTitle;

    private List<Uri> newImageUris = new ArrayList<>();
    private List<String> existingImageUrls = new ArrayList<>();
    private List<String> imagesToRemove = new ArrayList<>();
    private PropertyImagesAdapter imagePreviewAdapter;
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    private boolean isEditMode = false;
    private Property propertyToEdit;

    private String selectedCategory, selectedPropertyKind, selectedPropertyType;
    private String selectedBhkType, selectedFurnishing, selectedParking, selectedAvailability;
    private String selectedPropertyFacing, selectedOwnership, selectedPowerBackup;
    private String selectedPgTarget;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_post_property);

        getWindow().setStatusBarColor(getResources().getColor(R.color.main_background_color));
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initializeViews();
        setupToolbar();
        setupDropdowns();
        setupChipGroups();
        setupClickListeners();
        setupImagePicker();
        setupRecyclerView();
        checkEditMode();
    }

    private void setupToolbar() {
        toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(false);
            actionBar.setDisplayShowTitleEnabled(false);
        }

        toolbarTitle = findViewById(R.id.toolbar_title);
        if (toolbarTitle != null) {
            toolbarTitle.setText("Post Your Property");
        }
    }

    private void checkEditMode() {
        isEditMode = getIntent().getBooleanExtra("IS_EDIT_MODE", false);
        if (isEditMode) {
            propertyToEdit = (Property) getIntent().getSerializableExtra("PROPERTY_TO_EDIT");
            if (propertyToEdit != null) {
                newImageUris.clear();
                imagesToRemove.clear();
                setupEditMode();
            }
        }
    }

    private void setupEditMode() {
        if (toolbarTitle != null) {
            toolbarTitle.setText("Edit Property");
        }
        saveAndSubmitButton.setText("Update Property");

        populateFieldsFromProperty(propertyToEdit);

        // FIXED: Clear all lists first before populating
        existingImageUrls.clear();
        newImageUris.clear();
        imagesToRemove.clear();

        // FIXED: Use LinkedHashSet to remove duplicates while preserving order
        if (propertyToEdit.getImagePaths() != null && !propertyToEdit.getImagePaths().isEmpty()) {
            // Create a new list from LinkedHashSet to ensure no duplicates
            existingImageUrls = new ArrayList<>(new LinkedHashSet<>(propertyToEdit.getImagePaths()));
            showImagePreview();
        }
    }


    private void populateFieldsFromProperty(Property property) {
        // Populate category
        if (property.getCategory() != null) {
            Chip categoryChip = findChipByText(property.getCategory(), categoryChipGroup);
            if (categoryChip != null) {
                categoryChipGroup.check(categoryChip.getId());
            }
        }

        if (property.getPropertyKind() != null) {
            Chip kindChip = findChipByText(property.getPropertyKind(), propertyKindChipGroup);
            if (kindChip != null) {
                propertyKindChipGroup.check(kindChip.getId());
            }
        }

        // Populate property type
        if (property.getPropertyType() != null) {
            Chip typeChip = findChipByText(property.getPropertyType(), propertyTypeChipGroup);
            if (typeChip != null) {
                propertyTypeChipGroup.check(typeChip.getId());
            } else {
                Chip hiddenTypeChip = findChipByText(property.getPropertyType(), hiddenPropertyTypes);
                if (hiddenTypeChip != null) {
                    hiddenPropertyTypes.check(hiddenTypeChip.getId());
                    chipMoreOptions.performClick();
                }
            }
        }

        phoneNumberInput.setText(property.getPhoneNumber());
        cityInput.setText(property.getCity());
        localityInput.setText(property.getLocality());
        apartmentNameInput.setText(property.getSocietyName());

        if (property.getBhkType() != null) {
            Chip bhkChip = findChipByText(property.getBhkType(), bhkTypeChipGroup);
            if (bhkChip != null) {
                bhkTypeChipGroup.check(bhkChip.getId());
            }
        }

        bedroomsInput.setText(String.valueOf(property.getBedrooms()));
        bathroomsInput.setText(String.valueOf(property.getBathrooms()));
        balconiesInput.setText(String.valueOf(property.getBalcony()));
        kitchenInput.setText(String.valueOf(property.getKitchen()));
        hallInput.setText(String.valueOf(property.getHole()));

        if (property.getAreaUnit() != null) {
            String[] areaParts = property.getAreaUnit().split(" ", 2);
            if (areaParts.length >= 1) {
                carpetAreaInput.setText(areaParts[0]);
            }
            if (areaParts.length >= 2) {
                areaUnitSpinner.setText(areaParts[1], false);
            }
        }

        expectedPriceInput.setText(String.valueOf(property.getExpectedPrice()));

        if (property.getFurnishing() != null) {
            Chip furnishingChip = findChipByText(property.getFurnishing(), furnishingChipGroup);
            if (furnishingChip != null) {
                furnishingChipGroup.check(furnishingChip.getId());
            }
        }

        if (property.getParking() != null) {
            Chip parkingChip = findChipByText(property.getParking() ? "Yes" : "No", parkingChipGroup);
            if (parkingChip != null) {
                parkingChipGroup.check(parkingChip.getId());
            }
        }

        if (property.getPowerBackup() != null) {
            Chip powerChip = findChipByText(property.getPowerBackup(), powerBackupChipGroup);
            if (powerChip != null) {
                powerBackupChipGroup.check(powerChip.getId());
            }
        }

        if (property.getPropertyFacing() != null) {
            Chip facingChip = findChipByText(property.getPropertyFacing(), propertyFacingChipGroup);
            if (facingChip != null) {
                propertyFacingChipGroup.check(facingChip.getId());
            }
        }

        if (property.getOwnership() != null) {
            Chip ownershipChip = findChipByText(property.getOwnership(), ownershipChipGroup);
            if (ownershipChip != null) {
                ownershipChipGroup.check(ownershipChip.getId());
            }
        }

        if (property.getAvailabilityStatus() != null) {
            Chip availabilityChip = findChipByText(property.getAvailabilityStatus(), availabilityChipGroup);
            if (availabilityChip != null) {
                availabilityChipGroup.check(availabilityChip.getId());
            }
        }

        // NEW: Populate P.G target if property is P.G
        if (property.getPgTarget() != null && !property.getPgTarget().isEmpty()) {
            Chip pgTargetChip = findChipByText(property.getPgTarget(), pgTargetChipGroup);
            if (pgTargetChip != null) {
                pgTargetChipGroup.check(pgTargetChip.getId());
            }
        }

        totalFloorsInput.setText(String.valueOf(property.getTotalFloor()));

        if (property.getFlooringType() != null) {
            flooringTypeSpinner.setText(property.getFlooringType(), false);
        }

        descriptionInput.setText(property.getDescription());
    }

    private Chip findChipByText(String text, ChipGroup chipGroup) {
        for (int i = 0; i < chipGroup.getChildCount(); i++) {
            Chip chip = (Chip) chipGroup.getChildAt(i);
            if (chip.getText().toString().equalsIgnoreCase(text)) {
                return chip;
            }
        }
        return null;
    }

    private void initializeViews() {
        // Input fields
        phoneNumberInput = findViewById(R.id.phone_number_value);
        cityInput = findViewById(R.id.input_city);
        localityInput = findViewById(R.id.input_locality);
        apartmentNameInput = findViewById(R.id.input_apartment_name);
        bedroomsInput = findViewById(R.id.bedrooms_input);
        bathroomsInput = findViewById(R.id.bathrooms_input);
        balconiesInput = findViewById(R.id.balconies_input);
        kitchenInput = findViewById(R.id.kitchen_input);
        hallInput = findViewById(R.id.hall_input);
        carpetAreaInput = findViewById(R.id.input_carpet_area);
        expectedPriceInput = findViewById(R.id.input_expected_price);
        totalFloorsInput = findViewById(R.id.input_total_floors);
        descriptionInput = findViewById(R.id.input_description);

        // Dropdowns
        areaUnitSpinner = findViewById(R.id.area_unit_spinner);
        flooringTypeSpinner = findViewById(R.id.flooring_type_spinner);

        // Chip Groups
        categoryChipGroup = findViewById(R.id.category_chip_group);
        propertyKindChipGroup = findViewById(R.id.property_kind_chip_group);
        propertyTypeChipGroup = findViewById(R.id.property_type_chip_group);
        bhkTypeChipGroup = findViewById(R.id.bhk_type_chip_group);
        furnishingChipGroup = findViewById(R.id.furnishing_chip_group);
        parkingChipGroup = findViewById(R.id.parking_chip_group);
        availabilityChipGroup = findViewById(R.id.availability_chip_group);
        propertyFacingChipGroup = findViewById(R.id.property_facing_chip_group);
        ownershipChipGroup = findViewById(R.id.ownership_chip_group);
        powerBackupChipGroup = findViewById(R.id.power_backup_chip_group);
        hiddenPropertyTypes = findViewById(R.id.hidden_property_types);

        // NEW: P.G target chip group
        pgTargetChipGroup = findViewById(R.id.pg_target_chip_group);

        // UI Elements
        backArrow = findViewById(R.id.back_arrow);
        btnSelectImages = findViewById(R.id.btnSelectImages);
        helpWhatsappLayout = findViewById(R.id.help_whatsapp_layout);
        saveAndSubmitButton = findViewById(R.id.save_and_submit_button);
        chipMoreOptions = findViewById(R.id.chip_more_options);
        imagesRecyclerView = findViewById(R.id.images_recycler_view);

        // NEW: P.G target section
        pgTargetSection = findViewById(R.id.pg_target_section);
    }

    private void setupDropdowns() {
        // Area unit dropdown
        String[] areaUnits = {"Sq.Ft", "Sq.Meter", "Sq.Yard", "Acre", "Bigha", "Biswa"};
        ArrayAdapter<String> areaAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, areaUnits);
        areaUnitSpinner.setAdapter(areaAdapter);

        // Flooring type dropdown
        String[] flooringTypes = {"Marble", "Tiles", "Wooden", "Mosaic", "Granite",
                "Concrete", "IPS", "Ceramic", "POP", "Others"};
        ArrayAdapter<String> flooringAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, flooringTypes);
        flooringTypeSpinner.setAdapter(flooringAdapter);
    }

    private void setupChipGroups() {
        // Category selection
        categoryChipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (!checkedIds.isEmpty()) {
                Chip selectedChip = findViewById(checkedIds.get(0));
                selectedCategory = selectedChip.getText().toString();
            }
        });

        // Property kind selection
        propertyKindChipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (!checkedIds.isEmpty()) {
                Chip selectedChip = findViewById(checkedIds.get(0));
                selectedPropertyKind = selectedChip.getText().toString();
            }
        });

        // Property type selection (both visible and hidden)
        ChipGroup.OnCheckedStateChangeListener propertyTypeListener = (group, checkedIds) -> {
            if (!checkedIds.isEmpty()) {
                Chip selectedChip = findViewById(checkedIds.get(0));
                selectedPropertyType = selectedChip.getText().toString();

                // Clear selection in other property type group
                if (group == propertyTypeChipGroup) {
                    hiddenPropertyTypes.clearCheck();
                } else {
                    propertyTypeChipGroup.clearCheck();
                }

                // Update field visibility based on property type
                updateFieldVisibilityBasedOnPropertyType(selectedPropertyType);
            }
        };

        propertyTypeChipGroup.setOnCheckedStateChangeListener(propertyTypeListener);
        hiddenPropertyTypes.setOnCheckedStateChangeListener(propertyTypeListener);

        // BHK type selection
        bhkTypeChipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (!checkedIds.isEmpty()) {
                Chip selectedChip = findViewById(checkedIds.get(0));
                selectedBhkType = selectedChip.getText().toString();
            }
        });

        // Furnishing selection
        furnishingChipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (!checkedIds.isEmpty()) {
                Chip selectedChip = findViewById(checkedIds.get(0));
                selectedFurnishing = selectedChip.getText().toString();
            }
        });

        // Parking selection
        parkingChipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (!checkedIds.isEmpty()) {
                Chip selectedChip = findViewById(checkedIds.get(0));
                selectedParking = selectedChip.getText().toString();
            }
        });

        // Availability selection
        availabilityChipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (!checkedIds.isEmpty()) {
                Chip selectedChip = findViewById(checkedIds.get(0));
                selectedAvailability = selectedChip.getText().toString();
            }
        });

        // Property Facing selection
        propertyFacingChipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (!checkedIds.isEmpty()) {
                Chip selectedChip = findViewById(checkedIds.get(0));
                selectedPropertyFacing = selectedChip.getText().toString();
            }
        });

        // Ownership selection
        ownershipChipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (!checkedIds.isEmpty()) {
                Chip selectedChip = findViewById(checkedIds.get(0));
                selectedOwnership = selectedChip.getText().toString();
            }
        });

        // Power Backup selection
        powerBackupChipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (!checkedIds.isEmpty()) {
                Chip selectedChip = findViewById(checkedIds.get(0));
                selectedPowerBackup = selectedChip.getText().toString();
            }
        });

        // NEW: P.G Target selection
        pgTargetChipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (!checkedIds.isEmpty()) {
                Chip selectedChip = findViewById(checkedIds.get(0));
                selectedPgTarget = selectedChip.getText().toString();
            }
        });
    }

    // UPDATED: Update field visibility based on property type
    private void updateFieldVisibilityBasedOnPropertyType(String propertyType) {
        // Find UI elements
        View bhkSection = findViewById(R.id.bhk_section);
        LinearLayout roomConfigLayout = findViewById(R.id.room_config_layout);
        View bedroomsSection = findViewById(R.id.bedrooms_section);
        View bathroomsSection = findViewById(R.id.bathrooms_section);
        View balconiesSection = findViewById(R.id.balconies_section);
        View kitchenSection = findViewById(R.id.kitchen_section);
        View hallSection = findViewById(R.id.hall_section);

        // Reset all to visible first
        if (bhkSection != null) bhkSection.setVisibility(View.VISIBLE);
        if (roomConfigLayout != null) roomConfigLayout.setVisibility(View.VISIBLE);
        if (bedroomsSection != null) bedroomsSection.setVisibility(View.VISIBLE);
        if (bathroomsSection != null) bathroomsSection.setVisibility(View.VISIBLE);
        if (balconiesSection != null) balconiesSection.setVisibility(View.VISIBLE);
        if (kitchenSection != null) kitchenSection.setVisibility(View.VISIBLE);
        if (hallSection != null) hallSection.setVisibility(View.VISIBLE);

        if (pgTargetSection != null) {
            if (propertyType.toLowerCase().equals("p.g") || propertyType.toLowerCase().equals("paying guest")) {
                pgTargetSection.setVisibility(View.VISIBLE);
            } else {
                pgTargetSection.setVisibility(View.GONE);

                if (pgTargetChipGroup != null) {
                    pgTargetChipGroup.clearCheck();
                }
                selectedPgTarget = null;
            }
        }

        switch (propertyType.toLowerCase()) {
            case "office":
                if (bhkSection != null) bhkSection.setVisibility(View.GONE);
                if (kitchenSection != null) kitchenSection.setVisibility(View.GONE);
                if (balconiesSection != null) balconiesSection.setVisibility(View.GONE);
                updateFieldLabels("office");
                break;

            case "shop":
                if (bhkSection != null) bhkSection.setVisibility(View.GONE);
                if (bedroomsSection != null) bedroomsSection.setVisibility(View.GONE);
                if (kitchenSection != null) kitchenSection.setVisibility(View.GONE);
                if (balconiesSection != null) balconiesSection.setVisibility(View.GONE);
                updateFieldLabels("shop");
                break;

            case "godown":
                if (bhkSection != null) bhkSection.setVisibility(View.GONE);
                if (bedroomsSection != null) bedroomsSection.setVisibility(View.GONE);
                if (kitchenSection != null) kitchenSection.setVisibility(View.GONE);
                if (hallSection != null) hallSection.setVisibility(View.GONE);
                if (balconiesSection != null) balconiesSection.setVisibility(View.GONE);
                updateFieldLabels("godown");
                break;

            case "plot / land":
            case "plot":
            case "land":
                if (bhkSection != null) bhkSection.setVisibility(View.GONE);
                if (roomConfigLayout != null) roomConfigLayout.setVisibility(View.GONE);
                updateFieldLabels("plot");
                break;

            case "1 rk studio":
            case "studio":
                if (bhkSection != null) bhkSection.setVisibility(View.GONE);
                updateFieldLabels("studio");
                break;

            case "p.g":
            case "paying guest":
                updateFieldLabels("pg");
                break;

            default:
                updateFieldLabels("residential");
                break;
        }
    }

    // Update field labels based on property type
    private void updateFieldLabels(String propertyType) {
        TextView bedroomsLabel = findViewById(R.id.bedrooms_label);
        TextView hallLabel = findViewById(R.id.hall_label);

        if (bedroomsLabel == null || hallLabel == null) return;

        switch (propertyType) {
            case "office":
                bedroomsLabel.setText("Cabins");
                hallLabel.setText("Conference Room");
                break;

            case "shop":
                bedroomsLabel.setText("Rooms");
                hallLabel.setText("Display Area");
                break;

            case "godown":
                bedroomsLabel.setText("Sections");
                hallLabel.setText("Loading Area");
                break;

            case "pg":
                bedroomsLabel.setText("Rooms");
                hallLabel.setText("Common Area");
                break;

            default:
                bedroomsLabel.setText("Bedrooms");
                hallLabel.setText("Hall");
                break;
        }
    }

    private void setupClickListeners() {
        backArrow.setOnClickListener(v -> finish());

        helpWhatsappLayout.setOnClickListener(v -> {
            String phoneNumber = "+1234567890";
            String message = "Hi, I need help with posting my property";
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("https://wa.me/" + phoneNumber + "?text=" + message));
            startActivity(intent);
        });

        chipMoreOptions.setOnClickListener(v -> {
            boolean isHidden = hiddenPropertyTypes.getVisibility() == GONE;
            hiddenPropertyTypes.setVisibility(isHidden ? VISIBLE : GONE);
            chipMoreOptions.setText(isHidden ? "Less Options" : "More Options");
            chipMoreOptions.setChipIcon(getResources().getDrawable(
                    isHidden ? R.drawable.keyboard_arrow_up_24px : R.drawable.keyboard_arrow_down_24px));
        });

        btnSelectImages.setOnClickListener(v -> checkPermissionsAndOpenGallery());

        saveAndSubmitButton.setOnClickListener(v -> submitProperty());
    }

    private void setupImagePicker() {
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {

                        // Handle multiple images selection
                        if (result.getData().getClipData() != null) {
                            int count = result.getData().getClipData().getItemCount();
                            for (int i = 0; i < count; i++) {
                                Uri imageUri = result.getData().getClipData().getItemAt(i).getUri();
                                // Check for duplicates AND max limit
                                if (!newImageUris.contains(imageUri) && getTotalImageCount() < MAX_IMAGES) {
                                    newImageUris.add(imageUri);
                                }
                            }
                        }
                        // Handle single image selection
                        else if (result.getData().getData() != null) {
                            Uri imageUri = result.getData().getData();
                            // Check for duplicates AND max limit
                            if (!newImageUris.contains(imageUri) && getTotalImageCount() < MAX_IMAGES) {
                                newImageUris.add(imageUri);
                            }
                        }

                        // Show message if limit exceeded
                        if (getTotalImageCount() > MAX_IMAGES) {
                            Toast.makeText(this, "Maximum " + MAX_IMAGES + " images allowed", Toast.LENGTH_SHORT).show();
                        }

                        // Update the preview
                        showImagePreview();
                    }
                }
        );
    }

    private int getTotalImageCount() {
        // Count only images that are NOT marked for removal
        int existingCount = 0;
        for (String url : existingImageUrls) {
            if (!imagesToRemove.contains(url)) {
                existingCount++;
            }
        }
        return existingCount + newImageUris.size();
    }
    private void setupRecyclerView() {
        imagePreviewAdapter = new PropertyImagesAdapter(new ArrayList<>(), true, this);
        imagesRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        imagesRecyclerView.setAdapter(imagePreviewAdapter);
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
                        .setMessage("This app needs storage access to select images.")
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

    private void showImagePreview() {
        List<Object> allImages = new ArrayList<>();

        if (isEditMode) {
            // Only add existing images that are NOT marked for removal
            for (String imageUrl : existingImageUrls) {
                if (!imagesToRemove.contains(imageUrl)) {
                    allImages.add(imageUrl);
                }
            }
        } else {
            // In create mode, just add all existing images
            allImages.addAll(existingImageUrls);
        }

        // Add new images (no duplicates check needed as we prevent duplicates during selection)
        allImages.addAll(newImageUris);

        // Update RecyclerView visibility and adapter
        if (!allImages.isEmpty()) {
            imagesRecyclerView.setVisibility(VISIBLE);
            imagePreviewAdapter.updateImages(allImages);
        } else {
            imagesRecyclerView.setVisibility(GONE);
        }
    }

    @Override
    public void onImageRemoved(Object image, int position) {
        if (image instanceof String) {
            // Existing image URL - remove from display and mark for deletion
            String imageUrl = (String) image;

            // Only add to imagesToRemove if it's not already there
            if (!imagesToRemove.contains(imageUrl)) {
                imagesToRemove.add(imageUrl);
            }

        } else if (image instanceof Uri) {

            Uri imageUri = (Uri) image;
            newImageUris.remove(imageUri);
        }

        showImagePreview();
    }

    private void submitProperty() {
        if (!validateInputs()) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!validateImages()) {
            return;
        }

        saveAndSubmitButton.setEnabled(false);
        saveAndSubmitButton.setText(isEditMode ? "Updating..." : "Submitting...");

        PropertyListRequest property = createPropertyRequest();

        if (isEditMode) {
            updateProperty(property);
        } else {
            createProperty(property);
        }
    }

    private PropertyListRequest createPropertyRequest() {
        PropertyListRequest property = new PropertyListRequest();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            property.setFirebaseUid(user.getUid());
        }

        property.setCategory(selectedCategory);
        property.setPropertyKind(selectedPropertyKind);
        property.setPropertyType(selectedPropertyType);
        property.setPhoneNumber(phoneNumberInput.getText().toString().trim());
        property.setCity(cityInput.getText().toString().trim());
        property.setLocality(localityInput.getText().toString().trim());
        property.setSocietyName(apartmentNameInput.getText().toString().trim());
        property.setBhkType(selectedBhkType);
        property.setBedrooms(parseInteger(bedroomsInput.getText().toString()));
        property.setBathrooms(parseInteger(bathroomsInput.getText().toString()));
        property.setBalcony(parseInteger(balconiesInput.getText().toString()));
        property.setKitchen(parseInteger(kitchenInput.getText().toString()));
        property.setHole(parseInteger(hallInput.getText().toString()));

        String area = carpetAreaInput.getText().toString().trim();
        String unit = areaUnitSpinner.getText().toString().trim();
        property.setAreaUnit(area + " " + unit);
        property.setPropertyStatus("PENDING");

        property.setFurnishing(selectedFurnishing);
        property.setTotalFloor(parseInteger(totalFloorsInput.getText().toString()));
        property.setAvailabilityStatus(selectedAvailability);
        property.setExpectedPrice(parseDouble(expectedPriceInput.getText().toString()));
        property.setParking("Yes".equals(selectedParking));
        property.setFlooringType(flooringTypeSpinner.getText().toString().trim());
        property.setDescription(descriptionInput.getText().toString().trim());
        property.setPropertyFacing(selectedPropertyFacing);
        property.setOwnership(selectedOwnership);
        property.setPowerBackup(selectedPowerBackup);

        property.setPgTarget(selectedPgTarget);

        String createdAt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(new Date());
        property.setCreateAt(createdAt);

        if (isEditMode && !imagesToRemove.isEmpty()) {
            property.setImagesToRemove(imagesToRemove);
        }

        return property;
    }

    private void createProperty(PropertyListRequest property) {
        List<MultipartBody.Part> imageParts = new ArrayList<>();
        for (Uri uri : newImageUris) {
            try {
                InputStream inputStream = getContentResolver().openInputStream(uri);
                if (inputStream != null) {
                    byte[] imageBytes = getBytes(inputStream);
                    RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), imageBytes);
                    String fileName = getFileName(uri);
                    imageParts.add(MultipartBody.Part.createFormData("images", fileName, requestFile));
                }
            } catch (IOException e) {
                Log.e(TAG, "Error reading image: " + e.getMessage());
            }
        }

        RequestBody propertyBody = RequestBody.create(MediaType.parse("application/json"), new Gson().toJson(property));

        ApiService apiService = RetrofitClient.getApiService();
        Call<PropertyListResponse> call = apiService.createProperty(propertyBody, imageParts);
        call.enqueue(new Callback<PropertyListResponse>() {
            @Override
            public void onResponse(Call<PropertyListResponse> call, Response<PropertyListResponse> response) {
                resetSubmitButton();
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(PostPropertyActivity.this, "Property posted successfully!", Toast.LENGTH_LONG).show();
                    setResult(RESULT_OK);
                    finish();
                } else {
                    String errorMessage = "Failed to post property";
                    if (response.errorBody() != null) {
                        try {
                            errorMessage += ": " + response.errorBody().string();
                        } catch (IOException e) {
                            Log.e(TAG, "Error reading error body: " + e.getMessage());
                        }
                    }
                    Toast.makeText(PostPropertyActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<PropertyListResponse> call, Throwable t) {
                resetSubmitButton();
                Log.e(TAG, "Network error: " + t.getMessage());
                Toast.makeText(PostPropertyActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void updateProperty(PropertyListRequest property) {
        List<MultipartBody.Part> imageParts = new ArrayList<>();

        // Only add NEW images to the multipart request
        for (Uri uri : newImageUris) {
            try {
                InputStream inputStream = getContentResolver().openInputStream(uri);
                if (inputStream != null) {
                    byte[] imageBytes = getBytes(inputStream);
                    RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), imageBytes);
                    String fileName = getFileName(uri);
                    imageParts.add(MultipartBody.Part.createFormData("images", fileName, requestFile));
                }
            } catch (IOException e) {
                Log.e(TAG, "Error reading image: " + e.getMessage());
            }
        }

        RequestBody propertyBody = RequestBody.create(MediaType.parse("application/json"), new Gson().toJson(property));

        ApiService apiService = RetrofitClient.getApiService();
        Call<ResponseDTO> call = apiService.updateProperty(propertyToEdit.getId(), propertyBody, imageParts);
        call.enqueue(new Callback<ResponseDTO>() {
            @Override
            public void onResponse(Call<ResponseDTO> call, Response<ResponseDTO> response) {
                resetSubmitButton();
                if (response.isSuccessful() && response.body() != null) {
                    ResponseDTO responseDTO = response.body();
                    if (responseDTO.getStatusCode() == 0) {
                        Toast.makeText(PostPropertyActivity.this, "Property updated successfully!", Toast.LENGTH_LONG).show();

                        // FIXED: Clear all lists after successful update
                        newImageUris.clear();
                        existingImageUrls.clear();
                        imagesToRemove.clear();

                        setResult(RESULT_OK);
                        finish();
                    } else {
                        Toast.makeText(PostPropertyActivity.this, responseDTO.getStatusMessage(), Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(PostPropertyActivity.this, "Failed to update property", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseDTO> call, Throwable t) {
                resetSubmitButton();
                Log.e(TAG, "Network error: " + t.getMessage());
                Toast.makeText(PostPropertyActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void resetSubmitButton() {
        saveAndSubmitButton.setEnabled(true);
        saveAndSubmitButton.setText(isEditMode ? "Update Property" : "Save and Submit");
    }

    // UPDATED: Validation with P.G target validation
    private boolean validateInputs() {
        if (selectedCategory == null || selectedCategory.isEmpty()) {
            Toast.makeText(this, "Please select a category", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (selectedPropertyKind == null || selectedPropertyKind.isEmpty()) {
            Toast.makeText(this, "Please select property kind", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (selectedPropertyType == null || selectedPropertyType.isEmpty()) {
            Toast.makeText(this, "Please select property type", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (phoneNumberInput.getText().toString().trim().isEmpty()) {
            phoneNumberInput.setError("Phone number is required");
            return false;
        }

        if (cityInput.getText().toString().trim().isEmpty()) {
            cityInput.setError("City is required");
            return false;
        }

        if (localityInput.getText().toString().trim().isEmpty()) {
            localityInput.setError("Locality is required");
            return false;
        }

        if (carpetAreaInput.getText().toString().trim().isEmpty()) {
            carpetAreaInput.setError("Carpet area is required");
            return false;
        }

        if (areaUnitSpinner.getText().toString().trim().isEmpty()) {
            areaUnitSpinner.setError("Please select area unit");
            return false;
        }

        if (expectedPriceInput.getText().toString().trim().isEmpty()) {
            expectedPriceInput.setError("Expected price is required");
            return false;
        }

        String propertyType = selectedPropertyType.toLowerCase();

        // NEW: P.G target validation
        if (propertyType.equals("p.g") || propertyType.equals("paying guest")) {
            if (selectedPgTarget == null || selectedPgTarget.isEmpty()) {
                Toast.makeText(this, "Please select P.G target audience", Toast.LENGTH_SHORT).show();
                return false;
            }
        }

        if (!propertyType.equals("plot / land") && !propertyType.equals("plot") && !propertyType.equals("land")) {
            if (selectedFurnishing == null || selectedFurnishing.isEmpty()) {
                Toast.makeText(this, "Please select furnishing type", Toast.LENGTH_SHORT).show();
                return false;
            }
        }

        if (!propertyType.equals("plot / land") && !propertyType.equals("plot") && !propertyType.equals("land")) {
            if (selectedParking == null || selectedParking.isEmpty()) {
                Toast.makeText(this, "Please select parking availability", Toast.LENGTH_SHORT).show();
                return false;
            }
        }

        if (selectedAvailability == null || selectedAvailability.isEmpty()) {
            Toast.makeText(this, "Please select availability status", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!propertyType.equals("plot / land") && !propertyType.equals("plot") && !propertyType.equals("land")) {
            if (selectedPropertyFacing == null || selectedPropertyFacing.isEmpty()) {
                Toast.makeText(this, "Please select property facing", Toast.LENGTH_SHORT).show();
                return false;
            }
        }

        if (selectedOwnership == null || selectedOwnership.isEmpty()) {
            Toast.makeText(this, "Please select ownership type", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!propertyType.equals("plot / land") && !propertyType.equals("plot") && !propertyType.equals("land")) {
            if (selectedPowerBackup == null || selectedPowerBackup.isEmpty()) {
                Toast.makeText(this, "Please select power backup option", Toast.LENGTH_SHORT).show();
                return false;
            }
        }

        if (propertyType.equals("apartment") || propertyType.equals("flat") ||
                propertyType.equals("room") || propertyType.equals("independent house") ||
                propertyType.equals("p.g") || propertyType.equals("paying guest")) {

            View bhkSection = findViewById(R.id.bhk_section);
            if (bhkSection != null && bhkSection.getVisibility() == View.VISIBLE) {
                if (selectedBhkType == null || selectedBhkType.isEmpty()) {
                    Toast.makeText(this, "Please select BHK type", Toast.LENGTH_SHORT).show();
                    return false;
                }
            }
        }

        return true;
    }

    private boolean validateImages() {
        int totalImages = getTotalImageCount();

        if (totalImages < MIN_IMAGES) {
            Toast.makeText(this, "Please select at least " + MIN_IMAGES + " image", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (totalImages > MAX_IMAGES) {
            Toast.makeText(this, "Maximum " + MAX_IMAGES + " images allowed", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private Integer parseInteger(String value) {
        if (value == null || value.trim().isEmpty()) {
            return 0;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private Double parseDouble(String value) {
        if (value == null || value.trim().isEmpty()) {
            return 0.0;
        }
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }

    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            ContentResolver resolver = getContentResolver();
            Cursor cursor = resolver.query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        if (isEditMode) {
            new AlertDialog.Builder(this)
                    .setTitle("Discard Changes?")
                    .setMessage("Are you sure you want to discard all changes?")
                    .setPositiveButton("Discard", (dialog, which) -> super.onBackPressed())
                    .setNegativeButton("Continue Editing", null)
                    .show();
        } else {
            super.onBackPressed();
        }
    }
}

package com.example.citylinkrentals.Activities;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.citylinkrentals.Adapter.PropertyAdapter;
import com.example.citylinkrentals.R;
import com.example.citylinkrentals.model.Property;
import com.example.citylinkrentals.model.ResponseDTO;
import com.example.citylinkrentals.network.ApiService;
import com.example.citylinkrentals.network.RetrofitClient;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchPropertyActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private PropertyAdapter propertyAdapter;
    private List<Property> originalPropertyList = new ArrayList<>();
    private List<Property> filteredPropertyList = new ArrayList<>();
    private EditText searchBar;
    private MaterialButton backIcon, sortFilterButton, btnClearFilters;
    private TextView resultsCount;
    private ChipGroup chipGroup;
    private Chip chipAll, chipResidential, chipCommercial, chipRoom, chipShop, chipPg, chipGodown;
    private ShimmerFrameLayout shimmerLayout;
    private View emptyStateLayout;
    private SwipeRefreshLayout swipeRefreshLayout;

    private String currentFilter = "All";
    private String currentSearchQuery = "";

    // Intent extras
    private String intentPropertyType = "";
    private String intentCity = "";
    private String intentSearchQuery = "";
    private String intentAutoSelectChip = "";
    private String intentSearchCategory = "";

    // Define property type categories
    private final List<String> RESIDENTIAL_TYPES = Arrays.asList("Room", "Flat", "Apartment", "P.G", "1 RK", "Studio Apartment", "Independent House", "Villa", "Farmhouse");
    private final List<String> COMMERCIAL_TYPES = Arrays.asList("Shop", "Office", "Godown", "Warehouse", "Commercial Space");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_search_property);

        handleIntentExtras();
        initViews();
        setupRecyclerView();
        setupListeners();
        setupStatusBar();

        applyIntentData();
        fetchProperties();
    }

    private void handleIntentExtras() {
        Intent intent = getIntent();
        if (intent != null) {
            intentPropertyType = intent.getStringExtra("property_type");
            intentCity = intent.getStringExtra("city");
            intentSearchQuery = intent.getStringExtra("search_query");
            intentAutoSelectChip = intent.getStringExtra("auto_select_chip");
            intentSearchCategory = intent.getStringExtra("search_category");

            android.util.Log.d("SearchActivity", "Intent extras - PropertyType: " + intentPropertyType +
                    ", City: " + intentCity + ", SearchQuery: " + intentSearchQuery +
                    ", AutoSelectChip: " + intentAutoSelectChip +
                    ", SearchCategory: " + intentSearchCategory);
        }
    }

    private void applyIntentData() {
        // Set search query from intent
        if (intentSearchQuery != null && !intentSearchQuery.isEmpty()) {
            searchBar.setText(intentSearchQuery);
            currentSearchQuery = intentSearchQuery;
        } else if (intentCity != null && !intentCity.isEmpty()) {
            searchBar.setText(intentCity);
            currentSearchQuery = intentCity;
        }

        // Set filter based on intent
        if (intentAutoSelectChip != null && !intentAutoSelectChip.isEmpty()) {
            currentFilter = intentAutoSelectChip;
            selectChipByName(intentAutoSelectChip);
        } else if (intentPropertyType != null && !intentPropertyType.isEmpty()) {
            currentFilter = intentPropertyType;
            selectChipByName(intentPropertyType);
        }
    }

    private void selectChipByName(String chipName) {
        switch (chipName.toLowerCase()) {
            case "all":
                chipGroup.check(R.id.chip_all);
                break;
            case "residential":
                chipGroup.check(R.id.chip_residential);
                break;
            case "commercial":
                chipGroup.check(R.id.chip_commercial);
                break;
            case "room":
                chipGroup.check(R.id.chip_room);
                break;
            case "shop":
                chipGroup.check(R.id.chip_shop);
                break;
            case "p.g":
            case "pg":
                chipGroup.check(R.id.chip_pg);
                break;
            case "godown":
                chipGroup.check(R.id.chip_godown);
                break;
            default:
                chipGroup.check(R.id.chip_all);
                break;
        }
    }

    private void initViews() {
        recyclerView = findViewById(R.id.listView);
        searchBar = findViewById(R.id.search_bar);
        backIcon = findViewById(R.id.back_arrow);
        resultsCount = findViewById(R.id.results_count);
        sortFilterButton = findViewById(R.id.sort_filter_button);
        shimmerLayout = findViewById(R.id.shimmer_layout);
        emptyStateLayout = findViewById(R.id.empty_state_layout);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        btnClearFilters = findViewById(R.id.btn_clear_filters);

        // Initialize chips
        chipGroup = findViewById(R.id.chipGroup);
        chipAll = findViewById(R.id.chip_all);
        chipResidential = findViewById(R.id.chip_residential);
        chipCommercial = findViewById(R.id.chip_commercial);
        chipRoom = findViewById(R.id.chip_room);
        chipShop = findViewById(R.id.chip_shop);
        chipPg = findViewById(R.id.chip_pg);
        chipGodown = findViewById(R.id.chip_godown);
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        propertyAdapter = new PropertyAdapter(this, filteredPropertyList);
        recyclerView.setAdapter(propertyAdapter);
    }

    private void setupListeners() {
        backIcon.setOnClickListener(v -> finish());

        // Search functionality
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearchQuery = s.toString().trim();
                applyFilters();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Chip group listener for category filtering
        chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;

            int checkedId = checkedIds.get(0);
            if (checkedId == R.id.chip_all) {
                currentFilter = "All";
            } else if (checkedId == R.id.chip_residential) {
                currentFilter = "Residential";
            } else if (checkedId == R.id.chip_commercial) {
                currentFilter = "Commercial";
            } else if (checkedId == R.id.chip_room) {
                currentFilter = "Room";
            } else if (checkedId == R.id.chip_shop) {
                currentFilter = "Shop";
            } else if (checkedId == R.id.chip_pg) {
                currentFilter = "P.G";
            } else if (checkedId == R.id.chip_godown) {
                currentFilter = "Godown";
            }

            applyFilters();
        });

        sortFilterButton.setOnClickListener(v -> showSortDialog());

        // Clear filters button
        btnClearFilters.setOnClickListener(v -> clearAllFilters());

        // Swipe to refresh listener
        swipeRefreshLayout.setOnRefreshListener(() -> {
            // Clear search query and reset filter to "All"
            searchBar.setText("");
            currentSearchQuery = "";
            chipAll.setChecked(true);
            currentFilter = "All";
            fetchProperties();
        });

        swipeRefreshLayout.setColorSchemeResources(R.color.main_color, R.color.main_color);
        swipeRefreshLayout.setProgressBackgroundColorSchemeResource(R.color.main_background_color);
    }

    private void setupStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.main_background_color));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                View decorView = window.getDecorView();
                decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private boolean isPropertyActive(Property property) {
        return property.getPropertyStatus() == null ||
                !property.getPropertyStatus().equalsIgnoreCase("pending");
    }

    private void fetchProperties() {
        showShimmerLoading(true);
        swipeRefreshLayout.setRefreshing(true);

        ApiService apiService = RetrofitClient.getApiService();
        Call<ResponseDTO> call = apiService.getAllProperties();

        call.enqueue(new Callback<ResponseDTO>() {
            @Override
            public void onResponse(Call<ResponseDTO> call, Response<ResponseDTO> response) {
                showShimmerLoading(false);
                swipeRefreshLayout.setRefreshing(false);

                if (response.isSuccessful() && response.body() != null) {
                    originalPropertyList.clear();

                    // Filter out pending properties
                    for (Property property : response.body().getMessageBody()) {
                        if (isPropertyActive(property)) {
                            originalPropertyList.add(property);
                        }
                    }

                    applyFilters();
                } else {
                    showEmptyState(true);
                    Toast.makeText(SearchPropertyActivity.this, "Failed to load properties", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseDTO> call, Throwable t) {
                showShimmerLoading(false);
                swipeRefreshLayout.setRefreshing(false);
                showEmptyState(true);

                String errorMessage;
                if (t instanceof java.net.UnknownHostException) {
                    errorMessage = "No internet connection. Please check your network.";
                } else if (t instanceof java.net.SocketTimeoutException) {
                    errorMessage = "Connection timeout. Please try again.";
                } else {
                    errorMessage = "Error: " + t.getMessage();
                }
                Toast.makeText(SearchPropertyActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Modify the applyFilters() method
    private void applyFilters() {
        List<Property> tempList = new ArrayList<>();

        // First apply category filter and status filter
        for (Property property : originalPropertyList) {
            if (matchesCurrentFilter(property) && isPropertyActive(property)) {
                tempList.add(property);
            }
        }

        // Then apply search filter
        filteredPropertyList.clear();
        if (currentSearchQuery.isEmpty()) {
            filteredPropertyList.addAll(tempList);
        } else {
            for (Property property : tempList) {
                if (matchesSearchQuery(property, currentSearchQuery)) {
                    filteredPropertyList.add(property);
                }
            }
        }

        updateUI();
    }

    private boolean matchesCurrentFilter(Property property) {
        if (currentFilter.equals("All")) {
            return true;
        } else if (currentFilter.equals("Residential")) {
            return isResidentialProperty(property);
        } else if (currentFilter.equals("Commercial")) {
            return isCommercialProperty(property);
        } else {
            // Specific property type filter
            return property.getPropertyType() != null &&
                    property.getPropertyType().equalsIgnoreCase(currentFilter);
        }
    }

    private boolean isResidentialProperty(Property property) {
        if (property.getPropertyType() != null) {
            for (String type : RESIDENTIAL_TYPES) {
                if (property.getPropertyType().toLowerCase().contains(type.toLowerCase())) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isCommercialProperty(Property property) {
        if (property.getPropertyType() != null) {
            for (String type : COMMERCIAL_TYPES) {
                if (property.getPropertyType().toLowerCase().contains(type.toLowerCase())) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean matchesSearchQuery(Property property, String query) {
        String lowerQuery = query.toLowerCase();

        return (property.getCity() != null &&
                property.getCity().toLowerCase().contains(lowerQuery)) ||
                (property.getLocality() != null &&
                        property.getLocality().toLowerCase().contains(lowerQuery)) ||
                (property.getSocietyName() != null &&
                        property.getSocietyName().toLowerCase().contains(lowerQuery)) ||
                (property.getPropertyType() != null &&
                        property.getPropertyType().toLowerCase().contains(lowerQuery));
    }

    private void updateUI() {
        int count = filteredPropertyList.size();

        if (count == 0) {
            showEmptyState(true);
            resultsCount.setText("No properties found");
        } else {
            showEmptyState(false);
            String countText = count == 1 ? "1 property found" : count + " properties found";
            resultsCount.setText(countText);
        }

        propertyAdapter.updateList(filteredPropertyList);
        updateChipCounts();
    }

    private void updateChipCounts() {
        int allCount = originalPropertyList.size();
        int residentialCount = getResidentialPropertiesCount();
        int commercialCount = getCommercialPropertiesCount();
        int roomCount = getPropertyCountByType("Room");
        int shopCount = getPropertyCountByType("Shop");
        int pgCount = getPropertyCountByType("P.G");
        int godownCount = getPropertyCountByType("Godown");

        chipAll.setText(String.format("All (%d)", allCount));
        chipResidential.setText(String.format("Residential (%d)", residentialCount));
        chipCommercial.setText(String.format("Commercial (%d)", commercialCount));
        chipRoom.setText(String.format("Room (%d)", roomCount));
        chipShop.setText(String.format("Shop (%d)", shopCount));
        chipPg.setText(String.format("PG (%d)", pgCount));
        chipGodown.setText(String.format("Godown (%d)", godownCount));
    }

    // Modify the count methods to exclude pending properties
    private int getResidentialPropertiesCount() {
        int count = 0;
        for (Property property : originalPropertyList) {
            if (isResidentialProperty(property) && isPropertyActive(property)) {
                count++;
            }
        }
        return count;
    }


    private int getCommercialPropertiesCount() {
        int count = 0;
        for (Property property : originalPropertyList) {
            if (isCommercialProperty(property) && isPropertyActive(property)) {
                count++;
            }
        }
        return count;
    }

    private int getPropertyCountByType(String type) {
        int count = 0;
        for (Property property : originalPropertyList) {
            if (property.getPropertyType() != null &&
                    property.getPropertyType().equalsIgnoreCase(type) &&
                    isPropertyActive(property)) {
                count++;
            }
        }
        return count;
    }

    private void clearAllFilters() {

        currentSearchQuery = "";
        searchBar.setText("");

        currentFilter = "All";
        chipGroup.check(R.id.chip_all);

        applyFilters();

        Toast.makeText(this, "All filters cleared", Toast.LENGTH_SHORT).show();
    }

    private void showShimmerLoading(boolean show) {
        if (show) {
            shimmerLayout.setVisibility(View.VISIBLE);
            shimmerLayout.startShimmer();
            recyclerView.setVisibility(View.GONE);
            emptyStateLayout.setVisibility(View.GONE);
            swipeRefreshLayout.setEnabled(false);
        } else {
            shimmerLayout.setVisibility(View.GONE);
            shimmerLayout.stopShimmer();
            recyclerView.setVisibility(View.VISIBLE);
            swipeRefreshLayout.setEnabled(true);
        }
    }

    private void showEmptyState(boolean show) {
        if (show) {
            emptyStateLayout.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            swipeRefreshLayout.setEnabled(true);
        } else {
            emptyStateLayout.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            swipeRefreshLayout.setEnabled(true);
        }
    }

    private void showSortDialog() {
        String[] sortOptions = {"Price: Low to High", "Price: High to Low", "Newest First", "Oldest First"};

        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Sort By")
                .setItems(sortOptions, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            sortByPrice(true); // Low to high
                            break;
                        case 1:
                            sortByPrice(false); // High to low
                            break;
                        case 2:
                            sortByDate(false); // Newest first
                            break;
                        case 3:
                            sortByDate(true); // Oldest first
                            break;
                    }
                    dialog.dismiss();
                })
                .create()
                .show();
    }

    private void sortByPrice(boolean ascending) {
        if (ascending) {
            filteredPropertyList.sort((p1, p2) -> {
                double price1 = p1.getExpectedPrice() != null ? p1.getExpectedPrice() : 0.0;
                double price2 = p2.getExpectedPrice() != null ? p2.getExpectedPrice() : 0.0;
                return Double.compare(price1, price2);
            });
        } else {
            filteredPropertyList.sort((p1, p2) -> {
                double price1 = p1.getExpectedPrice() != null ? p1.getExpectedPrice() : 0.0;
                double price2 = p2.getExpectedPrice() != null ? p2.getExpectedPrice() : 0.0;
                return Double.compare(price2, price1);
            });
        }
        propertyAdapter.updateList(filteredPropertyList);
        Toast.makeText(this, "Sorted by price", Toast.LENGTH_SHORT).show();
    }

    private void sortByDate(boolean oldestFirst) {
        if (oldestFirst) {
            filteredPropertyList.sort((p1, p2) -> {
                String date1 = p1.getCreatedAt() != null ? p1.getCreatedAt() : "";
                String date2 = p2.getCreatedAt() != null ? p2.getCreatedAt() : "";
                return date1.compareTo(date2);
            });
        } else {
            filteredPropertyList.sort((p1, p2) -> {
                String date1 = p1.getCreatedAt() != null ? p1.getCreatedAt() : "";
                String date2 = p2.getCreatedAt() != null ? p2.getCreatedAt() : "";
                return date2.compareTo(date1);
            });
        }
        propertyAdapter.updateList(filteredPropertyList);
        Toast.makeText(this, "Sorted by date", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (shimmerLayout != null) {
            shimmerLayout.stopShimmer();
        }
    }
}
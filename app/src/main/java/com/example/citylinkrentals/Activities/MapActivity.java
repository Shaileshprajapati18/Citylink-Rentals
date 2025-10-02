package com.example.citylinkrentals.Activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.lifecycle.ViewModelProvider;

import com.example.citylinkrentals.R;
import com.example.citylinkrentals.model.LocationHelper;
import com.example.citylinkrentals.model.MapViewModel;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MapActivity extends AppCompatActivity implements
        OnMapReadyCallback,
        GoogleMap.OnMapClickListener,
        GoogleMap.OnMarkerDragListener,
        GoogleMap.OnCameraIdleListener {

    private static final String TAG = "MapActivity";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private static final float DEFAULT_ZOOM = 15f;
    private static final float STREET_ZOOM = 18f;

    // Map related
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private PlacesClient placesClient;
    private Marker selectedMarker;
    private LocationCallback locationCallback;

    // UI elements
    private TextView searchTextView;
    private ImageView clearSearchButton;
    private FloatingActionButton myLocationButton;
    private MaterialButton confirmButton, cancelButton;
    private CircularProgressIndicator progressIndicator;
    private TextView locationTitleText, locationAddressText, locationCoordinatesText;
    private BottomSheetBehavior<NestedScrollView> bottomSheetBehavior;

    // ViewModel
    private MapViewModel mapViewModel;

    // Helpers
    private LocationHelper locationHelper;
    private ExecutorService executorService;
    private Handler mainHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        // Initialize helpers
        executorService = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());
        locationHelper = new LocationHelper(this);

        // Initialize ViewModel
        mapViewModel = new ViewModelProvider(this).get(MapViewModel.class);

        // Initialize views
        initializeViews();

        // Initialize Places SDK
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), getString(R.string.google_map_api_key));
        }
        placesClient = Places.createClient(this);

        // Initialize location services
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        setupLocationCallback();

        // Setup map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Setup observers
        observeViewModel();

        // Setup click listeners
        setupClickListeners();

        // Setup search
        setupPlaceAutocomplete();
    }

    private void initializeViews() {
        searchTextView = findViewById(R.id.search_text_view);
        clearSearchButton = findViewById(R.id.clear_search_button);
        myLocationButton = findViewById(R.id.my_location_button);
        confirmButton = findViewById(R.id.confirm_button);
        cancelButton = findViewById(R.id.cancel_button);
        progressIndicator = findViewById(R.id.progress_indicator);
        locationTitleText = findViewById(R.id.location_title_text);
        locationAddressText = findViewById(R.id.location_address_text);
        locationCoordinatesText = findViewById(R.id.location_coordinates_text);

        // Setup bottom sheet
        NestedScrollView bottomSheet = findViewById(R.id.bottom_sheet);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }

    private void setupLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                Location location = locationResult.getLastLocation();
                if (location != null) {
                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                    mapViewModel.setCurrentLocation(latLng);
                    moveCamera(latLng, DEFAULT_ZOOM);
                    stopLocationUpdates();
                }
            }
        };
    }

    private void observeViewModel() {
        mapViewModel.getSelectedLocation().observe(this, latLng -> {
            if (latLng != null) {
                updateMarker(latLng);
                fetchAddressFromLocation(latLng);
                confirmButton.setEnabled(true);
            }
        });

        mapViewModel.getLocationAddress().observe(this, address -> {
            if (address != null) {
                locationAddressText.setText(address);
            }
        });

        mapViewModel.getIsLoading().observe(this, isLoading -> {
            progressIndicator.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });
    }

    private void setupClickListeners() {
        searchTextView.setOnClickListener(v -> {
            // Search functionality handled by AutocompleteSupportFragment
        });

        clearSearchButton.setOnClickListener(v -> clearSearch());

        myLocationButton.setOnClickListener(v -> requestCurrentLocation());

        confirmButton.setOnClickListener(v -> returnSelectedLocation());

        cancelButton.setOnClickListener(v -> finish());
    }

    private void setupPlaceAutocomplete() {
        // This would be implemented with AutocompleteSupportFragment
        // For brevity, showing the click listener approach
        searchTextView.setOnClickListener(v -> {
            // Launch place picker or autocomplete activity
            showPlaceAutocomplete();
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        setupMapSettings();
        setupMapListeners();

        // Check permissions and get location
        if (checkLocationPermission()) {
            enableMyLocation();
            requestCurrentLocation();
        } else {
            requestLocationPermission();
        }
    }

    private void setupMapSettings() {
        if (mMap != null) {
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            mMap.getUiSettings().setZoomControlsEnabled(false);
            mMap.getUiSettings().setCompassEnabled(true);
            mMap.getUiSettings().setMapToolbarEnabled(false);
            mMap.getUiSettings().setMyLocationButtonEnabled(false); // Using custom button

            // Set map bounds to India (optional)
            if (getIntent().hasExtra("default_bounds")) {
                // Set camera bounds if needed
            }
        }
    }

    private void setupMapListeners() {
        mMap.setOnMapClickListener(this);
        mMap.setOnMarkerDragListener(this);
        mMap.setOnCameraIdleListener(this);
    }

    @Override
    public void onMapClick(@NonNull LatLng latLng) {
        mapViewModel.setSelectedLocation(latLng);
        animateBottomSheet(BottomSheetBehavior.STATE_EXPANDED);
    }

    @Override
    public void onCameraIdle() {
        // Update location based on camera center if needed
        if (mMap != null && selectedMarker != null) {
            LatLng center = mMap.getCameraPosition().target;
            // Optional: Update marker position to center
        }
    }

    @Override
    public void onMarkerDragStart(@NonNull Marker marker) {
        animateBottomSheet(BottomSheetBehavior.STATE_COLLAPSED);
    }

    @Override
    public void onMarkerDrag(@NonNull Marker marker) {
        LatLng position = marker.getPosition();
        updateCoordinatesText(position);
    }

    @Override
    public void onMarkerDragEnd(@NonNull Marker marker) {
        LatLng position = marker.getPosition();
        mapViewModel.setSelectedLocation(position);
        animateBottomSheet(BottomSheetBehavior.STATE_EXPANDED);
    }

    private void updateMarker(LatLng latLng) {
        if (mMap == null) return;

        if (selectedMarker == null) {
            MarkerOptions markerOptions = new MarkerOptions()
                    .position(latLng)
                    .title("Selected Location")
                    .draggable(true)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
            selectedMarker = mMap.addMarker(markerOptions);
        } else {
            selectedMarker.setPosition(latLng);
        }

        // Animate marker appearance
        if (selectedMarker != null) {
            dropPinAnimation(selectedMarker);
        }
    }

    private void dropPinAnimation(final Marker marker) {
        final Handler handler = new Handler();
        final long start = System.currentTimeMillis();
        final long duration = 500;

        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = System.currentTimeMillis() - start;
                float t = Math.max(
                        1 - ((float) elapsed / duration), 0);
                marker.setAnchor(0.5f, 1.0f + 2 * t);

                if (t > 0.0) {
                    handler.postDelayed(this, 16);
                }
            }
        });
    }

    private void moveCamera(LatLng latLng, float zoom) {
        if (mMap != null) {
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(latLng)
                    .zoom(zoom)
                    .bearing(0)
                    .tilt(0)
                    .build();
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }
    }

    private void fetchAddressFromLocation(LatLng latLng) {
        mapViewModel.setIsLoading(true);
        updateCoordinatesText(latLng);

        executorService.execute(() -> {
            try {
                Geocoder geocoder = new Geocoder(MapActivity.this, Locale.getDefault());
                List<Address> addresses = geocoder.getFromLocation(
                        latLng.latitude, latLng.longitude, 1);

                if (addresses != null && !addresses.isEmpty()) {
                    Address address = addresses.get(0);
                    String addressText = locationHelper.formatAddress(address);

                    mainHandler.post(() -> {
                        mapViewModel.setLocationAddress(addressText);
                        mapViewModel.setIsLoading(false);
                    });
                } else {
                    mainHandler.post(() -> {
                        mapViewModel.setLocationAddress("Unknown location");
                        mapViewModel.setIsLoading(false);
                    });
                }
            } catch (IOException e) {
                Log.e(TAG, "Geocoding failed", e);
                mainHandler.post(() -> {
                    mapViewModel.setLocationAddress("Unable to get address");
                    mapViewModel.setIsLoading(false);
                });
            }
        });
    }

    private void updateCoordinatesText(LatLng latLng) {
        String coordinates = String.format(Locale.getDefault(),
                "Lat: %.6f, Lng: %.6f", latLng.latitude, latLng.longitude);
        locationCoordinatesText.setText(coordinates);
        locationCoordinatesText.setVisibility(View.VISIBLE);
    }

    private void requestCurrentLocation() {
        if (!checkLocationPermission()) {
            requestLocationPermission();
            return;
        }

        mapViewModel.setIsLoading(true);

        LocationRequest locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
                .setWaitForAccurateLocation(false)
                .setMinUpdateIntervalMillis(2000)
                .setMaxUpdateDelayMillis(5000)
                .build();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());

            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, location -> {
                        if (location != null) {
                            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                            mapViewModel.setCurrentLocation(latLng);
                            mapViewModel.setSelectedLocation(latLng);
                            moveCamera(latLng, DEFAULT_ZOOM);
                            mapViewModel.setIsLoading(false);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to get location", e);
                        showSnackbar("Unable to get current location");
                        mapViewModel.setIsLoading(false);
                    });
        }
    }

    private void stopLocationUpdates() {
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }

    private void clearSearch() {
        searchTextView.setText("");
        clearSearchButton.setVisibility(View.GONE);
    }

    private void showPlaceAutocomplete() {
        // Implement place autocomplete UI
        // This is a simplified version - you'd typically use AutocompleteSupportFragment
        Intent intent = new Intent(this, PlaceAutocompleteActivity.class);
        if (mapViewModel.getCurrentLocation().getValue() != null) {
            LatLng current = mapViewModel.getCurrentLocation().getValue();
            intent.putExtra("latitude", current.latitude);
            intent.putExtra("longitude", current.longitude);
        }
        startActivityForResult(intent, 2001);
    }

    private void returnSelectedLocation() {
        LatLng selectedLocation = mapViewModel.getSelectedLocation().getValue();
        String address = mapViewModel.getLocationAddress().getValue();

        if (selectedLocation != null) {
            Intent resultIntent = new Intent();
            resultIntent.putExtra("latitude", selectedLocation.latitude);
            resultIntent.putExtra("longitude", selectedLocation.longitude);
            resultIntent.putExtra("address", address != null ? address : "");

            setResult(RESULT_OK, resultIntent);
            finish();
        } else {
            showSnackbar("Please select a location");
        }
    }

    private void animateBottomSheet(int state) {
        if (bottomSheetBehavior != null) {
            bottomSheetBehavior.setState(state);
        }
    }

    private boolean checkLocationPermission() {
        return ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                LOCATION_PERMISSION_REQUEST_CODE);
    }

    @SuppressWarnings("MissingPermission")
    private void enableMyLocation() {
        if (mMap != null && checkLocationPermission()) {
            mMap.setMyLocationEnabled(true);
        }
    }

    private void showSnackbar(String message) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableMyLocation();
                requestCurrentLocation();
            } else {
                showSnackbar("Location permission is required to use this feature");
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 2001 && resultCode == RESULT_OK && data != null) {
            // Handle place selection result
            double latitude = data.getDoubleExtra("latitude", 0);
            double longitude = data.getDoubleExtra("longitude", 0);
            String placeName = data.getStringExtra("place_name");

            if (latitude != 0 && longitude != 0) {
                LatLng latLng = new LatLng(latitude, longitude);
                mapViewModel.setSelectedLocation(latLng);
                moveCamera(latLng, STREET_ZOOM);

                if (placeName != null) {
                    searchTextView.setText(placeName);
                    clearSearchButton.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (checkLocationPermission() && mMap != null) {
            enableMyLocation();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopLocationUpdates();
        executorService.shutdown();
    }
}
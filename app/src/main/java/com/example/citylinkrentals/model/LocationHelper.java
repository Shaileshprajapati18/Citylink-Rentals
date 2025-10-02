package com.example.citylinkrentals.model;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Helper class for location-related operations including geocoding,
 * address formatting, and location calculations.
 *
 * Geocoder calls must NOT be called on the main thread.
 * Use provided async methods or call them on background threads.
 */
public class LocationHelper {

    private static final String TAG = "LocationHelper";
    private final Context context;
    private final Geocoder geocoder;
    private final ExecutorService executor;

    /**
     * Constructor with default Locale.
     * @param context Android context
     */
    public LocationHelper(@NonNull Context context) {
        this(context, Locale.getDefault());
    }

    /**
     * Constructor with explicit Locale.
     * @param context Android context
     * @param locale Locale to use for Geocoder
     */
    public LocationHelper(@NonNull Context context, @NonNull Locale locale) {
        this.context = context;
        this.geocoder = new Geocoder(context, locale);
        this.executor = Executors.newSingleThreadExecutor();
    }

    private void checkNotMainThread() {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            throw new IllegalStateException("Geocoder methods should NOT be called on the main thread");
        }
    }

    /**
     * Formats an Address object into a readable string
     * @param address The Address object to format
     * @return Formatted address string
     */
    public String formatAddress(@Nullable Address address) {
        if (address == null) {
            return "Unknown location";
        }

        StringBuilder addressBuilder = new StringBuilder();

        // Add feature name (e.g., business or landmark)
        if (!TextUtils.isEmpty(address.getFeatureName())) {
            addressBuilder.append(address.getFeatureName());
        }

        // Add street number and name
        if (!TextUtils.isEmpty(address.getSubThoroughfare())) {
            if (addressBuilder.length() > 0) addressBuilder.append(" ");
            addressBuilder.append(address.getSubThoroughfare());
        }
        if (!TextUtils.isEmpty(address.getThoroughfare())) {
            if (addressBuilder.length() > 0) addressBuilder.append(" ");
            addressBuilder.append(address.getThoroughfare());
        }

        // Add locality (city)
        if (!TextUtils.isEmpty(address.getLocality())) {
            if (addressBuilder.length() > 0) {
                addressBuilder.append(", ");
            }
            addressBuilder.append(address.getLocality());
        }

        // Add sub-locality if available
        if (!TextUtils.isEmpty(address.getSubLocality())) {
            if (addressBuilder.length() > 0) {
                addressBuilder.append(", ");
            }
            addressBuilder.append(address.getSubLocality());
        }

        // Add administrative area (state)
        if (!TextUtils.isEmpty(address.getAdminArea())) {
            if (addressBuilder.length() > 0) {
                addressBuilder.append(", ");
            }
            addressBuilder.append(address.getAdminArea());
        }

        // Add postal code
        if (!TextUtils.isEmpty(address.getPostalCode())) {
            if (addressBuilder.length() > 0) {
                addressBuilder.append(" ");
            }
            addressBuilder.append(address.getPostalCode());
        }

        // Add country
        if (!TextUtils.isEmpty(address.getCountryName())) {
            if (addressBuilder.length() > 0) {
                addressBuilder.append(", ");
            }
            addressBuilder.append(address.getCountryName());
        }

        return addressBuilder.length() > 0 ? addressBuilder.toString() : "Unknown location";
    }

    /**
     * Gets a short formatted address (street and locality only)
     * @param address The Address object to format
     * @return Short formatted address string
     */
    public String getShortAddress(@Nullable Address address) {
        if (address == null) {
            return "Unknown location";
        }

        StringBuilder addressBuilder = new StringBuilder();

        // Add street name
        if (!TextUtils.isEmpty(address.getThoroughfare())) {
            addressBuilder.append(address.getThoroughfare());
        }

        // Add locality (city)
        if (!TextUtils.isEmpty(address.getLocality())) {
            if (addressBuilder.length() > 0) {
                addressBuilder.append(", ");
            }
            addressBuilder.append(address.getLocality());
        }

        return addressBuilder.length() > 0 ? addressBuilder.toString() : "Unknown location";
    }

    /**
     * Gets address from coordinates synchronously.
     * Must NOT be called on main thread.
     * @param latitude Latitude coordinate
     * @param longitude Longitude coordinate
     * @return Address object or null if not found
     */
    @Nullable
    public Address getAddressFromCoordinates(double latitude, double longitude) {
        checkNotMainThread();
        try {
            if (!Geocoder.isPresent()) {
                Log.w(TAG, "Geocoder not present on this device");
                return null;
            }

            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                return addresses.get(0);
            }
        } catch (IOException e) {
            Log.e(TAG, "Error getting address from coordinates", e);
        }
        return null;
    }

    /**
     * Async callback for getAddressFromCoordinatesAsync
     */
    public interface AddressResultListener {
        void onAddressFound(@Nullable Address address);
        void onError(Exception e);
    }

    /**
     * Gets address from coordinates asynchronously.
     * @param latitude Latitude
     * @param longitude Longitude
     * @param listener Callback listener
     */
    public void getAddressFromCoordinatesAsync(double latitude, double longitude, @NonNull AddressResultListener listener) {
        executor.execute(() -> {
            try {
                Address address = getAddressFromCoordinates(latitude, longitude);
                listener.onAddressFound(address);
            } catch (Exception e) {
                listener.onError(e);
            }
        });
    }

    /**
     * Gets coordinates from address string synchronously.
     * Must NOT be called on main thread.
     * @param addressString The address string to geocode
     * @return LatLng object or null if not found
     */
    @Nullable
    public LatLng getCoordinatesFromAddress(String addressString) {
        checkNotMainThread();
        try {
            if (!Geocoder.isPresent()) {
                Log.w(TAG, "Geocoder not present on this device");
                return null;
            }

            List<Address> addresses = geocoder.getFromLocationName(addressString, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                return new LatLng(address.getLatitude(), address.getLongitude());
            }
        } catch (IOException e) {
            Log.e(TAG, "Error getting coordinates from address", e);
        }
        return null;
    }

    /**
     * Async callback for getCoordinatesFromAddressAsync
     */
    public interface CoordinatesResultListener {
        void onCoordinatesFound(@Nullable LatLng latLng);
        void onError(Exception e);
    }

    /**
     * Gets coordinates from address asynchronously.
     * @param addressString Address string
     * @param listener Callback listener
     */
    public void getCoordinatesFromAddressAsync(String addressString, @NonNull CoordinatesResultListener listener) {
        executor.execute(() -> {
            try {
                LatLng latLng = getCoordinatesFromAddress(addressString);
                listener.onCoordinatesFound(latLng);
            } catch (Exception e) {
                listener.onError(e);
            }
        });
    }

    /**
     * Calculates distance between two coordinates in meters
     * @param lat1 First latitude
     * @param lng1 First longitude
     * @param lat2 Second latitude
     * @param lng2 Second longitude
     * @return Distance in meters
     */
    public float calculateDistance(double lat1, double lng1, double lat2, double lng2) {
        Location location1 = new Location("point1");
        location1.setLatitude(lat1);
        location1.setLongitude(lng1);

        Location location2 = new Location("point2");
        location2.setLatitude(lat2);
        location2.setLongitude(lng2);

        return location1.distanceTo(location2);
    }

    /**
     * Calculates distance between two LatLng points in meters
     * @param point1 First point
     * @param point2 Second point
     * @return Distance in meters
     */
    public float calculateDistance(@NonNull LatLng point1, @NonNull LatLng point2) {
        return calculateDistance(
                point1.latitude, point1.longitude,
                point2.latitude, point2.longitude
        );
    }

    /**
     * Formats distance for display
     * @param distanceInMeters Distance in meters
     * @return Formatted distance string
     */
    public String formatDistance(float distanceInMeters) {
        if (distanceInMeters < 1000) {
            return String.format(Locale.getDefault(), "%.0f m", distanceInMeters);
        } else {
            return String.format(Locale.getDefault(), "%.1f km", distanceInMeters / 1000);
        }
    }

    /**
     * Checks if coordinates are valid
     * @param latitude Latitude to check
     * @param longitude Longitude to check
     * @return true if valid, false otherwise
     */
    public boolean areCoordinatesValid(double latitude, double longitude) {
        return latitude >= -90 && latitude <= 90 && longitude >= -180 && longitude <= 180;
    }

    /**
     * Checks if LatLng is valid
     * @param latLng LatLng to check
     * @return true if valid, false otherwise
     */
    public boolean isLatLngValid(@Nullable LatLng latLng) {
        return latLng != null && areCoordinatesValid(latLng.latitude, latLng.longitude);
    }

    /**
     * Gets a location name from address components
     * @param address Address object
     * @return Location name or "Unknown location"
     */
    public String getLocationName(@Nullable Address address) {
        if (address == null) {
            return "Unknown location";
        }

        // Try feature name first (like business names)
        if (!TextUtils.isEmpty(address.getFeatureName())) {
            return address.getFeatureName();
        }

        // Try thoroughfare (street name)
        if (!TextUtils.isEmpty(address.getThoroughfare())) {
            return address.getThoroughfare();
        }

        // Try locality (city)
        if (!TextUtils.isEmpty(address.getLocality())) {
            return address.getLocality();
        }

        // Try sub-locality
        if (!TextUtils.isEmpty(address.getSubLocality())) {
            return address.getSubLocality();
        }

        return "Unknown location";
    }

    /**
     * Formats coordinates for display
     * @param latitude Latitude
     * @param longitude Longitude
     * @return Formatted coordinates string
     */
    public String formatCoordinates(double latitude, double longitude) {
        return String.format(Locale.getDefault(), "%.6f, %.6f", latitude, longitude);
    }

    /**
     * Formats LatLng for display
     * @param latLng LatLng object
     * @return Formatted coordinates string
     */
    public String formatCoordinates(@Nullable LatLng latLng) {
        if (latLng == null) {
            return "0.000000, 0.000000";
        }
        return formatCoordinates(latLng.latitude, latLng.longitude);
    }

    /**
     * Shutdown executor service - call when no longer needed to prevent leaks
     */
    public void shutdown() {
        executor.shutdown();
    }
}

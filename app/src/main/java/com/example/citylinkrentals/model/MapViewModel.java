package com.example.citylinkrentals.model;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.maps.model.LatLng;

public class MapViewModel extends ViewModel {

    private final MutableLiveData<LatLng> selectedLocation = new MutableLiveData<>();
    private final MutableLiveData<LatLng> currentLocation = new MutableLiveData<>();
    private final MutableLiveData<String> locationAddress = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);

    // Getters for LiveData
    public LiveData<LatLng> getSelectedLocation() {
        return selectedLocation;
    }

    public LiveData<LatLng> getCurrentLocation() {
        return currentLocation;
    }

    public LiveData<String> getLocationAddress() {
        return locationAddress;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    // Setters
    public void setSelectedLocation(LatLng location) {
        selectedLocation.setValue(location);
    }

    public void setCurrentLocation(LatLng location) {
        currentLocation.setValue(location);
    }

    public void setLocationAddress(String address) {
        locationAddress.setValue(address);
    }

    public void setIsLoading(boolean loading) {
        isLoading.setValue(loading);
    }

    // Business logic methods
    public boolean hasSelectedLocation() {
        return selectedLocation.getValue() != null;
    }

    public void clearSelection() {
        selectedLocation.setValue(null);
        locationAddress.setValue(null);
    }
}
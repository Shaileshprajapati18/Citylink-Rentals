package com.example.citylinkrentals.model;

import android.location.Address;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

public class MockPrediction implements Parcelable {
    private String placeId;
    private String primaryText;
    private String secondaryText;
    private String fullText;
    private double latitude;
    private double longitude;

    // Constructor using Android Address
    public MockPrediction(Address address) {
        if (address != null) {
            this.latitude = address.hasLatitude() ? address.getLatitude() : 0.0;
            this.longitude = address.hasLongitude() ? address.getLongitude() : 0.0;
            this.placeId = "mock_" + latitude + "_" + longitude;

            this.primaryText = extractPrimaryText(address);
            this.secondaryText = extractSecondaryText(address);
            this.fullText = address.getAddressLine(0) != null ? address.getAddressLine(0) : primaryText;
        } else {
            this.placeId = "mock_place_id";
            this.primaryText = "Unknown Location";
            this.secondaryText = "";
            this.fullText = "Unknown Location";
            this.latitude = 0.0;
            this.longitude = 0.0;
        }
    }

    private String extractPrimaryText(Address address) {
        if (!TextUtils.isEmpty(address.getFeatureName())) {
            return address.getFeatureName();
        } else if (!TextUtils.isEmpty(address.getThoroughfare())) {
            return address.getThoroughfare();
        } else if (!TextUtils.isEmpty(address.getLocality())) {
            return address.getLocality();
        } else if (!TextUtils.isEmpty(address.getAddressLine(0))) {
            String[] parts = address.getAddressLine(0).split(",");
            return parts[0].trim();
        }
        return "Unknown Location";
    }

    private String extractSecondaryText(Address address) {
        StringBuilder secondary = new StringBuilder();

        if (!TextUtils.isEmpty(address.getLocality())) {
            secondary.append(address.getLocality());
        }

        if (!TextUtils.isEmpty(address.getAdminArea())) {
            if (secondary.length() > 0) secondary.append(", ");
            secondary.append(address.getAdminArea());
        }

        if (!TextUtils.isEmpty(address.getCountryName())) {
            if (secondary.length() > 0) secondary.append(", ");
            secondary.append(address.getCountryName());
        }

        return secondary.toString();
    }

    // Getters
    public String getPlaceId() {
        return placeId;
    }

    public String getPrimaryText() {
        return primaryText;
    }

    public String getSecondaryText() {
        return secondaryText;
    }

    public String getFullText() {
        return fullText;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    // Parcelable Implementation
    protected MockPrediction(Parcel in) {
        placeId = in.readString();
        primaryText = in.readString();
        secondaryText = in.readString();
        fullText = in.readString();
        latitude = in.readDouble();
        longitude = in.readDouble();
    }

    public static final Creator<MockPrediction> CREATOR = new Creator<MockPrediction>() {
        @Override
        public MockPrediction createFromParcel(Parcel in) {
            return new MockPrediction(in);
        }

        @Override
        public MockPrediction[] newArray(int size) {
            return new MockPrediction[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(placeId);
        dest.writeString(primaryText);
        dest.writeString(secondaryText);
        dest.writeString(fullText);
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
    }

    @Override
    public int describeContents() {
        return 0;
    }
}

package com.example.citylinkrentals.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class PropertyListRequest implements Serializable {

    @SerializedName("firebaseUid")
    private String firebaseUid;

    @SerializedName("category")
    private String category;

    @SerializedName("propertyKind")
    private String propertyKind;

    @SerializedName("propertyType")
    private String propertyType;

    @SerializedName("pgTarget")
    private String pgTarget;

    @SerializedName("phoneNumber")
    private String phoneNumber;

    @SerializedName("city")
    private String city;

    @SerializedName("locality")
    private String locality;

    @SerializedName("societyName")
    private String societyName;

    @SerializedName("bhkType")
    private String bhkType;

    @SerializedName("bedrooms")
    private Integer bedrooms;

    @SerializedName("bathrooms")
    private Integer bathrooms;

    @SerializedName("hole")
    private Integer hole;

    @SerializedName("kitchen")
    private Integer kitchen;

    @SerializedName("balcony")
    private Integer balcony;

    @SerializedName("areaUnit")
    private String areaUnit;

    @SerializedName("furnishing")
    private String furnishing;

    @SerializedName("totalFloor")
    private Integer totalFloor;

    @SerializedName("ownership")
    private String ownership;

    @SerializedName("availabilityStatus")
    private String availabilityStatus;

    @SerializedName("expectedPrice")
    private Double expectedPrice;

    @SerializedName("parking")
    private Boolean parking;

    @SerializedName("powerBackup")
    private String powerBackup;

    @SerializedName("propertyFacing")
    private String propertyFacing;

    @SerializedName("flooringType")
    private String flooringType;

    @SerializedName("description")
    private String description;

    @SerializedName("createdAt")
    private String createAt;

    @SerializedName("propertyStatus")
    private String propertyStatus;

    private List<String> imagesToRemove;

    public String getFirebaseUid() {
        return firebaseUid;
    }

    public String getPgTarget() {
        return pgTarget;
    }

    public void setPgTarget(String pgTarget) {
        this.pgTarget = pgTarget;
    }

    public void setFirebaseUid(String firebaseUid) {
        this.firebaseUid = firebaseUid;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getPropertyKind() {
        return propertyKind;
    }

    public void setPropertyKind(String propertyKind) {
        this.propertyKind = propertyKind;
    }

    public String getPropertyType() {
        return propertyType;
    }

    public void setPropertyType(String propertyType) {
        this.propertyType = propertyType;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getOwnership() {
        return ownership;
    }

    public void setOwnership(String ownership) {
        this.ownership = ownership;
    }

    public String getAvailabilityStatus() {
        return availabilityStatus;
    }

    public void setAvailabilityStatus(String availabilityStatus) {
        this.availabilityStatus = availabilityStatus;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getLocality() {
        return locality;
    }

    public void setLocality(String locality) {
        this.locality = locality;
    }

    public String getSocietyName() {
        return societyName;
    }

    public void setSocietyName(String societyName) {
        this.societyName = societyName;
    }

    public String getBhkType() {
        return bhkType;
    }

    public void setBhkType(String bhkType) {
        this.bhkType = bhkType;
    }

    public Integer getBedrooms() {
        return bedrooms;
    }

    public void setBedrooms(Integer bedrooms) {
        this.bedrooms = bedrooms;
    }

    public Integer getBathrooms() {
        return bathrooms;
    }

    public void setBathrooms(Integer bathrooms) {
        this.bathrooms = bathrooms;
    }

    public Integer getBalcony() {
        return balcony;
    }

    public void setBalcony(Integer balcony) {
        this.balcony = balcony;
    }

    public String getAreaUnit() {
        return areaUnit;
    }

    public void setAreaUnit(String areaUnit) {
        this.areaUnit = areaUnit;
    }

    public String getFurnishing() {
        return furnishing;
    }

    public void setFurnishing(String furnishing) {
        this.furnishing = furnishing;
    }

    public Integer getTotalFloor() {
        return totalFloor;
    }

    public void setTotalFloor(Integer totalFloor) {
        this.totalFloor = totalFloor;
    }

    public Double getExpectedPrice() {
        return expectedPrice;
    }

    public void setExpectedPrice(Double expectedPrice) {
        this.expectedPrice = expectedPrice;
    }

    public Boolean getParking() {
        return parking;
    }

    public void setParking(Boolean parking) {
        this.parking = parking;
    }

    public String getPowerBackup() {
        return powerBackup;
    }

    public void setPowerBackup(String powerBackup) {
        this.powerBackup = powerBackup;
    }

    public String getPropertyFacing() {
        return propertyFacing;
    }

    public void setPropertyFacing(String propertyFacing) {
        this.propertyFacing = propertyFacing;
    }

    public String getFlooringType() {
        return flooringType;
    }

    public void setFlooringType(String flooringType) {
        this.flooringType = flooringType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCreateAt() {
        return createAt;
    }

    public void setCreateAt(String createAt) {
        this.createAt = createAt;
    }

    public Integer getHole() {
        return hole;
    }

    public void setHole(Integer hole) {
        this.hole = hole;
    }

    public Integer getKitchen() {
        return kitchen;
    }

    public void setKitchen(Integer kitchen) {
        this.kitchen = kitchen;
    }

    public List<String> getImagesToRemove() {
        return imagesToRemove;
    }

    public void setImagesToRemove(List<String> imagesToRemove) {
        this.imagesToRemove = imagesToRemove;
    }

    public String getPropertyStatus() {
        return propertyStatus;
    }

    public void setPropertyStatus(String propertyStatus) {
        this.propertyStatus = propertyStatus;
    }
}
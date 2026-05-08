package com.example.travelappbe.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

public class WishlistResponseDto {

    @JsonProperty("user_id")
    private UUID userId;

    private String userEmail;

    @JsonProperty("location_id")
    private UUID locationId;

    private String locationName;

    private String description;

    private String category;

    private Double price;

    @JsonProperty("location_name")
    private String locName;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    // Constructors
    public WishlistResponseDto() {
    }

    public WishlistResponseDto(UUID userId, String userEmail, UUID locationId,
                             String locationName, String description, String category,
                             Double price, String locName, LocalDateTime createdAt) {
        this.userId = userId;
        this.userEmail = userEmail;
        this.locationId = locationId;
        this.locationName = locationName;
        this.description = description;
        this.category = category;
        this.price = price;
        this.locName = locName;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public UUID getLocationId() {
        return locationId;
    }

    public void setLocationId(UUID locationId) {
        this.locationId = locationId;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getLocName() {
        return locName;
    }

    public void setLocName(String locName) {
        this.locName = locName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

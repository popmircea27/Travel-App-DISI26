package com.example.travelappbe.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

public class LocationResponseDto {

    private UUID id;

    private String name;

    private String description;

    @JsonProperty("audio_url")
    private String audioUrl;
    
    private String category;

    private Double price;

    @JsonProperty("location_name")
    private String locationName;

    @JsonProperty("admin_id")
    private UUID adminId;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    // Constructors
    public LocationResponseDto() {
    }

    public LocationResponseDto(UUID id, String name, String description, String audioUrl, String category,
                               Double price, String locationName, UUID adminId, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.audioUrl = audioUrl;
        this.category = category;
        this.price = price;
        this.locationName = locationName;
        this.adminId = adminId;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAudioUrl() {
        return audioUrl;
    }

    public void setAudioUrl(String audioUrl) {
        this.audioUrl = audioUrl;
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

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public UUID getAdminId() {
        return adminId;
    }

    public void setAdminId(UUID adminId) {
        this.adminId = adminId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    // --- Frontend Compatibility Getters ---

    @JsonProperty("city")
    public String getCity() {
        if (this.locationName == null || this.locationName.trim().isEmpty()) {
            return null;
        }
        // Returns the first part before a comma, or the whole string.
        return this.locationName.split(",")[0].trim();
    }

    @JsonProperty("country")
    public String getCountry() {
        if (this.locationName == null || !this.locationName.contains(",")) {
            return null;
        }
        String[] parts = this.locationName.split(",");
        // Returns the second part if it exists.
        return parts.length > 1 ? parts[1].trim() : null;
    }

    @JsonProperty("imageUrl")
    public String getImageUrl() { return null; }
}

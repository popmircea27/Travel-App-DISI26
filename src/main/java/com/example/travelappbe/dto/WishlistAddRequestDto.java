package com.example.travelappbe.dto;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

public class WishlistAddRequestDto {

    @NotNull(message = "Location ID is required")
    @JsonProperty("location_id")
    private UUID locationId;

    // Constructors
    public WishlistAddRequestDto() {
    }

    public WishlistAddRequestDto(UUID locationId) {
        this.locationId = locationId;
    }

    // Getters and Setters
    public UUID getLocationId() {
        return locationId;
    }

    public void setLocationId(UUID locationId) {
        this.locationId = locationId;
    }
}

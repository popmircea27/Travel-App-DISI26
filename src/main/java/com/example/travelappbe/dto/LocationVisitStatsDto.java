package com.example.travelappbe.dto;

import java.util.UUID;
import com.fasterxml.jackson.annotation.JsonProperty;

public class LocationVisitStatsDto {

    @JsonProperty("location_id")
    private UUID locationId;

    @JsonProperty("location_name")
    private String locationName;

    private String category;

    @JsonProperty("total_visits")
    private long totalVisits;

    @JsonProperty("average_rating")
    private Double averageRating;

    // Constructors
    public LocationVisitStatsDto() {
    }

    public LocationVisitStatsDto(UUID locationId, String locationName, String category, long totalVisits, Double averageRating) {
        this.locationId = locationId;
        this.locationName = locationName;
        this.category = category;
        this.totalVisits = totalVisits;
        this.averageRating = averageRating;
    }

    // Getters and Setters
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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public long getTotalVisits() {
        return totalVisits;
    }

    public void setTotalVisits(long totalVisits) {
        this.totalVisits = totalVisits;
    }

    public Double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(Double averageRating) {
        this.averageRating = averageRating;
    }
}

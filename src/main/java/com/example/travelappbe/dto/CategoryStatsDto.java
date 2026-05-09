package com.example.travelappbe.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CategoryStatsDto {

    private String category;

    @JsonProperty("total_locations")
    private long totalLocations;

    @JsonProperty("total_visits")
    private long totalVisits;

    @JsonProperty("average_rating")
    private Double averageRating;

    // Constructors
    public CategoryStatsDto() {
    }

    public CategoryStatsDto(String category, long totalLocations, long totalVisits, Double averageRating) {
        this.category = category;
        this.totalLocations = totalLocations;
        this.totalVisits = totalVisits;
        this.averageRating = averageRating;
    }

    // Getters and Setters
    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public long getTotalLocations() {
        return totalLocations;
    }

    public void setTotalLocations(long totalLocations) {
        this.totalLocations = totalLocations;
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

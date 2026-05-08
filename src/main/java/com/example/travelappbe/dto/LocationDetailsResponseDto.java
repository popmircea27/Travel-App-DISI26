package com.example.travelappbe.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for returning detailed location information with reviews and rating
 * Used for GET /locations/{id}/details endpoint
 */
public class LocationDetailsResponseDto {

    private UUID id;

    private String name;

    private String description;

    @JsonProperty("audio_url")
    private String audioUrl;

    private String category;

    private Double price;

    @JsonProperty("location_name")
    private String locationName;

    private Double latitude;

    private Double longitude;

    @JsonProperty("admin_id")
    private UUID adminId;

    @JsonProperty("average_rating")
    private Double averageRating;

    @JsonProperty("total_reviews")
    private Integer totalReviews;

    private List<ReviewResponseDto> reviews;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    // Constructors
    public LocationDetailsResponseDto() {
    }

    public LocationDetailsResponseDto(UUID id, String name, String description, String audioUrl, String category,
                                      Double price, String locationName, Double latitude, Double longitude, 
                                      UUID adminId, Double averageRating,
                                      Integer totalReviews, List<ReviewResponseDto> reviews,
                                      LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.audioUrl = audioUrl;
        this.category = category;
        this.price = price;
        this.locationName = locationName;
        this.latitude = latitude;
        this.longitude = longitude;
        this.adminId = adminId;
        this.averageRating = averageRating;
        this.totalReviews = totalReviews;
        this.reviews = reviews;
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

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public UUID getAdminId() {
        return adminId;
    }

    public void setAdminId(UUID adminId) {
        this.adminId = adminId;
    }

    public Double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(Double averageRating) {
        this.averageRating = averageRating;
    }

    public Integer getTotalReviews() {
        return totalReviews;
    }

    public void setTotalReviews(Integer totalReviews) {
        this.totalReviews = totalReviews;
    }

    public List<ReviewResponseDto> getReviews() {
        return reviews;
    }

    public void setReviews(List<ReviewResponseDto> reviews) {
        this.reviews = reviews;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "LocationDetailsResponseDto{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", averageRating=" + averageRating +
                ", totalReviews=" + totalReviews +
                '}';
    }
}

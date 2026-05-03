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

    private Double latitude;

    private Double longitude;

    private String country;

    private String city;

    private String category;

    private String imageUrl;

    @JsonProperty("average_rating")
    private Double averageRating;

    @JsonProperty("total_reviews")
    private Integer totalReviews;

    private List<ReviewResponseDto> reviews;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;

    // Constructors
    public LocationDetailsResponseDto() {
    }

    public LocationDetailsResponseDto(UUID id, String name, String description, Double latitude, Double longitude,
                                      String country, String city, String category, String imageUrl, Double averageRating,
                                      Integer totalReviews, List<ReviewResponseDto> reviews,
                                      LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.latitude = latitude;
        this.longitude = longitude;
        this.country = country;
        this.city = city;
        this.category = category;
        this.imageUrl = imageUrl;
        this.averageRating = averageRating;
        this.totalReviews = totalReviews;
        this.reviews = reviews;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
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

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
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

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
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

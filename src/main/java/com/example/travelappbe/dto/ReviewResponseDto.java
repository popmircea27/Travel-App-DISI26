package com.example.travelappbe.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ReviewResponseDto {

    private UUID id;

    private Integer rating;

    private String comment;

    @JsonProperty("user_id")
    private UUID userId;

    private String userEmail;

    @JsonProperty("location_id")
    private UUID locationId;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    // Constructors
    public ReviewResponseDto() {
    }

    public ReviewResponseDto(UUID id, Integer rating, String comment, UUID userId, String userEmail,
                            UUID locationId, LocalDateTime createdAt) {
        this.id = id;
        this.rating = rating;
        this.comment = comment;
        this.userId = userId;
        this.userEmail = userEmail;
        this.locationId = locationId;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

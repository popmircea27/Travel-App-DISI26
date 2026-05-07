package com.example.travelappbe.dto;

import java.time.LocalDateTime;

public class NearbyFriendResponseDto {

    private String userId;
    private String email;
    private double latitude;
    private double longitude;
    private double distanceKm;
    private LocalDateTime updatedAt;

    public NearbyFriendResponseDto() {
    }

    public NearbyFriendResponseDto(String userId, String email, double latitude, double longitude, double distanceKm, LocalDateTime updatedAt) {
        this.userId = userId;
        this.email = email;
        this.latitude = latitude;
        this.longitude = longitude;
        this.distanceKm = distanceKm;
        this.updatedAt = updatedAt;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getDistanceKm() {
        return distanceKm;
    }

    public void setDistanceKm(double distanceKm) {
        this.distanceKm = distanceKm;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}

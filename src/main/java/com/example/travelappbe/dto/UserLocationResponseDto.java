package com.example.travelappbe.dto;

import java.time.LocalDateTime;

public class UserLocationResponseDto {

    private String userId;
    private String email;
    private double latitude;
    private double longitude;
    private String geohash;
    private LocalDateTime updatedAt;

    public UserLocationResponseDto() {
    }

    public UserLocationResponseDto(String userId, String email, double latitude, double longitude, String geohash, LocalDateTime updatedAt) {
        this.userId = userId;
        this.email = email;
        this.latitude = latitude;
        this.longitude = longitude;
        this.geohash = geohash;
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

    public String getGeohash() {
        return geohash;
    }

    public void setGeohash(String geohash) {
        this.geohash = geohash;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}

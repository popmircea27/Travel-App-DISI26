package com.example.travelappbe.dto;

import java.time.LocalDateTime;

public class UserProfileResponseDto {

    private String userId;
    private String email;
    private String displayName;
    private String profileType;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public UserProfileResponseDto() {
    }

    public UserProfileResponseDto(String userId, String email, String displayName, String profileType, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.userId = userId;
        this.email = email;
        this.displayName = displayName;
        this.profileType = profileType;
        this.createdAt = createdAt;
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

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getProfileType() {
        return profileType;
    }

    public void setProfileType(String profileType) {
        this.profileType = profileType;
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
}

package com.example.travelappbe.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;

public class LocationRequestDto {

    @NotBlank(message = "Name is required")
    private String name;

    private String description;

    @NotBlank(message = "Category is required")
    private String category;

    @JsonProperty("audio_url")
    private String audioUrl;

    private Double price;

    @NotBlank(message = "Location Name is required")
    private String locationName;

    private Double latitude;

    private Double longitude;

    @NotBlank(message = "City is required")
    private String city;

    private String country;

    private String offers;

    // Constructors
    public LocationRequestDto() {
    }

    public LocationRequestDto(String name, String category, Double price, String locationName, 
                              Double latitude, Double longitude, String city, String country, String offers) {
        this.name = name;
        this.category = category;
        this.price = price;
        this.locationName = locationName;
        this.latitude = latitude;
        this.longitude = longitude;
        this.city = city;
        this.country = country;
        this.offers = offers;
    }

    // Getters and Setters
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

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getOffers() {
        return offers;
    }

    public void setOffers(String offers) {
        this.offers = offers;
    }
}

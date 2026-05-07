package com.example.travelappbe.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Document(collection = "user_locations")
public class UserLocationDocument {

    @Id
    private String id;
    private String userId;
    private String email;
    private double latitude;
    private double longitude;
    private String geohash;
    private GeoJsonPoint coordinates;
    private LocalDateTime updatedAt;

    public UserLocationDocument() {
    }

    public UserLocationDocument(String userId, String email, double latitude, double longitude, String geohash, GeoJsonPoint coordinates, LocalDateTime updatedAt) {
        this.userId = userId;
        this.email = email;
        this.latitude = latitude;
        this.longitude = longitude;
        this.geohash = geohash;
        this.coordinates = coordinates;
        this.updatedAt = updatedAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public GeoJsonPoint getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(GeoJsonPoint coordinates) {
        this.coordinates = coordinates;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}

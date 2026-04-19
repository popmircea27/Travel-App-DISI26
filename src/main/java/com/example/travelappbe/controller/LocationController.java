package com.example.travelappbe.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.travelappbe.dto.LocationRequestDto;
import com.example.travelappbe.dto.LocationResponseDto;
import com.example.travelappbe.dto.ReviewRequestDto;
import com.example.travelappbe.dto.ReviewResponseDto;
import com.example.travelappbe.entity.User;
import com.example.travelappbe.security.JwtTokenProvider;
import com.example.travelappbe.service.LocationService;
import com.example.travelappbe.service.ReviewService;
import com.example.travelappbe.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

/**
 * REST Controller for location-related operations (list, create, update, delete, reviews)
 */
@RestController
@RequestMapping("/api/locations")
@CrossOrigin(origins = "*", maxAge = 3600)
public class LocationController {

    private final LocationService locationService;
    private final ReviewService reviewService;
    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;

    public LocationController(LocationService locationService, ReviewService reviewService,
                            UserService userService, JwtTokenProvider jwtTokenProvider) {
        this.locationService = locationService;
        this.reviewService = reviewService;
        this.userService = userService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    /**
     * Get all locations.
     *
     * @return ResponseEntity with list of all locations
     */
    @GetMapping
    public ResponseEntity<List<LocationResponseDto>> getAllLocations() {
        List<LocationResponseDto> locations = locationService.getAllLocations();
        return ResponseEntity.ok(locations);
    }

    /**
     * Get a specific location by ID.
     *
     * @param id the location ID
     * @return ResponseEntity with the location details
     */
    @GetMapping("/{id}")
    public ResponseEntity<LocationResponseDto> getLocationById(@PathVariable UUID id) {
        try {
            LocationResponseDto location = locationService.getLocationById(id);
            return ResponseEntity.ok(location);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Create a new location (ADMIN ONLY).
     *
     * @param locationRequestDto the location data
     * @return ResponseEntity with the created location
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LocationResponseDto> createLocation(@Valid @RequestBody LocationRequestDto locationRequestDto) {
        LocationResponseDto location = locationService.createLocation(locationRequestDto);
        return new ResponseEntity<>(location, HttpStatus.CREATED);
    }

    /**
     * Update an existing location (ADMIN ONLY).
     *
     * @param id the location ID
     * @param locationRequestDto the updated location data
     * @return ResponseEntity with the updated location
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LocationResponseDto> updateLocation(@PathVariable UUID id,
                                                             @Valid @RequestBody LocationRequestDto locationRequestDto) {
        try {
            LocationResponseDto location = locationService.updateLocation(id, locationRequestDto);
            return ResponseEntity.ok(location);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Delete a location (ADMIN ONLY).
     *
     * @param id the location ID
     * @return ResponseEntity with no content
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteLocation(@PathVariable UUID id) {
        try {
            locationService.deleteLocation(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get all reviews for a location.
     *
     * @param locationId the location ID
     * @return ResponseEntity with list of reviews
     */
    @GetMapping("/{locationId}/reviews")
    public ResponseEntity<List<ReviewResponseDto>> getReviews(@PathVariable UUID locationId) {
        try {
            List<ReviewResponseDto> reviews = reviewService.getReviewsByLocation(locationId);
            return ResponseEntity.ok(reviews);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Add a review to a location.
     *
     * @param locationId the location ID
     * @param reviewRequestDto the review data
     * @param request the HTTP request containing the JWT token
     * @return ResponseEntity with the created review
     */
    @PostMapping("/{locationId}/reviews")
    public ResponseEntity<ReviewResponseDto> addReview(@PathVariable UUID locationId,
                                                       @Valid @RequestBody ReviewRequestDto reviewRequestDto,
                                                       HttpServletRequest request) {
        String email = extractEmailFromToken(request);
        if (email == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            User user = userService.getUserByEmail(email);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            ReviewResponseDto review = reviewService.addReview(locationId, user, reviewRequestDto);
            return new ResponseEntity<>(review, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Extract email from JWT token in the Authorization header.
     *
     * @param request the HTTP request
     * @return email if token is valid, null otherwise
     */
    private String extractEmailFromToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                return jwtTokenProvider.validateAndGetEmail(token);
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }
}

package com.example.travelappbe.controller;

import java.util.UUID;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.travelappbe.dto.LocationDetailsResponseDto;
import com.example.travelappbe.dto.LocationRequestDto;
import com.example.travelappbe.dto.LocationResponseDto;
import com.example.travelappbe.security.JwtTokenProvider;
import com.example.travelappbe.service.LocationService;
import com.example.travelappbe.service.ReviewService;
import com.example.travelappbe.service.UserService;
import com.example.travelappbe.entity.User;

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
     * Get locations with optional filtering and pagination.
     *
     * @param category optional category filter
     * @param locationName optional locationName filter
     * @param pageable pagination info
     * @return ResponseEntity with page of locations
     */
    @GetMapping
    public ResponseEntity<List<LocationResponseDto>> getLocations(@RequestParam(required = false) String category, @RequestParam(required = false) String locationName, Pageable pageable) {
        List<LocationResponseDto> locations = locationService.getLocations(category, locationName, pageable);
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
     * Get detailed location information including reviews and average rating.
     * Returns full location details, list of reviews, and calculated average rating.
     *
     * @param id the location ID
     * @return ResponseEntity with LocationDetailsResponseDto containing aggregated data
     */
    @GetMapping("/{id}/details")
    public ResponseEntity<LocationDetailsResponseDto> getLocationDetails(@PathVariable UUID id) {
        try {
            LocationDetailsResponseDto details = locationService.getLocationDetails(id);
            return ResponseEntity.ok(details);
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
    public ResponseEntity<LocationResponseDto> createLocation(@Valid @RequestBody LocationRequestDto locationRequestDto, HttpServletRequest request) {
        String email = extractEmailFromToken(request);
        User admin = userService.getUserByEmail(email);
        if (admin == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        LocationResponseDto location = locationService.createLocation(locationRequestDto, admin);
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

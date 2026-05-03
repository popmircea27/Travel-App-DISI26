package com.example.travelappbe.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.travelappbe.dto.LocationDetailsResponseDto;
import com.example.travelappbe.dto.LocationRequestDto;
import com.example.travelappbe.dto.LocationResponseDto;
import com.example.travelappbe.dto.ReviewResponseDto;
import com.example.travelappbe.entity.Location;
import com.example.travelappbe.entity.Review;
import com.example.travelappbe.repository.LocationRepository;
import com.example.travelappbe.repository.ReviewRepository;

@Service
public class LocationService {

    private final LocationRepository locationRepository;
    private final ReviewRepository reviewRepository;

    public LocationService(LocationRepository locationRepository, ReviewRepository reviewRepository) {
        this.locationRepository = locationRepository;
        this.reviewRepository = reviewRepository;
    }

    /**
     * Retrieves all locations.
     *
     * @return List of LocationResponseDto
     */
    public List<LocationResponseDto> getAllLocations() {
        return locationRepository.findAll()
                .stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves a location by ID.
     *
     * @param id the location ID
     * @return LocationResponseDto
     * @throws IllegalArgumentException if location not found
     */
    public LocationResponseDto getLocationById(UUID id) {
        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Location not found with id: " + id));
        return convertToResponseDto(location);
    }

    /**
     * Retrieves detailed location information including reviews and average rating.
     * Used for GET /locations/{id}/details endpoint
     *
     * @param id the location ID
     * @return LocationDetailsResponseDto with aggregated data
     * @throws IllegalArgumentException if location not found
     */
    @Transactional(readOnly = true)
    public LocationDetailsResponseDto getLocationDetails(UUID id) {
        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Location not found with id: " + id));

        // Fetch all reviews for this location
        List<Review> reviews = reviewRepository.findByLocation(location);

        // Convert reviews to DTOs
        List<ReviewResponseDto> reviewDtos = reviews.stream()
                .map(this::convertReviewToDto)
                .collect(Collectors.toList());

        // Calculate average rating
        Double averageRating = reviews.isEmpty() ? null :
                reviews.stream()
                        .mapToDouble(Review::getRating)
                        .average()
                        .orElse(0.0);

        // Return detailed response with all aggregated data
        return new LocationDetailsResponseDto(
                location.getId(),
                location.getName(),
                location.getDescription(),
                location.getLatitude(),
                location.getLongitude(),
                location.getCountry(),
                location.getCity(),
                location.getImageUrl(),
                averageRating,
                reviews.size(),
                reviewDtos,
                location.getCreatedAt(),
                location.getUpdatedAt()
        );
    }

    /**
     * Creates a new location (ADMIN ONLY).
     *
     * @param locationRequestDto the location data
     * @return LocationResponseDto
     */
    @Transactional
    public LocationResponseDto createLocation(LocationRequestDto locationRequestDto) {
        Location location = new Location();
        location.setName(locationRequestDto.getName());
        location.setDescription(locationRequestDto.getDescription());
        location.setLatitude(locationRequestDto.getLatitude());
        location.setLongitude(locationRequestDto.getLongitude());
        location.setCountry(locationRequestDto.getCountry());
        location.setCity(locationRequestDto.getCity());
        location.setImageUrl(locationRequestDto.getImageUrl());

        Location savedLocation = locationRepository.save(location);
        return convertToResponseDto(savedLocation);
    }

    /**
     * Updates an existing location (ADMIN ONLY).
     *
     * @param id the location ID
     * @param locationRequestDto the updated location data
     * @return LocationResponseDto
     * @throws IllegalArgumentException if location not found
     */
    @Transactional
    public LocationResponseDto updateLocation(UUID id, LocationRequestDto locationRequestDto) {
        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Location not found with id: " + id));

        if (locationRequestDto.getName() != null) {
            location.setName(locationRequestDto.getName());
        }
        if (locationRequestDto.getDescription() != null) {
            location.setDescription(locationRequestDto.getDescription());
        }
        if (locationRequestDto.getLatitude() != null) {
            location.setLatitude(locationRequestDto.getLatitude());
        }
        if (locationRequestDto.getLongitude() != null) {
            location.setLongitude(locationRequestDto.getLongitude());
        }
        if (locationRequestDto.getCountry() != null) {
            location.setCountry(locationRequestDto.getCountry());
        }
        if (locationRequestDto.getCity() != null) {
            location.setCity(locationRequestDto.getCity());
        }
        if (locationRequestDto.getImageUrl() != null) {
            location.setImageUrl(locationRequestDto.getImageUrl());
        }

        Location updatedLocation = locationRepository.save(location);
        return convertToResponseDto(updatedLocation);
    }

    /**
     * Deletes a location (ADMIN ONLY).
     *
     * @param id the location ID
     * @throws IllegalArgumentException if location not found
     */
    @Transactional
    public void deleteLocation(UUID id) {
        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Location not found with id: " + id));
        locationRepository.delete(location);
    }

    /**
     * Convert Location entity to LocationResponseDto.
     */
    private LocationResponseDto convertToResponseDto(Location location) {
        return new LocationResponseDto(
                location.getId(),
                location.getName(),
                location.getDescription(),
                location.getLatitude(),
                location.getLongitude(),
                location.getCountry(),
                location.getCity(),
                location.getImageUrl(),
                location.getCreatedAt(),
                location.getUpdatedAt()
        );
    }

    /**
     * Convert Review entity to ReviewResponseDto.
     */
    private ReviewResponseDto convertReviewToDto(Review review) {
        return new ReviewResponseDto(
                review.getId(),
                review.getRating(),
                review.getComment(),
                review.getUser().getId(),
                review.getUser().getEmail(),
                review.getLocation().getId(),
                review.getCreatedAt(),
                review.getUpdatedAt()
        );
    }
}

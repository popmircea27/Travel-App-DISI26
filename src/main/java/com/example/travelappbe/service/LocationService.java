package com.example.travelappbe.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.travelappbe.dto.LocationDetailsResponseDto;
import com.example.travelappbe.dto.LocationRequestDto;
import com.example.travelappbe.dto.LocationResponseDto;
import com.example.travelappbe.dto.ReviewResponseDto;
import com.example.travelappbe.entity.Location;
import com.example.travelappbe.entity.User;
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
     * Retrieves locations with optional filtering and pagination.
     *
     * @param category optional category filter
     * @param locationName optional locationName filter
     * @param pageable pagination info
     * @return Page of LocationResponseDto
     */
    public List<LocationResponseDto> getLocations(String category, String locationName, Pageable pageable) {
        Location probe = new Location();
        boolean hasFilters = false;
        
        if (category != null && !category.trim().isEmpty()) {
            probe.setCategory(category.trim());
            hasFilters = true;
        }
        if (locationName != null && !locationName.trim().isEmpty()) {
            probe.setLocationName(locationName.trim());
            hasFilters = true;
        }
        
        if (hasFilters) {
            ExampleMatcher matcher = ExampleMatcher.matching()
                    .withIgnoreNullValues()
                    .withIgnorePaths("price")
                    .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING)
                    .withIgnoreCase();
            Example<Location> example = Example.of(probe, matcher);
            return locationRepository.findAll(example, pageable).map(this::convertToResponseDto).getContent();
        } else {
            return locationRepository.findAll(pageable).map(this::convertToResponseDto).getContent();
        }
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
                location.getAudioUrl(),
                location.getCategory(),
                location.getPrice(),
                location.getLocationName(),
                location.getAdmin().getId(),
                averageRating,
                reviews.size(),
                reviewDtos,
                location.getCreatedAt()
        );
    }

    /**
     * Creates a new location (ADMIN ONLY).
     *
     * @param locationRequestDto the location data
     * @param admin the admin creating this location
     * @return LocationResponseDto
     */
    @Transactional
    public LocationResponseDto createLocation(LocationRequestDto locationRequestDto, User admin) {
        Location location = new Location();
        location.setName(locationRequestDto.getName());
        location.setDescription(locationRequestDto.getDescription());
        location.setCategory(locationRequestDto.getCategory());
        location.setAudioUrl(locationRequestDto.getAudioUrl());
        location.setPrice(locationRequestDto.getPrice() != null ? locationRequestDto.getPrice() : 0.0);
        location.setLocationName(locationRequestDto.getLocationName());
        location.setAdmin(admin);

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
        if (locationRequestDto.getAudioUrl() != null) {
            location.setAudioUrl(locationRequestDto.getAudioUrl());
        }
        if (locationRequestDto.getCategory() != null) {
            location.setCategory(locationRequestDto.getCategory());
        }
        if (locationRequestDto.getPrice() != null) {
            location.setPrice(locationRequestDto.getPrice());
        }
        if (locationRequestDto.getLocationName() != null) {
            location.setLocationName(locationRequestDto.getLocationName());
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
                location.getAudioUrl(),
                location.getCategory(),
                location.getPrice(),
                location.getLocationName(),
                location.getAdmin().getId(),
                location.getCreatedAt()
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
                review.getCreatedAt()
        );
    }
}

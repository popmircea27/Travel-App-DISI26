package com.example.travelappbe.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.travelappbe.dto.ReviewRequestDto;
import com.example.travelappbe.dto.ReviewResponseDto;
import com.example.travelappbe.entity.Location;
import com.example.travelappbe.entity.Review;
import com.example.travelappbe.entity.User;
import com.example.travelappbe.repository.LocationRepository;
import com.example.travelappbe.repository.ReviewRepository;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final LocationRepository locationRepository;

    public ReviewService(ReviewRepository reviewRepository, LocationRepository locationRepository) {
        this.reviewRepository = reviewRepository;
        this.locationRepository = locationRepository;
    }

    /**
     * Retrieves all reviews for a location.
     *
     * @param locationId the location ID
     * @return List of ReviewResponseDto
     * @throws IllegalArgumentException if location not found
     */
    public List<ReviewResponseDto> getReviewsByLocation(UUID locationId) {
        Location location = locationRepository.findById(locationId)
                .orElseThrow(() -> new IllegalArgumentException("Location not found with id: " + locationId));

        return reviewRepository.findByLocation(location)
                .stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves paginated reviews for a location natively from the database.
     *
     * @param locationId the location ID
     * @param pageable the pagination information
     * @return Page of ReviewResponseDto
     * @throws IllegalArgumentException if location not found
     */
    public Page<ReviewResponseDto> getReviewsByLocation(UUID locationId, Pageable pageable) {
        Location location = locationRepository.findById(locationId)
                .orElseThrow(() -> new IllegalArgumentException("Location not found with id: " + locationId));

        Page<Review> reviewPage = reviewRepository.findByLocation(location, pageable);
        return reviewPage.map(this::convertToResponseDto);
    }

    /**
     * Adds a review to a location.
     *
     * @param locationId the location ID
     * @param user the authenticated user
     * @param reviewRequestDto the review data
     * @return ReviewResponseDto
     * @throws IllegalArgumentException if location not found
     */
    @Transactional
    public ReviewResponseDto addReview(UUID locationId, User user, ReviewRequestDto reviewRequestDto) {
        Location location = locationRepository.findById(locationId)
                .orElseThrow(() -> new IllegalArgumentException("Location not found with id: " + locationId));

        Review review = new Review();
        review.setRating(reviewRequestDto.getRating());
        review.setComment(reviewRequestDto.getComment());
        review.setLocation(location);
        review.setUser(user);
        review.setCreatedAt(java.time.LocalDateTime.now());
        review.setUpdatedAt(java.time.LocalDateTime.now());

        Review savedReview = reviewRepository.save(review);
        return convertToResponseDto(savedReview);
    }

    /**
     * Convert Review entity to ReviewResponseDto.
     */
    private ReviewResponseDto convertToResponseDto(Review review) {
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

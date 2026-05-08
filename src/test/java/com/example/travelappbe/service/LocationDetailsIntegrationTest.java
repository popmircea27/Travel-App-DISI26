package com.example.travelappbe.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.example.travelappbe.dto.LocationDetailsResponseDto;
import com.example.travelappbe.dto.LocationRequestDto;
import com.example.travelappbe.dto.LocationResponseDto;
import com.example.travelappbe.dto.ReviewRequestDto;
import com.example.travelappbe.entity.Location;
import com.example.travelappbe.entity.Review;
import com.example.travelappbe.entity.User;
import com.example.travelappbe.entity.UserRole;
import com.example.travelappbe.repository.LocationRepository;
import com.example.travelappbe.repository.ReviewRepository;
import com.example.travelappbe.repository.UserRepository;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.within;

/**
 * Integration tests for Location Details functionality (US3 - SCRUM-46)
 * Tests the getLocationDetails endpoint that returns:
 * - Full location details (description, category/attributes, media)
 * - Average rating from reviews
 * - List of all reviews
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("Location Details Integration Tests (US3 - SCRUM-46)")
class LocationDetailsIntegrationTest {

    @Autowired
    private LocationService locationService;

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private UserService userService;

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private UUID locationId;
    private UUID userId;

    @BeforeEach
    void setUp() {
        // Create a test user for reviews
        User user = new User("tourist@example.com", passwordEncoder.encode("password123"), UserRole.TOURIST);
        User savedUser = userRepository.save(user);
        userId = savedUser.getId();

        // Create a test location
        Location location = new Location(
                "Eiffel Tower",
                "The iconic iron lattice tower built in 1889 for the World's Fair",
                "Monument",
                20.0,
                "Paris",
                48.8584,
                2.2945
        );
        location.setAdmin(savedUser);
        Location savedLocation = locationRepository.save(location);
        locationId = savedLocation.getId();
    }

    // ============================================================
    // FULL LOCATION DETAILS TESTS
    // ============================================================

    @Test
    @DisplayName("Should return complete location details with all fields")
    void testGetLocationDetailsReturnsAllFields() {
        // Act
        LocationDetailsResponseDto details = locationService.getLocationDetails(locationId);

        // Assert - Verify all location fields are present
        assertThat(details).isNotNull();
        assertThat(details.getId()).isEqualTo(locationId);
        assertThat(details.getName()).isEqualTo("Eiffel Tower");
        assertThat(details.getDescription()).isEqualTo("The iconic iron lattice tower built in 1889 for the World's Fair");
        assertThat(details.getLocationName()).isEqualTo("Paris");
        assertThat(details.getPrice()).isEqualTo(20.0);
    }

    @Test
    @DisplayName("Should return empty reviews list and null average rating when no reviews exist")
    void testGetLocationDetailsWithNoReviews() {
        // Act
        LocationDetailsResponseDto details = locationService.getLocationDetails(locationId);

        // Assert
        assertThat(details.getReviews()).isEmpty();
        assertThat(details.getAverageRating()).isNull();
        assertThat(details.getTotalReviews()).isZero();
    }

    @Test
    @DisplayName("Should return list of reviews with user information")
    void testGetLocationDetailsReturnsReviewsList() {
        // Arrange - Add multiple reviews to location
        Location location = locationRepository.findById(locationId).get();
        User user = userRepository.findById(userId).get();

        Review review1 = new Review(5, "Amazing landmark!", location, user);
        Review review2 = new Review(4, "Beautiful but crowded", location, user);
        reviewRepository.save(review1);
        reviewRepository.save(review2);

        // Act
        LocationDetailsResponseDto details = locationService.getLocationDetails(locationId);

        // Assert - Verify reviews are returned with all details
        assertThat(details.getReviews()).hasSize(2);
        assertThat(details.getTotalReviews()).isEqualTo(2);

        // Verify each review has required fields
        details.getReviews().forEach(review -> {
            assertThat(review.getId()).isNotNull();
            assertThat(review.getRating()).isIn(4, 5);
            assertThat(review.getComment()).isNotEmpty();
            assertThat(review.getUserEmail()).isEqualTo("tourist@example.com");
            assertThat(review.getLocationId()).isEqualTo(locationId);
            assertThat(review.getCreatedAt()).isNotNull();
        });
    }

    // ============================================================
    // AVERAGE RATING TESTS
    // ============================================================

    @Test
    @DisplayName("Should calculate correct average rating from reviews")
    void testAverageRatingCalculation() {
        // Arrange - Add reviews with known ratings
        Location location = locationRepository.findById(locationId).get();
        User user = userRepository.findById(userId).get();

        Review review1 = new Review(5, "Excellent", location, user);
        Review review2 = new Review(4, "Good", location, user);
        Review review3 = new Review(3, "Average", location, user);
        reviewRepository.saveAll(List.of(review1, review2, review3));

        // Act
        LocationDetailsResponseDto details = locationService.getLocationDetails(locationId);

        // Assert - Verify average is (5+4+3)/3 = 4.0
        assertThat(details.getAverageRating()).isEqualTo(4.0);
    }

    @Test
    @DisplayName("Should handle single review rating correctly")
    void testAverageRatingWithSingleReview() {
        // Arrange
        Location location = locationRepository.findById(locationId).get();
        User user = userRepository.findById(userId).get();

        Review review = new Review(5, "Perfect!", location, user);
        reviewRepository.save(review);

        // Act
        LocationDetailsResponseDto details = locationService.getLocationDetails(locationId);

        // Assert
        assertThat(details.getAverageRating()).isEqualTo(5.0);
        assertThat(details.getTotalReviews()).isOne();
    }

    @Test
    @DisplayName("Should handle five-star rating scale correctly")
    void testAverageRatingWithVariousScales() {
        // Arrange - Add reviews with 1-5 star ratings
        Location location = locationRepository.findById(locationId).get();
        User user = userRepository.findById(userId).get();

        Review review1 = new Review(1, "Poor", location, user);
        Review review2 = new Review(2, "Fair", location, user);
        Review review3 = new Review(3, "Average", location, user);
        Review review4 = new Review(4, "Good", location, user);
        Review review5 = new Review(5, "Excellent", location, user);
        reviewRepository.saveAll(List.of(review1, review2, review3, review4, review5));

        // Act
        LocationDetailsResponseDto details = locationService.getLocationDetails(locationId);

        // Assert - Verify average is (1+2+3+4+5)/5 = 3.0
        assertThat(details.getAverageRating()).isEqualTo(3.0);
        assertThat(details.getTotalReviews()).isEqualTo(5);
    }

    // ============================================================
    // ERROR HANDLING TESTS
    // ============================================================

    @Test
    @DisplayName("Should throw exception when location not found")
    void testGetLocationDetailsWithNonExistentLocation() {
        // Act & Assert
        UUID nonExistentId = UUID.randomUUID();
        assertThatThrownBy(() -> locationService.getLocationDetails(nonExistentId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Location not found with id: " + nonExistentId);
    }

    // ============================================================
    // COMPLETE INFORMATION AGGREGATION TESTS
    // ============================================================

    @Test
    @DisplayName("Should aggregate all data for detailed location response")
    void testCompleteLocationDetailsAggregation() {
        // Arrange - Create location with multiple reviews
        Location location = locationRepository.findById(locationId).get();
        User user = userRepository.findById(userId).get();

        Review review1 = new Review(5, "Stunning architecture!", location, user);
        Review review2 = new Review(4, "Worth visiting", location, user);
        Review review3 = new Review(5, "Absolutely beautiful", location, user);
        reviewRepository.saveAll(List.of(review1, review2, review3));

        // Act
        LocationDetailsResponseDto details = locationService.getLocationDetails(locationId);

        // Assert - Verify complete aggregation
        assertThat(details.getName()).isEqualTo("Eiffel Tower");
        assertThat(details.getLocationName()).isEqualTo("Paris");
        assertThat(details.getTotalReviews()).isEqualTo(3);
        assertThat(details.getAverageRating()).isCloseTo(4.67, within(0.01));

        // Verify description and media
        assertThat(details.getDescription()).isNotEmpty();

        // Verify reviews list
        assertThat(details.getReviews()).hasSize(3);
        assertThat(details.getReviews())
                .extracting("rating")
                .containsExactlyInAnyOrder(5, 4, 5);
    }

    @Test
    @DisplayName("Should include timestamps in detailed response")
    void testLocationDetailsIncludesTimestamps() {
        // Act
        LocationDetailsResponseDto details = locationService.getLocationDetails(locationId);

        // Assert
        assertThat(details.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Should return details for location with maximum reviews")
    void testLocationDetailsWithMultipleReviewsPerLocation() {
        // Arrange - Add 10 reviews to test performance and completeness
        Location location = locationRepository.findById(locationId).get();
        User user = userRepository.findById(userId).get();

        for (int i = 1; i <= 10; i++) {
            Review review = new Review(
                    (i % 5) + 1, // Cycle through ratings 1-5
                    "Review " + i,
                    location,
                    user
            );
            reviewRepository.save(review);
        }

        // Act
        LocationDetailsResponseDto details = locationService.getLocationDetails(locationId);

        // Assert
        assertThat(details.getTotalReviews()).isEqualTo(10);
        assertThat(details.getReviews()).hasSize(10);
        assertThat(details.getAverageRating()).isNotNull();
    }
}

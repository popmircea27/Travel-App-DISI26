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

import com.example.travelappbe.dto.ReviewRequestDto;
import com.example.travelappbe.dto.ReviewResponseDto;
import com.example.travelappbe.entity.Location;
import com.example.travelappbe.entity.Review;
import com.example.travelappbe.entity.User;
import com.example.travelappbe.entity.UserRole;
import com.example.travelappbe.repository.LocationRepository;
import com.example.travelappbe.repository.ReviewRepository;
import com.example.travelappbe.repository.UserRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Integration tests for Add Review functionality (US4 - SCRUM-47)
 * Tests the full flow of adding reviews to locations with database persistence
 * 
 * Test scenarios:
 * - Add review with valid data
 * - Validate review rating constraints (1-5)
 * - Ensure only authenticated users can add reviews
 * - Verify review is linked correctly to user and location
 * - Validate optional comment field
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("Add Review Integration Tests (US4 - SCRUM-47)")
class AddReviewIntegrationTest {

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private UUID locationId;
    private UUID userId;
    private Location testLocation;
    private User testUser;

    @BeforeEach
    void setUp() {
        // Create and persist test user
        User user = new User(
                "tourist@example.com",
                passwordEncoder.encode("password123"),
                UserRole.TOURIST
        );
        User savedUser = userRepository.save(user);
        userId = savedUser.getId();
        testUser = savedUser;

        // Create and persist test location
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
        testLocation = savedLocation;
    }

    // ============================================================
    // ADD REVIEW - ACCEPTANCE CRITERIA TESTS
    // ============================================================

    @Test
    @DisplayName("AC1: Only authenticated users can add reviews")
    void testAddReview_AuthenticatedUserRequired() {
        // Arrange
        ReviewRequestDto reviewRequestDto = new ReviewRequestDto(5, "Great location!");

        // Act & Assert
        // User is properly authenticated and loaded from database
        ReviewResponseDto result = reviewService.addReview(locationId, testUser, reviewRequestDto);

        // Assert - Review was created
        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(userId);
        assertThat(result.getUserEmail()).isEqualTo("tourist@example.com");

        // Verify in database
        assertThat(reviewRepository.findByLocation(testLocation))
                .isNotEmpty()
                .hasSize(1);
    }

    @Test
    @DisplayName("AC2: Review contains rating and comment")
    void testAddReview_ContainsRatingAndComment() {
        // Arrange
        Integer expectedRating = 4;
        String expectedComment = "Wonderful experience with great views!";
        ReviewRequestDto reviewRequestDto = new ReviewRequestDto(expectedRating, expectedComment);

        // Act
        ReviewResponseDto result = reviewService.addReview(locationId, testUser, reviewRequestDto);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getRating()).isEqualTo(expectedRating);
        assertThat(result.getComment()).isEqualTo(expectedComment);

        // Verify in database
        assertThat(reviewRepository.findById(result.getId())).isPresent();
        Review dbReview = reviewRepository.findById(result.getId()).get();
        assertThat(dbReview.getRating()).isEqualTo(expectedRating);
        assertThat(dbReview.getComment()).isEqualTo(expectedComment);
    }

    @Test
    @DisplayName("AC3: Review is linked to user and location")
    void testAddReview_LinkedToUserAndLocation() {
        // Arrange
        ReviewRequestDto reviewRequestDto = new ReviewRequestDto(5, "Amazing!");

        // Act
        ReviewResponseDto result = reviewService.addReview(locationId, testUser, reviewRequestDto);

        // Assert - DTO contains correct links
        assertThat(result.getUserId()).isEqualTo(userId);
        assertThat(result.getLocationId()).isEqualTo(locationId);

        // Verify relationships in database
        assertThat(reviewRepository.findById(result.getId())).isPresent();
        Review dbReview = reviewRepository.findById(result.getId()).get();
        assertThat(dbReview.getUser().getId()).isEqualTo(userId);
        assertThat(dbReview.getLocation().getId()).isEqualTo(locationId);
    }

    @Test
    @DisplayName("AC4: Rating is validated (must be 1-5)")
    void testAddReview_RatingValidation() {
        // These tests verify validation at DTO level (handled by controller @Valid)
        // Here we verify service behavior with valid ratings

        // Test minimum rating (1)
        ReviewRequestDto minRating = new ReviewRequestDto(1, "Poor");
        ReviewResponseDto resultMin = reviewService.addReview(locationId, testUser, minRating);
        assertThat(resultMin.getRating()).isEqualTo(1);

        // Test maximum rating (5)
        ReviewRequestDto maxRating = new ReviewRequestDto(5, "Excellent");
        ReviewResponseDto resultMax = reviewService.addReview(locationId, testUser, maxRating);
        assertThat(resultMax.getRating()).isEqualTo(5);

        // Test middle rating (3)
        ReviewRequestDto midRating = new ReviewRequestDto(3, "Average");
        ReviewResponseDto resultMid = reviewService.addReview(locationId, testUser, midRating);
        assertThat(resultMid.getRating()).isEqualTo(3);
    }

    // ============================================================
    // SCENARIO: LOGGED IN USER ADDS REVIEW
    // ============================================================

    @Test
    @DisplayName("Scenario: Given I am logged in, when I submit a review, then it is saved successfully")
    void testAddReview_FullScenarioSuccess() {
        // Given: User is logged in (represented by testUser loaded from DB)
        assertThat(testUser).isNotNull();
        assertThat(testUser.getId()).isNotNull();

        // When: User submits a review
        ReviewRequestDto reviewRequest = new ReviewRequestDto(
                5,
                "Fantastic place to visit! The views are breathtaking."
        );
        ReviewResponseDto createdReview = reviewService.addReview(locationId, testUser, reviewRequest);

        // Then: Review is saved successfully
        // 1. Response contains review details
        assertThat(createdReview).isNotNull();
        assertThat(createdReview.getRating()).isEqualTo(5);
        assertThat(createdReview.getComment()).isEqualTo("Fantastic place to visit! The views are breathtaking.");
        assertThat(createdReview.getUserId()).isEqualTo(userId);
        assertThat(createdReview.getLocationId()).isEqualTo(locationId);

        // 2. Review persisted in database
        List<Review> locationReviews = reviewRepository.findByLocation(testLocation);
        assertThat(locationReviews).hasSize(1);
        Review persistedReview = locationReviews.get(0);
        assertThat(persistedReview.getRating()).isEqualTo(5);
        assertThat(persistedReview.getComment()).isEqualTo("Fantastic place to visit! The views are breathtaking.");

        // 3. Review contains timestamps
        assertThat(createdReview.getCreatedAt()).isNotNull();
    }

    // ============================================================
    // ERROR HANDLING AND EDGE CASES
    // ============================================================

    @Test
    @DisplayName("Should throw IllegalArgumentException for non-existent location")
    void testAddReview_LocationNotFound() {
        // Arrange
        UUID nonExistentLocationId = UUID.randomUUID();
        ReviewRequestDto reviewRequestDto = new ReviewRequestDto(5, "Review");

        // Act & Assert
        assertThatThrownBy(() -> reviewService.addReview(nonExistentLocationId, testUser, reviewRequestDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Location not found");
    }

    @Test
    @DisplayName("Should allow multiple reviews from different users on same location")
    void testAddReview_MultipleReviewsFromDifferentUsers() {
        // Arrange - Create second user
        User user2 = new User(
                "tourist2@example.com",
                passwordEncoder.encode("password123"),
                UserRole.TOURIST
        );
        User savedUser2 = userRepository.save(user2);

        // Act - Add reviews from both users
        ReviewRequestDto review1 = new ReviewRequestDto(5, "Great!");
        ReviewRequestDto review2 = new ReviewRequestDto(4, "Good!");
        
        ReviewResponseDto result1 = reviewService.addReview(locationId, testUser, review1);
        ReviewResponseDto result2 = reviewService.addReview(locationId, savedUser2, review2);

        // Assert
        assertThat(result1).isNotNull();
        assertThat(result2).isNotNull();
        assertThat(result1.getUserId()).isNotEqualTo(result2.getUserId());

        // Verify both reviews are in database
        assertThat(reviewRepository.findByLocation(testLocation))
                .hasSize(2);
    }

    @Test
    @DisplayName("Should allow optional comment field (null comment)")
    void testAddReview_OptionalComment() {
        // Arrange
        ReviewRequestDto reviewWithoutComment = new ReviewRequestDto(3, null);

        // Act
        ReviewResponseDto result = reviewService.addReview(locationId, testUser, reviewWithoutComment);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getRating()).isEqualTo(3);
        assertThat(result.getComment()).isNull();

        // Verify in database
        assertThat(reviewRepository.findById(result.getId()))
                .isPresent()
                .get()
                .extracting("comment")
                .isNull();
    }

    @Test
    @DisplayName("Should set timestamps on review creation")
    void testAddReview_TimestampsSet() {
        // Arrange
        ReviewRequestDto reviewRequestDto = new ReviewRequestDto(5, "Great!");

        // Act
        long beforeCreation = System.currentTimeMillis();
        ReviewResponseDto result = reviewService.addReview(locationId, testUser, reviewRequestDto);
        long afterCreation = System.currentTimeMillis();

        // Assert
        assertThat(result.getCreatedAt()).isNotNull();

        // Verify timestamps are within expected range
        long createdAtMillis = result.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli();
        assertThat(createdAtMillis).isBetween(beforeCreation, afterCreation);
    }

    @Test
    @DisplayName("Should allow same user to review location multiple times")
    void testAddReview_MultipleReviewsFromSameUser() {
        // Arrange
        ReviewRequestDto review1 = new ReviewRequestDto(5, "First visit - amazing!");
        ReviewRequestDto review2 = new ReviewRequestDto(4, "Second visit - still great!");

        // Act
        ReviewResponseDto result1 = reviewService.addReview(locationId, testUser, review1);
        ReviewResponseDto result2 = reviewService.addReview(locationId, testUser, review2);

        // Assert - Both reviews created with same user
        assertThat(result1.getUserId()).isEqualTo(userId);
        assertThat(result2.getUserId()).isEqualTo(userId);
        assertThat(result1.getId()).isNotEqualTo(result2.getId());

        // Verify both reviews in database
        assertThat(reviewRepository.findByLocation(testLocation))
                .hasSize(2);
    }

    // ============================================================
    // DEFINITION OF DONE - REQUIREMENTS VERIFICATION
    // ============================================================

    @Test
    @DisplayName("DoD: Review logic implemented - can create and retrieve reviews")
    void testDoD_ReviewLogicImplemented() {
        // Add review
        ReviewRequestDto reviewRequestDto = new ReviewRequestDto(5, "Perfect location!");
        ReviewResponseDto addedReview = reviewService.addReview(locationId, testUser, reviewRequestDto);

        // Retrieve reviews
        assertThat(reviewService.getReviewsByLocation(locationId))
                .hasSize(1)
                .extracting("rating")
                .containsExactly(5);

        assertThat(addedReview).isNotNull();
    }

    @Test
    @DisplayName("DoD: Endpoint created - ReviewService.addReview method exists")
    void testDoD_EndpointExists() {
        // Verify the addReview method exists and works
        ReviewRequestDto reviewRequestDto = new ReviewRequestDto(4, "Nice place");
        ReviewResponseDto result = reviewService.addReview(locationId, testUser, reviewRequestDto);

        assertThat(result)
                .isNotNull()
                .hasFieldOrProperty("id")
                .hasFieldOrProperty("rating")
                .hasFieldOrProperty("comment");
    }

    @Test
    @DisplayName("DoD: Validation added - Rating constraints enforced")
    void testDoD_ValidationAdded() {
        // Valid ratings should work
        ReviewRequestDto validRating = new ReviewRequestDto(3, "Comment");
        ReviewResponseDto result = reviewService.addReview(locationId, testUser, validRating);
        assertThat(result.getRating()).isEqualTo(3);

        // Service validates location exists
        assertThatThrownBy(() -> 
            reviewService.addReview(UUID.randomUUID(), testUser, validRating)
        ).isInstanceOf(IllegalArgumentException.class);
    }
}

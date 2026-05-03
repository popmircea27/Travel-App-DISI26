package com.example.travelappbe.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.example.travelappbe.dto.ReviewRequestDto;
import com.example.travelappbe.dto.ReviewResponseDto;
import com.example.travelappbe.entity.Location;
import com.example.travelappbe.entity.Review;
import com.example.travelappbe.entity.User;
import com.example.travelappbe.entity.UserRole;
import com.example.travelappbe.repository.LocationRepository;
import com.example.travelappbe.repository.ReviewRepository;

/**
 * Unit tests for ReviewService
 * Tests review creation and retrieval functionality
 * Implements requirements from US4 - SCRUM-47: Add Review feature
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ReviewService Tests (US4 - SCRUM-47)")
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private LocationRepository locationRepository;

    @InjectMocks
    private ReviewService reviewService;

    @Captor
    private ArgumentCaptor<Review> reviewCaptor;

    private UUID locationId;
    private UUID userId;
    private Location testLocation;
    private User testUser;
    private Review testReview;
    private ReviewRequestDto reviewRequestDto;

    @BeforeEach
    void setUp() {
        // Initialize test data
        locationId = UUID.randomUUID();
        userId = UUID.randomUUID();

        // Create test location
        testLocation = new Location(
                "Eiffel Tower",
                "Iconic iron tower in Paris",
                48.8584,
                2.2945
        );
        testLocation.setId(locationId);
        testLocation.setCountry("France");
        testLocation.setCity("Paris");

        // Create test user
        testUser = new User("tourist@example.com", "$2a$10$hashedPassword", UserRole.TOURIST);
        testUser.setId(userId);

        // Create test review
        testReview = new Review();
        testReview.setId(UUID.randomUUID());
        testReview.setRating(5);
        testReview.setComment("Amazing experience!");
        testReview.setLocation(testLocation);
        testReview.setUser(testUser);
        testReview.setCreatedAt(LocalDateTime.now());
        testReview.setUpdatedAt(LocalDateTime.now());

        // Create review request DTO
        reviewRequestDto = new ReviewRequestDto(5, "Amazing experience!");
    }

    // ============================================================
    // ADD REVIEW TESTS
    // ============================================================

    @Test
    @DisplayName("Should successfully add review with valid data")
    void testAddReview_Success() {
        // Arrange
        when(locationRepository.findById(locationId)).thenReturn(Optional.of(testLocation));
        when(reviewRepository.save(any(Review.class))).thenReturn(testReview);

        // Act
        ReviewResponseDto result = reviewService.addReview(locationId, testUser, reviewRequestDto);

        // Assert
        assertNotNull(result);
        assertEquals(5, result.getRating());
        assertEquals("Amazing experience!", result.getComment());
        assertEquals(userId, result.getUserId());
        assertEquals("tourist@example.com", result.getUserEmail());
        assertEquals(locationId, result.getLocationId());
        assertNotNull(result.getId());
        assertNotNull(result.getCreatedAt());

        verify(locationRepository, times(1)).findById(locationId);
        verify(reviewRepository, times(1)).save(reviewCaptor.capture());
        
        Review capturedReview = reviewCaptor.getValue();
        assertEquals(5, capturedReview.getRating());
        assertEquals("Amazing experience!", capturedReview.getComment());
        assertEquals(testLocation, capturedReview.getLocation());
        assertEquals(testUser, capturedReview.getUser());
    }

    @Test
    @DisplayName("Should successfully add review with only rating (no comment)")
    void testAddReview_WithoutComment() {
        // Arrange
        ReviewRequestDto requestWithoutComment = new ReviewRequestDto(4, null);
        Review reviewWithoutComment = new Review();
        reviewWithoutComment.setId(UUID.randomUUID());
        reviewWithoutComment.setRating(4);
        reviewWithoutComment.setComment(null);
        reviewWithoutComment.setLocation(testLocation);
        reviewWithoutComment.setUser(testUser);
        reviewWithoutComment.setCreatedAt(LocalDateTime.now());
        reviewWithoutComment.setUpdatedAt(LocalDateTime.now());

        when(locationRepository.findById(locationId)).thenReturn(Optional.of(testLocation));
        when(reviewRepository.save(any(Review.class))).thenReturn(reviewWithoutComment);

        // Act
        ReviewResponseDto result = reviewService.addReview(locationId, testUser, requestWithoutComment);

        // Assert
        assertNotNull(result);
        assertEquals(4, result.getRating());
        assertEquals(null, result.getComment());
        verify(locationRepository, times(1)).findById(locationId);
        verify(reviewRepository, times(1)).save(any(Review.class));
    }

    @Test
    @DisplayName("Should successfully add review with minimum rating")
    void testAddReview_MinimumRating() {
        // Arrange
        ReviewRequestDto minRatingRequest = new ReviewRequestDto(1, "Poor experience");
        Review minRatingReview = new Review();
        minRatingReview.setId(UUID.randomUUID());
        minRatingReview.setRating(1);
        minRatingReview.setComment("Poor experience");
        minRatingReview.setLocation(testLocation);
        minRatingReview.setUser(testUser);
        minRatingReview.setCreatedAt(LocalDateTime.now());
        minRatingReview.setUpdatedAt(LocalDateTime.now());

        when(locationRepository.findById(locationId)).thenReturn(Optional.of(testLocation));
        when(reviewRepository.save(any(Review.class))).thenReturn(minRatingReview);

        // Act
        ReviewResponseDto result = reviewService.addReview(locationId, testUser, minRatingRequest);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getRating());
        assertEquals("Poor experience", result.getComment());
        verify(reviewRepository, times(1)).save(any(Review.class));
    }

    @Test
    @DisplayName("Should successfully add review with maximum rating")
    void testAddReview_MaximumRating() {
        // Arrange
        ReviewRequestDto maxRatingRequest = new ReviewRequestDto(5, "Excellent!");
        Review maxRatingReview = new Review();
        maxRatingReview.setId(UUID.randomUUID());
        maxRatingReview.setRating(5);
        maxRatingReview.setComment("Excellent!");
        maxRatingReview.setLocation(testLocation);
        maxRatingReview.setUser(testUser);
        maxRatingReview.setCreatedAt(LocalDateTime.now());
        maxRatingReview.setUpdatedAt(LocalDateTime.now());

        when(locationRepository.findById(locationId)).thenReturn(Optional.of(testLocation));
        when(reviewRepository.save(any(Review.class))).thenReturn(maxRatingReview);

        // Act
        ReviewResponseDto result = reviewService.addReview(locationId, testUser, maxRatingRequest);

        // Assert
        assertNotNull(result);
        assertEquals(5, result.getRating());
        assertEquals("Excellent!", result.getComment());
        verify(reviewRepository, times(1)).save(any(Review.class));
    }

    @Test
    @DisplayName("Should throw exception when location not found")
    void testAddReview_LocationNotFound() {
        // Arrange
        when(locationRepository.findById(locationId)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> reviewService.addReview(locationId, testUser, reviewRequestDto)
        );

        assertEquals("Location not found with id: " + locationId, exception.getMessage());
        verify(locationRepository, times(1)).findById(locationId);
        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    @DisplayName("Should link review to user and location correctly")
    void testAddReview_LinkageCorrect() {
        // Arrange
        when(locationRepository.findById(locationId)).thenReturn(Optional.of(testLocation));
        when(reviewRepository.save(any(Review.class))).thenReturn(testReview);

        // Act
        ReviewResponseDto result = reviewService.addReview(locationId, testUser, reviewRequestDto);

        // Assert
        assertEquals(userId, result.getUserId());
        assertEquals("tourist@example.com", result.getUserEmail());
        assertEquals(locationId, result.getLocationId());
        verify(reviewRepository, times(1)).save(any(Review.class));
    }

    // ============================================================
    // GET REVIEWS BY LOCATION TESTS
    // ============================================================

    @Test
    @DisplayName("Should retrieve all reviews for a location")
    void testGetReviewsByLocation_Success() {
        // Arrange
        Review review2 = new Review();
        review2.setId(UUID.randomUUID());
        review2.setRating(4);
        review2.setComment("Good!");
        review2.setLocation(testLocation);
        review2.setUser(testUser);
        review2.setCreatedAt(LocalDateTime.now());
        review2.setUpdatedAt(LocalDateTime.now());

        List<Review> reviews = List.of(testReview, review2);

        when(locationRepository.findById(locationId)).thenReturn(Optional.of(testLocation));
        when(reviewRepository.findByLocation(testLocation)).thenReturn(reviews);

        // Act
        List<ReviewResponseDto> result = reviewService.getReviewsByLocation(locationId);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(5, result.get(0).getRating());
        assertEquals(4, result.get(1).getRating());
        verify(locationRepository, times(1)).findById(locationId);
        verify(reviewRepository, times(1)).findByLocation(testLocation);
    }

    @Test
    @DisplayName("Should return empty list when location has no reviews")
    void testGetReviewsByLocation_Empty() {
        // Arrange
        when(locationRepository.findById(locationId)).thenReturn(Optional.of(testLocation));
        when(reviewRepository.findByLocation(testLocation)).thenReturn(List.of());

        // Act
        List<ReviewResponseDto> result = reviewService.getReviewsByLocation(locationId);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(locationRepository, times(1)).findById(locationId);
        verify(reviewRepository, times(1)).findByLocation(testLocation);
    }

    @Test
    @DisplayName("Should throw exception when getting reviews for non-existent location")
    void testGetReviewsByLocation_LocationNotFound() {
        // Arrange
        when(locationRepository.findById(locationId)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> reviewService.getReviewsByLocation(locationId)
        );

        assertEquals("Location not found with id: " + locationId, exception.getMessage());
        verify(locationRepository, times(1)).findById(locationId);
        verify(reviewRepository, never()).findByLocation(any(Location.class));
    }

    @Test
    @DisplayName("Should map review entity to DTO correctly")
    void testGetReviewsByLocation_DTOMapping() {
        // Arrange
        List<Review> reviews = List.of(testReview);
        when(locationRepository.findById(locationId)).thenReturn(Optional.of(testLocation));
        when(reviewRepository.findByLocation(testLocation)).thenReturn(reviews);

        // Act
        List<ReviewResponseDto> result = reviewService.getReviewsByLocation(locationId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        ReviewResponseDto dto = result.get(0);
        assertEquals(testReview.getId(), dto.getId());
        assertEquals(testReview.getRating(), dto.getRating());
        assertEquals(testReview.getComment(), dto.getComment());
        assertEquals(testUser.getId(), dto.getUserId());
        assertEquals(testUser.getEmail(), dto.getUserEmail());
        assertEquals(testLocation.getId(), dto.getLocationId());
        assertNotNull(dto.getCreatedAt());
        assertNotNull(dto.getUpdatedAt());
    }

    @Test
    @DisplayName("Should retrieve paginated reviews natively for a location")
    void testGetPaginatedReviewsByLocation_Success() {
        // Arrange
        Review review2 = new Review();
        review2.setId(UUID.randomUUID());
        review2.setRating(4);
        review2.setComment("Good!");
        review2.setLocation(testLocation);
        review2.setUser(testUser);
        review2.setCreatedAt(LocalDateTime.now());
        review2.setUpdatedAt(LocalDateTime.now());

        List<Review> reviews = List.of(testReview, review2);
        Pageable pageable = PageRequest.of(0, 10);
        Page<Review> mockPage = new PageImpl<>(reviews, pageable, reviews.size());

        when(locationRepository.findById(locationId)).thenReturn(Optional.of(testLocation));
        when(reviewRepository.findByLocation(testLocation, pageable)).thenReturn(mockPage);

        // Act
        Page<ReviewResponseDto> result = reviewService.getReviewsByLocation(locationId, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertEquals(1, result.getTotalPages());
        assertEquals(2, result.getContent().size());
        assertEquals(5, result.getContent().get(0).getRating());
        verify(locationRepository, times(1)).findById(locationId);
        verify(reviewRepository, times(1)).findByLocation(testLocation, pageable);
    }
}

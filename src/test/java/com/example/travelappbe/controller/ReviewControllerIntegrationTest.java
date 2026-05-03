package com.example.travelappbe.controller;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import com.example.travelappbe.entity.Location;
import com.example.travelappbe.entity.User;
import com.example.travelappbe.entity.UserRole;
import com.example.travelappbe.repository.LocationRepository;
import com.example.travelappbe.repository.UserRepository;
import com.example.travelappbe.security.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for Review endpoints (US4 - SCRUM-47)
 * Tests POST /locations/{locationId}/reviews endpoint
 * 
 * Tests the complete HTTP flow including:
 * - JWT authentication validation
 * - Request validation
 * - Response format
 * - Error handling
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Review Controller Integration Tests (US4 - SCRUM-47)")
class ReviewControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private UUID locationId;
    private UUID userId;
    private String validToken;
    private String invalidToken;

    @BeforeEach
    void setUp() {
        // Create test user
        User user = new User(
                "tourist@example.com",
                passwordEncoder.encode("password123"),
                UserRole.TOURIST
        );
        User savedUser = userRepository.save(user);
        userId = savedUser.getId();
        validToken = jwtTokenProvider.generateToken("tourist@example.com", "TOURIST");

        // Create invalid token
        invalidToken = "invalid_token_xyz";

        // Create test location
        Location location = new Location(
                "Eiffel Tower",
                "The iconic iron lattice tower built in 1889 for the World's Fair",
                48.8584,
                2.2945
        );
        location.setCountry("France");
        location.setCity("Paris");
        location.setImageUrl("https://example.com/eiffel-tower.jpg");
        Location savedLocation = locationRepository.save(location);
        locationId = savedLocation.getId();
    }

    // ============================================================
    // POST /locations/{locationId}/reviews - SUCCESS SCENARIOS
    // ============================================================

    @Test
    @DisplayName("POST /locations/{locationId}/reviews - Should create review with valid data")
    void testAddReview_Success() throws Exception {
        // Arrange
        String reviewJson = objectMapper.writeValueAsString(
                java.util.Map.of("rating", 5, "comment", "Amazing experience!")
        );

        // Act & Assert
        mockMvc.perform(post("/api/locations/{locationId}/reviews", locationId)
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(reviewJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.rating", equalTo(5)))
                .andExpect(jsonPath("$.comment", equalTo("Amazing experience!")))
                .andExpect(jsonPath("$.user_id", equalTo(userId.toString())))
                .andExpect(jsonPath("$.user_email", equalTo("tourist@example.com")))
                .andExpect(jsonPath("$.location_id", equalTo(locationId.toString())))
                .andExpect(jsonPath("$.created_at").exists())
                .andExpect(jsonPath("$.updated_at").exists());
    }

    @Test
    @DisplayName("POST /locations/{locationId}/reviews - Should create review with minimum rating")
    void testAddReview_MinimumRating() throws Exception {
        // Arrange
        String reviewJson = objectMapper.writeValueAsString(
                java.util.Map.of("rating", 1, "comment", "Poor experience")
        );

        // Act & Assert
        mockMvc.perform(post("/api/locations/{locationId}/reviews", locationId)
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(reviewJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rating", equalTo(1)))
                .andExpect(jsonPath("$.comment", equalTo("Poor experience")));
    }

    @Test
    @DisplayName("POST /locations/{locationId}/reviews - Should create review with maximum rating")
    void testAddReview_MaximumRating() throws Exception {
        // Arrange
        String reviewJson = objectMapper.writeValueAsString(
                java.util.Map.of("rating", 5, "comment", "Excellent!")
        );

        // Act & Assert
        mockMvc.perform(post("/api/locations/{locationId}/reviews", locationId)
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(reviewJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rating", equalTo(5)))
                .andExpect(jsonPath("$.comment", equalTo("Excellent!")));
    }

    @Test
    @DisplayName("POST /locations/{locationId}/reviews - Should create review without comment")
    void testAddReview_WithoutComment() throws Exception {
        // Arrange
        String reviewJson = objectMapper.writeValueAsString(
                java.util.Map.of("rating", 4)
        );

        // Act & Assert
        mockMvc.perform(post("/api/locations/{locationId}/reviews", locationId)
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(reviewJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rating", equalTo(4)))
                .andExpect(jsonPath("$.comment").isEmpty());
    }

    // ============================================================
    // POST /locations/{locationId}/reviews - AUTHENTICATION TESTS
    // ============================================================

    @Test
    @DisplayName("POST /locations/{locationId}/reviews - Should reject request without authentication")
    void testAddReview_NoAuthentication() throws Exception {
        // Arrange
        String reviewJson = objectMapper.writeValueAsString(
                java.util.Map.of("rating", 5, "comment", "Review")
        );

        // Act & Assert
        mockMvc.perform(post("/api/locations/{locationId}/reviews", locationId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(reviewJson))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /locations/{locationId}/reviews - Should reject request with invalid token")
    void testAddReview_InvalidToken() throws Exception {
        // Arrange
        String reviewJson = objectMapper.writeValueAsString(
                java.util.Map.of("rating", 5, "comment", "Review")
        );

        // Act & Assert
        mockMvc.perform(post("/api/locations/{locationId}/reviews", locationId)
                .header("Authorization", "Bearer " + invalidToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(reviewJson))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /locations/{locationId}/reviews - Should reject request with malformed token")
    void testAddReview_MalformedToken() throws Exception {
        // Arrange
        String reviewJson = objectMapper.writeValueAsString(
                java.util.Map.of("rating", 5, "comment", "Review")
        );

        // Act & Assert
        mockMvc.perform(post("/api/locations/{locationId}/reviews", locationId)
                .header("Authorization", "InvalidFormat " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(reviewJson))
                .andExpect(status().isUnauthorized());
    }

    // ============================================================
    // POST /locations/{locationId}/reviews - VALIDATION TESTS
    // ============================================================

    @Test
    @DisplayName("POST /locations/{locationId}/reviews - Should reject rating below 1")
    void testAddReview_RatingTooLow() throws Exception {
        // Arrange
        String reviewJson = objectMapper.writeValueAsString(
                java.util.Map.of("rating", 0, "comment", "Bad")
        );

        // Act & Assert
        mockMvc.perform(post("/api/locations/{locationId}/reviews", locationId)
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(reviewJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /locations/{locationId}/reviews - Should reject rating above 5")
    void testAddReview_RatingTooHigh() throws Exception {
        // Arrange
        String reviewJson = objectMapper.writeValueAsString(
                java.util.Map.of("rating", 6, "comment", "Good")
        );

        // Act & Assert
        mockMvc.perform(post("/api/locations/{locationId}/reviews", locationId)
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(reviewJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /locations/{locationId}/reviews - Should reject request without rating")
    void testAddReview_MissingRating() throws Exception {
        // Arrange
        String reviewJson = objectMapper.writeValueAsString(
                java.util.Map.of("comment", "No rating provided")
        );

        // Act & Assert
        mockMvc.perform(post("/api/locations/{locationId}/reviews", locationId)
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(reviewJson))
                .andExpect(status().isBadRequest());
    }

    // ============================================================
    // POST /locations/{locationId}/reviews - ERROR HANDLING
    // ============================================================

    @Test
    @DisplayName("POST /locations/{locationId}/reviews - Should return 404 for non-existent location")
    void testAddReview_LocationNotFound() throws Exception {
        // Arrange
        UUID nonExistentLocationId = UUID.randomUUID();
        String reviewJson = objectMapper.writeValueAsString(
                java.util.Map.of("rating", 5, "comment", "Review")
        );

        // Act & Assert
        mockMvc.perform(post("/api/locations/{locationId}/reviews", nonExistentLocationId)
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(reviewJson))
                .andExpect(status().isNotFound());
    }

    // ============================================================
    // GET /locations/{locationId}/reviews - RETRIEVE REVIEWS
    // ============================================================

    @Test
    @DisplayName("GET /locations/{locationId}/reviews - Should retrieve all reviews for location")
    void testGetReviews_Success() throws Exception {
        // Arrange - Add a review first
        String reviewJson = objectMapper.writeValueAsString(
                java.util.Map.of("rating", 5, "comment", "Great!")
        );
        mockMvc.perform(post("/api/locations/{locationId}/reviews", locationId)
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(reviewJson))
                .andExpect(status().isCreated());

        // Act & Assert - Retrieve reviews
        mockMvc.perform(get("/api/locations/{locationId}/reviews", locationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].rating", equalTo(5)))
                .andExpect(jsonPath("$[0].comment", equalTo("Great!")))
                .andExpect(jsonPath("$[0].user_email", equalTo("tourist@example.com")));
    }

    @Test
    @DisplayName("GET /locations/{locationId}/reviews - Should return empty list for location with no reviews")
    void testGetReviews_Empty() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/locations/{locationId}/reviews", locationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("GET /locations/{locationId}/reviews - Should return 404 for non-existent location")
    void testGetReviews_LocationNotFound() throws Exception {
        // Arrange
        UUID nonExistentLocationId = UUID.randomUUID();

        // Act & Assert
        mockMvc.perform(get("/api/locations/{locationId}/reviews", nonExistentLocationId))
                .andExpect(status().isNotFound());
    }

    // ============================================================
    // FULL SCENARIO TESTS
    // ============================================================

    @Test
    @DisplayName("Full flow: Add review and retrieve it")
    void testFullFlow_AddAndRetrieveReview() throws Exception {
        // Step 1: Add a review
        String reviewJson = objectMapper.writeValueAsString(
                java.util.Map.of("rating", 4, "comment", "Very nice place")
        );
        MvcResult addResult = mockMvc.perform(post("/api/locations/{locationId}/reviews", locationId)
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(reviewJson))
                .andExpect(status().isCreated())
                .andReturn();

        // Step 2: Retrieve reviews for the location
        mockMvc.perform(get("/api/locations/{locationId}/reviews", locationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].rating", equalTo(4)))
                .andExpect(jsonPath("$[0].comment", equalTo("Very nice place")));
    }

    @Test
    @DisplayName("Multiple users can add reviews to same location")
    void testMultipleUsersReviews() throws Exception {
        // Add review from first user
        String review1Json = objectMapper.writeValueAsString(
                java.util.Map.of("rating", 5, "comment", "Amazing!")
        );
        mockMvc.perform(post("/api/locations/{locationId}/reviews", locationId)
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(review1Json))
                .andExpect(status().isCreated());

        // Create second user
        User user2 = new User(
                "tourist2@example.com",
                passwordEncoder.encode("password456"),
                UserRole.TOURIST
        );
        userRepository.save(user2);
        String token2 = jwtTokenProvider.generateToken("tourist2@example.com", "TOURIST");

        // Add review from second user
        String review2Json = objectMapper.writeValueAsString(
                java.util.Map.of("rating", 4, "comment", "Good!")
        );
        mockMvc.perform(post("/api/locations/{locationId}/reviews", locationId)
                .header("Authorization", "Bearer " + token2)
                .contentType(MediaType.APPLICATION_JSON)
                .content(review2Json))
                .andExpect(status().isCreated());

        // Verify both reviews are present
        mockMvc.perform(get("/api/locations/{locationId}/reviews", locationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].rating", equalTo(5)))
                .andExpect(jsonPath("$[1].rating", equalTo(4)));
    }

    @Test
    @DisplayName("User can add multiple reviews to same location")
    void testMultipleReviewsFromSameUser() throws Exception {
        // Add first review
        String review1Json = objectMapper.writeValueAsString(
                java.util.Map.of("rating", 5, "comment", "First visit")
        );
        mockMvc.perform(post("/api/locations/{locationId}/reviews", locationId)
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(review1Json))
                .andExpect(status().isCreated());

        // Add second review from same user
        String review2Json = objectMapper.writeValueAsString(
                java.util.Map.of("rating", 4, "comment", "Second visit")
        );
        mockMvc.perform(post("/api/locations/{locationId}/reviews", locationId)
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(review2Json))
                .andExpect(status().isCreated());

        // Verify both reviews are present
        mockMvc.perform(get("/api/locations/{locationId}/reviews", locationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    @DisplayName("Response contains all required fields")
    void testAddReviewResponse_ContainsAllFields() throws Exception {
        // Arrange
        String reviewJson = objectMapper.writeValueAsString(
                java.util.Map.of("rating", 5, "comment", "Perfect!")
        );

        // Act & Assert - Verify all response fields
        mockMvc.perform(post("/api/locations/{locationId}/reviews", locationId)
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(reviewJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.rating", notNullValue()))
                .andExpect(jsonPath("$.comment", notNullValue()))
                .andExpect(jsonPath("$.user_id", notNullValue()))
                .andExpect(jsonPath("$.user_email", notNullValue()))
                .andExpect(jsonPath("$.location_id", notNullValue()))
                .andExpect(jsonPath("$.created_at", notNullValue()))
                .andExpect(jsonPath("$.updated_at", notNullValue()));
    }
}

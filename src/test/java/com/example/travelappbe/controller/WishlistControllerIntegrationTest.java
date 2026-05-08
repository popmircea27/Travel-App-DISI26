package com.example.travelappbe.controller;

import java.util.UUID;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import com.example.travelappbe.entity.Location;
import com.example.travelappbe.entity.User;
import com.example.travelappbe.entity.UserRole;
import com.example.travelappbe.repository.LocationRepository;
import com.example.travelappbe.repository.UserRepository;
import com.example.travelappbe.security.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.Filter;

/**
 * Integration tests for Wishlist endpoints (US1 - SCRUM-68)
 * Tests CRUD operations for user wishlists
 * 
 * Tests the complete HTTP flow including:
 * - JWT authentication validation
 * - Request validation
 * - Response format
 * - Error handling
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("Wishlist Controller Integration Tests (US1 - SCRUM-68)")
class WishlistControllerIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private Filter springSecurityFilterChain;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private UUID locationId1;
    private UUID locationId2;
    private UUID userId;
    private String validToken;
    private String invalidToken;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .addFilters(springSecurityFilterChain)
                .build();

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

        // Create test locations
        Location location1 = new Location(
                "Eiffel Tower",
                "The iconic iron lattice tower built in 1889 for the World's Fair",
                "Monument",
                20.0,
                "Paris"
        );
        location1.setAdmin(savedUser);
        Location savedLocation1 = locationRepository.save(location1);
        locationId1 = savedLocation1.getId();

        Location location2 = new Location(
                "Statue of Liberty",
                "A colossal neoclassical sculpture located on Liberty Island",
                "Monument",
                25.0,
                "New York"
        );
        location2.setAdmin(savedUser);
        Location savedLocation2 = locationRepository.save(location2);
        locationId2 = savedLocation2.getId();
    }

    // ============================================================
    // POST /api/wishlist - ADD TO WISHLIST - SUCCESS SCENARIOS
    // ============================================================

    @Test
    @DisplayName("POST /api/wishlist - Should add location to wishlist with valid data")
    void testAddToWishlist_Success() throws Exception {
        // Arrange
        String requestJson = objectMapper.writeValueAsString(
                java.util.Map.of("location_id", locationId1)
        );

        // Act & Assert
        mockMvc.perform(post("/api/wishlist")
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.user_id", equalTo(userId.toString())))
                .andExpect(jsonPath("$.location_id", equalTo(locationId1.toString())))
                .andExpect(jsonPath("$.locationName", equalTo("Eiffel Tower")));
    }

    @Test
    @DisplayName("POST /api/wishlist - Should add multiple locations to wishlist")
    void testAddMultipleToWishlist_Success() throws Exception {
        // Add first location
        String requestJson1 = objectMapper.writeValueAsString(
                java.util.Map.of("location_id", locationId1)
        );
        mockMvc.perform(post("/api/wishlist")
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson1))
                .andExpect(status().isCreated());

        // Add second location
        String requestJson2 = objectMapper.writeValueAsString(
                java.util.Map.of("location_id", locationId2)
        );
        mockMvc.perform(post("/api/wishlist")
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson2))
                .andExpect(status().isCreated());

        // Get wishlist and verify both are present
        mockMvc.perform(get("/api/wishlist")
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    // ============================================================
    // POST /api/wishlist - FAILURE SCENARIOS
    // ============================================================

    @Test
    @DisplayName("POST /api/wishlist - Should fail without authorization")
    void testAddToWishlist_Unauthorized() throws Exception {
        // Arrange
        String requestJson = objectMapper.writeValueAsString(
                java.util.Map.of("location_id", locationId1)
        );

        // Act & Assert
        mockMvc.perform(post("/api/wishlist")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /api/wishlist - Should fail with duplicate location")
    void testAddToWishlist_Duplicate() throws Exception {
        // Add location first time
        String requestJson = objectMapper.writeValueAsString(
                java.util.Map.of("location_id", locationId1)
        );
        mockMvc.perform(post("/api/wishlist")
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isCreated());

        // Try to add same location again
        mockMvc.perform(post("/api/wishlist")
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", equalTo("Location already exists in wishlist")));
    }

    @Test
    @DisplayName("POST /api/wishlist - Should fail with invalid location ID")
    void testAddToWishlist_InvalidLocationId() throws Exception {
        // Arrange
        UUID invalidLocationId = UUID.randomUUID();
        String requestJson = objectMapper.writeValueAsString(
                java.util.Map.of("location_id", invalidLocationId)
        );

        // Act & Assert
        mockMvc.perform(post("/api/wishlist")
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @DisplayName("POST /api/wishlist - Should fail with missing location ID")
    void testAddToWishlist_MissingLocationId() throws Exception {
        // Arrange
        String requestJson = objectMapper.writeValueAsString(
                java.util.Map.of()
        );

        // Act & Assert
        mockMvc.perform(post("/api/wishlist")
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isBadRequest());
    }

    // ============================================================
    // GET /api/wishlist - VIEW WISHLIST - SUCCESS SCENARIOS
    // ============================================================

    @Test
    @DisplayName("GET /api/wishlist - Should return empty wishlist for new user")
    void testGetWishlist_Empty() throws Exception {
        mockMvc.perform(get("/api/wishlist")
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("GET /api/wishlist - Should return all items in wishlist")
    void testGetWishlist_WithItems() throws Exception {
        // Add items to wishlist
        String requestJson1 = objectMapper.writeValueAsString(
                java.util.Map.of("location_id", locationId1)
        );
        mockMvc.perform(post("/api/wishlist")
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson1))
                .andExpect(status().isCreated());

        String requestJson2 = objectMapper.writeValueAsString(
                java.util.Map.of("location_id", locationId2)
        );
        mockMvc.perform(post("/api/wishlist")
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson2))
                .andExpect(status().isCreated());

        // Get wishlist
        mockMvc.perform(get("/api/wishlist")
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].locationName").exists())
                .andExpect(jsonPath("$[0].description").exists())
                .andExpect(jsonPath("$[0].category").exists())
                .andExpect(jsonPath("$[0].price").exists());
    }

    // ============================================================
    // GET /api/wishlist - FAILURE SCENARIOS
    // ============================================================

    @Test
    @DisplayName("GET /api/wishlist - Should fail without authorization")
    void testGetWishlist_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/wishlist")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    // ============================================================
    // GET /api/wishlist/check/{locationId} - CHECK IF IN WISHLIST
    // ============================================================

    @Test
    @DisplayName("GET /api/wishlist/check/{locationId} - Should return false for item not in wishlist")
    void testIsInWishlist_NotPresent() throws Exception {
        mockMvc.perform(get("/api/wishlist/check/{locationId}", locationId1)
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.inWishlist", equalTo(false)));
    }

    @Test
    @DisplayName("GET /api/wishlist/check/{locationId} - Should return true for item in wishlist")
    void testIsInWishlist_Present() throws Exception {
        // Add location to wishlist
        String requestJson = objectMapper.writeValueAsString(
                java.util.Map.of("location_id", locationId1)
        );
        mockMvc.perform(post("/api/wishlist")
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isCreated());

        // Check if present
        mockMvc.perform(get("/api/wishlist/check/{locationId}", locationId1)
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.inWishlist", equalTo(true)));
    }

    // ============================================================
    // DELETE /api/wishlist/{locationId} - REMOVE FROM WISHLIST
    // ============================================================

    @Test
    @DisplayName("DELETE /api/wishlist/{locationId} - Should remove location from wishlist")
    void testRemoveFromWishlist_Success() throws Exception {
        // Add location to wishlist
        String requestJson = objectMapper.writeValueAsString(
                java.util.Map.of("location_id", locationId1)
        );
        mockMvc.perform(post("/api/wishlist")
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isCreated());

        // Verify it's in wishlist
        mockMvc.perform(get("/api/wishlist/check/{locationId}", locationId1)
                .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.inWishlist", equalTo(true)));

        // Remove from wishlist
        mockMvc.perform(delete("/api/wishlist/{locationId}", locationId1)
                .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isNoContent());

        // Verify it's no longer in wishlist
        mockMvc.perform(get("/api/wishlist/check/{locationId}", locationId1)
                .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.inWishlist", equalTo(false)));
    }

    @Test
    @DisplayName("DELETE /api/wishlist/{locationId} - Should fail without authorization")
    void testRemoveFromWishlist_Unauthorized() throws Exception {
        mockMvc.perform(delete("/api/wishlist/{locationId}", locationId1))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("DELETE /api/wishlist/{locationId} - Should handle non-existent location gracefully")
    void testRemoveFromWishlist_NonExistent() throws Exception {
        UUID nonExistentId = UUID.randomUUID();
        mockMvc.perform(delete("/api/wishlist/{locationId}", nonExistentId)
                .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isNotFound());
    }
}

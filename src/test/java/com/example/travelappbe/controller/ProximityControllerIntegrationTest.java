package com.example.travelappbe.controller;

import com.example.travelappbe.document.UserLocationDocument;
import com.example.travelappbe.document.UserProfileDocument;
import com.example.travelappbe.entity.Friendship;
import com.example.travelappbe.entity.FriendshipStatus;
import com.example.travelappbe.entity.User;
import com.example.travelappbe.entity.UserRole;
import com.example.travelappbe.repository.FriendshipRepository;
import com.example.travelappbe.repository.UserLocationMongoRepository;
import com.example.travelappbe.repository.UserProfileMongoRepository;
import com.example.travelappbe.repository.UserRepository;
import com.example.travelappbe.security.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.Filter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("Proximity Controller Integration Tests")
class ProximityControllerIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private Filter springSecurityFilterChain;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FriendshipRepository friendshipRepository;

    @Autowired
    private UserProfileMongoRepository userProfileMongoRepository;

    @Autowired
    private UserLocationMongoRepository userLocationMongoRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private User mainUser;
    private User nearbyFriend;
    private User farFriend;
    private User stranger;

    private String mainUserId;
    private String nearbyFriendId;
    private String authToken;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .addFilters(springSecurityFilterChain)
                .build();

        userLocationMongoRepository.deleteAll();
        userProfileMongoRepository.deleteAll();
        friendshipRepository.deleteAll();
        userRepository.deleteAll();

        mainUser = createUser("main.proximity@example.com");
        nearbyFriend = createUser("near.friend@example.com");
        farFriend = createUser("far.friend@example.com");
        stranger = createUser("stranger@example.com");

        mainUserId = mainUser.getId().toString();
        nearbyFriendId = nearbyFriend.getId().toString();

        authToken = jwtTokenProvider.generateToken(mainUser.getEmail(), "TOURIST");

        createProfile(mainUser);
        createProfile(nearbyFriend);
        createProfile(farFriend);
        createProfile(stranger);
    }

    @Test
    @DisplayName("GET /api/proximity/profiles should return all profiles")
    void getAllProfiles_ReturnsAllProfiles() throws Exception {
        mockMvc.perform(get("/api/proximity/profiles")
                        .header("Authorization", bearer()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(4)))
                .andExpect(jsonPath("$[*].email", hasItems(
                        mainUser.getEmail(),
                        nearbyFriend.getEmail(),
                        farFriend.getEmail(),
                        stranger.getEmail()
                )));
    }

    @Test
    @DisplayName("GET /api/proximity/locations should return empty list when no locations exist")
    void getAllLocations_WhenNoLocations_ReturnsEmptyList() throws Exception {
        mockMvc.perform(get("/api/proximity/locations")
                        .header("Authorization", bearer()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("POST /api/proximity/me/location should create current user location")
    void setMyLocation_CreatesLocation() throws Exception {
        mockMvc.perform(post("/api/proximity/me/location")
                        .param("userId", mainUserId)
                        .header("Authorization", bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonLocation(44.4268, 26.1025)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId", equalTo(mainUserId)))
                .andExpect(jsonPath("$.email", equalTo(mainUser.getEmail())))
                .andExpect(jsonPath("$.latitude", equalTo(44.4268)))
                .andExpect(jsonPath("$.longitude", equalTo(26.1025)))
                .andExpect(jsonPath("$.geohash", not(emptyOrNullString())));
    }

    @Test
    @DisplayName("POST /api/proximity/me/location should update existing location, not create duplicate")
    void setMyLocation_WhenLocationExists_UpdatesExistingLocation() throws Exception {
        mockMvc.perform(post("/api/proximity/me/location")
                        .param("userId", mainUserId)
                        .header("Authorization", bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonLocation(44.4268, 26.1025)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/proximity/me/location")
                        .param("userId", mainUserId)
                        .header("Authorization", bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonLocation(45.7489, 21.2087)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.latitude", equalTo(45.7489)))
                .andExpect(jsonPath("$.longitude", equalTo(21.2087)));

        mockMvc.perform(get("/api/proximity/locations")
                        .header("Authorization", bearer()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].userId", equalTo(mainUserId)));
    }

    @Test
    @DisplayName("GET /api/proximity/me/location should return current user location")
    void getMyLocation_WhenExists_ReturnsLocation() throws Exception {
        createLocation(mainUser, 44.4268, 26.1025);

        mockMvc.perform(get("/api/proximity/me/location")
                        .param("userId", mainUserId)
                        .header("Authorization", bearer()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId", equalTo(mainUserId)))
                .andExpect(jsonPath("$.email", equalTo(mainUser.getEmail())))
                .andExpect(jsonPath("$.latitude", equalTo(44.4268)))
                .andExpect(jsonPath("$.longitude", equalTo(26.1025)));
    }

    @Test
    @DisplayName("GET /api/proximity/me/location should return 404 when location does not exist")
    void getMyLocation_WhenMissing_ReturnsNotFound() throws Exception {
        mockMvc.perform(get("/api/proximity/me/location")
                        .param("userId", mainUserId)
                        .header("Authorization", bearer()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/proximity/me/location should reject invalid latitude")
    void setMyLocation_InvalidLatitude_ReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/proximity/me/location")
                        .param("userId", mainUserId)
                        .header("Authorization", bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonLocation(91.0, 26.1025)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/proximity/me/location should reject invalid longitude")
    void setMyLocation_InvalidLongitude_ReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/proximity/me/location")
                        .param("userId", mainUserId)
                        .header("Authorization", bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonLocation(44.4268, 181.0)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/proximity/me/location should return bad request for invalid UUID")
    void setMyLocation_InvalidUserIdFormat_ReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/proximity/me/location")
                        .param("userId", "not-a-uuid")
                        .header("Authorization", bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonLocation(44.4268, 26.1025)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/proximity/me/location should return 404 for non-existing user")
    void setMyLocation_NonExistingUser_ReturnsNotFound() throws Exception {
        mockMvc.perform(post("/api/proximity/me/location")
                        .param("userId", UUID.randomUUID().toString())
                        .header("Authorization", bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonLocation(44.4268, 26.1025)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/proximity/friends/nearby should return bad request when radius is zero")
    void getNearbyFriends_ZeroRadius_ReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/proximity/friends/nearby")
                        .param("userId", mainUserId)
                        .param("radiusKm", "0")
                        .header("Authorization", bearer()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/proximity/friends/nearby should return bad request when radius is negative")
    void getNearbyFriends_NegativeRadius_ReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/proximity/friends/nearby")
                        .param("userId", mainUserId)
                        .param("radiusKm", "-5")
                        .header("Authorization", bearer()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/proximity/friends/nearby should return internal server error when radius is missing")
    void getNearbyFriends_MissingRadius_ReturnsInternalServerError() throws Exception {
        mockMvc.perform(get("/api/proximity/friends/nearby")
                        .param("userId", mainUserId)
                        .header("Authorization", bearer()))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("GET /api/proximity/friends/nearby should return empty list when user has no accepted friends")
    void getNearbyFriends_NoAcceptedFriends_ReturnsEmptyList() throws Exception {
        createLocation(mainUser, 44.4268, 26.1025);
        createLocation(stranger, 44.4270, 26.1030);

        mockMvc.perform(get("/api/proximity/friends/nearby")
                        .param("userId", mainUserId)
                        .param("radiusKm", "10")
                        .header("Authorization", bearer()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("GET /api/proximity/friends/nearby should return only accepted nearby friends")
    void getNearbyFriends_ReturnsOnlyAcceptedNearbyFriends() throws Exception {
        createAcceptedFriendship(mainUser, nearbyFriend);
        createAcceptedFriendship(mainUser, farFriend);

        createLocation(mainUser, 44.4268, 26.1025);
        createLocation(nearbyFriend, 44.4270, 26.1030);
        createLocation(farFriend, 46.7712, 23.6236);
        createLocation(stranger, 44.4271, 26.1031);

        mockMvc.perform(get("/api/proximity/friends/nearby")
                        .param("userId", mainUserId)
                        .param("radiusKm", "5")
                        .header("Authorization", bearer()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].userId", equalTo(nearbyFriendId)))
                .andExpect(jsonPath("$[0].email", equalTo(nearbyFriend.getEmail())))
                .andExpect(jsonPath("$[0].distanceKm").isNumber());
    }

    @Test
    @DisplayName("GET /api/proximity/friends/nearby should not return pending friends")
    void getNearbyFriends_PendingFriendship_IsIgnored() throws Exception {
        createPendingFriendship(mainUser, nearbyFriend);

        createLocation(mainUser, 44.4268, 26.1025);
        createLocation(nearbyFriend, 44.4270, 26.1030);

        mockMvc.perform(get("/api/proximity/friends/nearby")
                        .param("userId", mainUserId)
                        .param("radiusKm", "5")
                        .header("Authorization", bearer()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("GET /api/proximity/friends/nearby should return 404 when current user has no location")
    void getNearbyFriends_CurrentUserHasNoLocation_ReturnsNotFound() throws Exception {
        createAcceptedFriendship(mainUser, nearbyFriend);
        createLocation(nearbyFriend, 44.4270, 26.1030);

        mockMvc.perform(get("/api/proximity/friends/nearby")
                        .param("userId", mainUserId)
                        .param("radiusKm", "5")
                        .header("Authorization", bearer()))
                .andExpect(status().isNotFound());
    }

    private String bearer() {
        return "Bearer " + authToken;
    }

    private User createUser(String email) {
        User user = new User(email, passwordEncoder.encode("password123"), UserRole.TOURIST);
        return userRepository.save(user);
    }

    private void createProfile(User user) {
        LocalDateTime now = LocalDateTime.now();

        UserProfileDocument profile = new UserProfileDocument(
                user.getId().toString(),
                user.getEmail(),
                user.getEmail().split("@")[0],
                "TOURIST",
                now,
                now
        );

        userProfileMongoRepository.save(profile);
    }

    private void createLocation(User user, double latitude, double longitude) {
        LocalDateTime now = LocalDateTime.now();

        UserLocationDocument location = new UserLocationDocument(
                user.getId().toString(),
                user.getEmail(),
                latitude,
                longitude,
                "testhash",
                new GeoJsonPoint(longitude, latitude),
                now
        );

        userLocationMongoRepository.save(location);
    }

    private void createAcceptedFriendship(User requester, User receiver) {
        Friendship friendship = new Friendship(
                requester.getId(),
                receiver.getId(),
                FriendshipStatus.ACCEPTED
        );

        friendshipRepository.save(friendship);
    }

    private void createPendingFriendship(User requester, User receiver) {
        Friendship friendship = new Friendship(
                requester.getId(),
                receiver.getId(),
                FriendshipStatus.PENDING
        );

        friendshipRepository.save(friendship);
    }

    private String jsonLocation(double latitude, double longitude) throws Exception {
        return objectMapper.writeValueAsString(Map.of(
                "latitude", latitude,
                "longitude", longitude
        ));
    }
}
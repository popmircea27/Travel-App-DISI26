package com.example.travelappbe.controller;

import com.example.travelappbe.entity.User;
import com.example.travelappbe.entity.UserRole;
import com.example.travelappbe.repository.NotificationRepository;
import com.example.travelappbe.repository.UserRepository;
import com.example.travelappbe.security.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.Filter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("Notification Controller Integration Tests (US2 - SCRUM-69)")
class NotificationControllerIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private Filter springSecurityFilterChain;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private ObjectMapper objectMapper = new ObjectMapper();

    private String adminToken;
    private String touristToken;
    private UUID touristId;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .addFilters(springSecurityFilterChain)
                .build();

        notificationRepository.deleteAll();

        // Create Admin
        User admin = new User("admin_notif@example.com", passwordEncoder.encode("password123"), UserRole.ADMIN);
        userRepository.save(admin);
        adminToken = jwtTokenProvider.generateToken("admin_notif@example.com", "ADMIN");

        // Create Tourist
        User tourist = new User("tourist_notif@example.com", passwordEncoder.encode("password123"), UserRole.TOURIST);
        userRepository.save(tourist);
        touristId = tourist.getId();
        touristToken = jwtTokenProvider.generateToken("tourist_notif@example.com", "TOURIST");
    }

    @Test
    @DisplayName("POST /api/notifications/broadcast - Admin can broadcast notifications")
    void adminCanBroadcastNotification() throws Exception {
        String requestJson = objectMapper.writeValueAsString(Map.of(
                "title", "Special Summer Offer!",
                "message", "Get 20% off on your next trip."
        ));

        mockMvc.perform(post("/api/notifications/broadcast")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message", equalTo("Notifications broadcasted successfully")));

        // Verify it was saved to DB (at least 2 users exist: admin + tourist)
        org.junit.jupiter.api.Assertions.assertTrue(notificationRepository.count() >= 2);
    }

    @Test
    @DisplayName("POST /api/notifications/broadcast - Tourist is forbidden from broadcasting")
    void touristCannotBroadcastNotification() throws Exception {
        String requestJson = objectMapper.writeValueAsString(Map.of(
                "title", "Fake Offer",
                "message", "This shouldn't work"
        ));

        mockMvc.perform(post("/api/notifications/broadcast")
                .header("Authorization", "Bearer " + touristToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    org.junit.jupiter.api.Assertions.assertTrue(status == 403 || status == 500);
                });
    }

    @Test
    @DisplayName("GET /api/notifications - User can retrieve their notifications")
    void userCanRetrieveNotifications() throws Exception {
        // Create a notification for tourist manually
        User tourist = userRepository.findById(touristId).get();
        com.example.travelappbe.entity.Notification notif = new com.example.travelappbe.entity.Notification(tourist, "Test Title", "Test Message");
        notificationRepository.save(notif);

        mockMvc.perform(get("/api/notifications")
                .header("Authorization", "Bearer " + touristToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title", equalTo("Test Title")))
                .andExpect(jsonPath("$[0].read", equalTo(false)));
    }
}
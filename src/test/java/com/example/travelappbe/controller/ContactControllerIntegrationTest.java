package com.example.travelappbe.controller;

import com.example.travelappbe.entity.User;
import com.example.travelappbe.entity.UserRole;
import com.example.travelappbe.repository.UserRepository;
import com.example.travelappbe.security.JwtTokenProvider;
import com.example.travelappbe.service.EmailService;
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("Contact Controller Integration Tests")
class ContactControllerIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private Filter springSecurityFilterChain;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private EmailService emailService; // Mocking EmailService to avoid sending actual emails

    private final ObjectMapper objectMapper = new ObjectMapper();
    private String validToken;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .addFilters(springSecurityFilterChain)
                .build();

        // Create test user
        User user = new User(
                "contact.user@example.com",
                passwordEncoder.encode("password123"),
                UserRole.TOURIST
        );
        userRepository.save(user);
        validToken = jwtTokenProvider.generateToken("contact.user@example.com", "TOURIST");

        // Mock the email service behavior
        doNothing().when(emailService).sendContactMessage(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("POST /api/contact - Should succeed with valid data and token")
    void testSendMessage_Success() throws Exception {
        // Arrange
        String contactJson = objectMapper.writeValueAsString(
                java.util.Map.of("subject", "Support Request", "message", "My account is locked.")
        );

        // Act & Assert
        mockMvc.perform(post("/api/contact")
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(contactJson))
                .andExpect(status().isNoContent()); // FIX: Expect 204 No Content

        // Verify that the service method was called
        verify(emailService).sendContactMessage("contact.user@example.com", "Support Request", "My account is locked.");
    }

    @Test
    @DisplayName("POST /api/contact - Should return 403 Forbidden without token")
    void testSendMessage_Unauthorized() throws Exception {
        // Arrange
        String contactJson = objectMapper.writeValueAsString(
                java.util.Map.of("subject", "Support Request", "message", "My account is locked.")
        );

        // Act & Assert
        mockMvc.perform(post("/api/contact")
                .contentType(MediaType.APPLICATION_JSON)
                .content(contactJson))
                .andExpect(status().isForbidden());
    }
}
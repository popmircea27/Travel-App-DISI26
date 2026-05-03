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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

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

    @MockitoBean
    private EmailService emailService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private String validToken;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .addFilters(springSecurityFilterChain)
                .build();

        userRepository.deleteAll();
        User user = new User("tourist@example.com", passwordEncoder.encode("password"), UserRole.TOURIST);
        userRepository.save(user);
        validToken = jwtTokenProvider.generateToken("tourist@example.com", "TOURIST");
    }

    @Test
    @DisplayName("POST /contact - Should send message successfully for authenticated user")
    void testSendMessage_Success() throws Exception {
        String contactJson = objectMapper.writeValueAsString(
                java.util.Map.of("subject", "Help Needed", "message", "My booking is not showing up.")
        );

        mockMvc.perform(post("/api/contact")
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(contactJson))
                .andExpect(status().isOk());

        verify(emailService).sendContactMessage("tourist@example.com", "Help Needed", "My booking is not showing up.");
    }

    @Test
    @DisplayName("POST /contact - Should return 403 for unauthenticated user")
    void testSendMessage_NoAuth() throws Exception {
        String contactJson = objectMapper.writeValueAsString(java.util.Map.of("subject", "Test", "message", "Test"));
        mockMvc.perform(post("/api/contact").contentType(MediaType.APPLICATION_JSON).content(contactJson))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /contact - Should return 400 for invalid input (blank subject)")
    void testSendMessage_BlankSubject() throws Exception {
        String contactJson = objectMapper.writeValueAsString(java.util.Map.of("subject", "", "message", "A message."));
        mockMvc.perform(post("/api/contact").header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON).content(contactJson))
                .andExpect(status().isBadRequest());
    }
}
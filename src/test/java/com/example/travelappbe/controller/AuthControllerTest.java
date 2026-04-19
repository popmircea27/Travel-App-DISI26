package com.example.travelappbe.controller;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.example.travelappbe.dto.LoginRequestDto;
import com.example.travelappbe.dto.LoginResponseDto;
import com.example.travelappbe.dto.RegisterRequestDto;
import com.example.travelappbe.dto.RegisterResponseDto;
import com.example.travelappbe.exception.InvalidCredentialsException;
import com.example.travelappbe.exception.UserAlreadyExistsException;
import com.example.travelappbe.service.UserService;

/**
 * Unit tests for AuthController
 * Tests registration, login, and health check endpoints
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthController Tests")
class AuthControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private AuthController authController;

    private RegisterRequestDto registerRequest;
    private LoginRequestDto loginRequest;
    private RegisterResponseDto registerResponse;
    private LoginResponseDto loginResponse;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequestDto("test@example.com", "password123");
        loginRequest = new LoginRequestDto("test@example.com", "password123");
        
        UUID testId = UUID.randomUUID();
        registerResponse = new RegisterResponseDto(testId, "test@example.com", "TOURIST", LocalDateTime.now());
        loginResponse = new LoginResponseDto("jwt_token_12345");
    }

    @Test
    @DisplayName("Should successfully register a new user")
    void testRegister_Success() {
        // Arrange
        when(userService.registerUser(any(RegisterRequestDto.class))).thenReturn(registerResponse);

        // Act
        ResponseEntity<RegisterResponseDto> result = authController.register(registerRequest);

        // Assert
        assertNotNull(result);
        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertEquals("test@example.com", result.getBody().getEmail());
    }

    @Test
    @DisplayName("Should return error when registering with duplicate email")
    void testRegister_DuplicateEmail() {
        // Arrange
        when(userService.registerUser(any(RegisterRequestDto.class)))
                .thenThrow(new UserAlreadyExistsException("User already exists"));

        // Act & Assert
        try {
            authController.register(registerRequest);
        } catch (UserAlreadyExistsException e) {
            assertEquals("User already exists", e.getMessage());
        }
    }

    @Test
    @DisplayName("Should successfully login with correct credentials")
    void testLogin_Success() {
        // Arrange
        when(userService.loginUser(any(LoginRequestDto.class))).thenReturn(loginResponse);

        // Act
        ResponseEntity<LoginResponseDto> result = authController.login(loginRequest);

        // Assert
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals("jwt_token_12345", result.getBody().getToken());
    }

    @Test
    @DisplayName("Should return error when login with wrong credentials")
    void testLogin_InvalidCredentials() {
        // Arrange
        when(userService.loginUser(any(LoginRequestDto.class)))
                .thenThrow(new InvalidCredentialsException("Invalid credentials"));

        // Act & Assert
        try {
            authController.login(loginRequest);
        } catch (InvalidCredentialsException e) {
            assertEquals("Invalid credentials", e.getMessage());
        }
    }

    @Test
    @DisplayName("Should return 200 OK on health check")
    void testHealth_Success() {
        // Act
        ResponseEntity<String> result = authController.health();

        // Assert
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
    }
}

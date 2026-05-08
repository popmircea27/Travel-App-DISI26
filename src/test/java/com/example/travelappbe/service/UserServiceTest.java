package com.example.travelappbe.service;

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
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.travelappbe.dto.LoginRequestDto;
import com.example.travelappbe.dto.LoginResponseDto;
import com.example.travelappbe.dto.RegisterRequestDto;
import com.example.travelappbe.dto.RegisterResponseDto;
import com.example.travelappbe.entity.User;
import com.example.travelappbe.entity.UserRole;
import com.example.travelappbe.exception.InvalidCredentialsException;
import com.example.travelappbe.exception.UserAlreadyExistsException;
import com.example.travelappbe.repository.UserRepository;
import com.example.travelappbe.security.JwtTokenProvider;
import com.example.travelappbe.service.UserProfileSyncService;

/**
 * Unit tests for UserService
 * Tests user registration, login, and profile management
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Tests")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private UserProfileSyncService userProfileSyncService;

    @InjectMocks
    private UserService userService;

    private RegisterRequestDto registerRequest;
    private LoginRequestDto loginRequest;
    private User testUser;
    private UUID testUserId;

    @BeforeEach
    void setUp() {
        // Initialize test data
        testUserId = UUID.randomUUID();
        registerRequest = new RegisterRequestDto("test@example.com", "password123");
        loginRequest = new LoginRequestDto("test@example.com", "password123");
        testUser = new User("test@example.com", "$2a$10$hashedPassword", UserRole.TOURIST);
        testUser.setId(testUserId);
    }

    // ============== Registration Tests ==============

    @Test
    @DisplayName("Should successfully register new user")
    void testRegisterUser_Success() {
        // Arrange
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        RegisterResponseDto response = userService.registerUser(registerRequest);

        // Assert
        assertNotNull(response);
        assertEquals("test@example.com", response.getEmail());
        verify(userRepository, times(1)).existsByEmail("test@example.com");
        verify(passwordEncoder, times(1)).encode("password123");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw exception when registering with duplicate email")
    void testRegisterUser_DuplicateEmail() {
        // Arrange
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        // Act & Assert
        assertThrows(
                UserAlreadyExistsException.class,
                () -> userService.registerUser(registerRequest)
        );

        verify(userRepository, times(1)).existsByEmail("test@example.com");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should hash password before saving during registration")
    void testRegisterUser_PasswordHashing() {
        // Arrange
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("$2a$10$hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        RegisterResponseDto response = userService.registerUser(registerRequest);

        // Assert
        assertNotNull(response);
        verify(passwordEncoder, times(1)).encode("password123");
    }

    // ============== Login Tests ==============

    @Test
    @DisplayName("Should successfully login with correct credentials")
    void testLoginUser_Success() {
        // Arrange
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", "$2a$10$hashedPassword")).thenReturn(true);
        when(jwtTokenProvider.generateToken("test@example.com", "TOURIST")).thenReturn("jwt_token");

        // Act
        LoginResponseDto response = userService.loginUser(loginRequest);

        // Assert
        assertNotNull(response);
        assertEquals("jwt_token", response.getToken());
        verify(userRepository, times(1)).findByEmail("test@example.com");
        verify(passwordEncoder, times(1)).matches("password123", "$2a$10$hashedPassword");
        verify(jwtTokenProvider, times(1)).generateToken("test@example.com", "TOURIST");
    }

    @Test
    @DisplayName("Should throw exception for non-existent user on login")
    void testLoginUser_UserNotFound() {
        // Arrange
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(
                InvalidCredentialsException.class,
                () -> userService.loginUser(loginRequest)
        );

        verify(userRepository, times(1)).findByEmail("test@example.com");
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    @DisplayName("Should throw exception for incorrect password on login")
    void testLoginUser_WrongPassword() {
        // Arrange
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", "$2a$10$hashedPassword")).thenReturn(false);

        // Act & Assert
        assertThrows(
                InvalidCredentialsException.class,
                () -> userService.loginUser(loginRequest)
        );

        verify(userRepository, times(1)).findByEmail("test@example.com");
        verify(passwordEncoder, times(1)).matches("password123", "$2a$10$hashedPassword");
        verify(jwtTokenProvider, never()).generateToken(anyString(), anyString());
    }

    @Test
    @DisplayName("Should generate JWT token with correct role on successful login")
    void testLoginUser_JwtTokenGeneration() {
        // Arrange
        UUID adminUserId = UUID.randomUUID();
        User adminUser = new User("admin@example.com", "$2a$10$hashedPassword", UserRole.ADMIN);
        adminUser.setId(adminUserId);
        LoginRequestDto adminLogin = new LoginRequestDto("admin@example.com", "password123");
        
        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(adminUser));
        when(passwordEncoder.matches("password123", "$2a$10$hashedPassword")).thenReturn(true);
        when(jwtTokenProvider.generateToken("admin@example.com", "ADMIN")).thenReturn("admin_jwt_token");

        // Act
        LoginResponseDto response = userService.loginUser(adminLogin);

        // Assert
        assertNotNull(response);
        assertEquals("admin_jwt_token", response.getToken());
        verify(jwtTokenProvider, times(1)).generateToken("admin@example.com", "ADMIN");
    }

    // ============== User Retrieval Tests ==============

    @Test
    @DisplayName("Should retrieve user by email")
    void testGetUserByEmail() {
        // Arrange
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));

        // Act
        User result = userService.getUserByEmail("test@example.com");

        // Assert
        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
        verify(userRepository, times(1)).findByEmail("test@example.com");
    }

    @Test
    @DisplayName("Should return null when user not found by email")
    void testGetUserByEmail_NotFound() {
        // Arrange
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        // Act
        User result = userService.getUserByEmail("nonexistent@example.com");

        // Assert
        assertEquals(null, result);
        verify(userRepository, times(1)).findByEmail("nonexistent@example.com");
    }

    // ============== Password Verification Tests ==============

    @Test
    @DisplayName("Should verify password matches encoded password")
    void testPasswordMatching_Valid() {
        // Arrange
        when(passwordEncoder.matches("password123", "$2a$10$hashedPassword")).thenReturn(true);

        // Act
        boolean result = passwordEncoder.matches("password123", "$2a$10$hashedPassword");

        // Assert
        assertTrue(result);
    }

    @Test
    @DisplayName("Should verify password does not match different password")
    void testPasswordMatching_Invalid() {
        // Arrange
        when(passwordEncoder.matches("wrongpassword", "$2a$10$hashedPassword")).thenReturn(false);

        // Act
        boolean result = passwordEncoder.matches("wrongpassword", "$2a$10$hashedPassword");

        // Assert
        assertTrue(!result);
    }
}

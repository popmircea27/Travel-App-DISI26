package com.example.travelappbe.service;

import com.example.travelappbe.dto.LoginRequestDto;
import com.example.travelappbe.dto.LoginResponseDto;
import com.example.travelappbe.dto.RegisterRequestDto;
import com.example.travelappbe.dto.RegisterResponseDto;
import com.example.travelappbe.entity.User;
import com.example.travelappbe.exception.InvalidCredentialsException;
import com.example.travelappbe.exception.UserAlreadyExistsException;
import com.example.travelappbe.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Integration tests for Authentication APIs (register and login endpoints)
 * Tests cover:
 * - Successful user registration
 * - Duplicate email validation
 * - Successful user login
 * - Invalid credentials handling
 * - Database persistence
 * - API response validation
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Authentication Integration Tests")
class AuthenticationIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        // Clear database before each test
        userRepository.deleteAll();
    }

    // ============================================================
    // REGISTRATION TESTS
    // ============================================================

    @Test
    @DisplayName("Should successfully register a new user with valid credentials")
    void testRegisterUserSuccessfully() {
        // Arrange
        RegisterRequestDto request = new RegisterRequestDto("test@example.com", "password123");

        // Act
        RegisterResponseDto response = userService.registerUser(request);

        // Assert - Verify response content
        assertThat(response.getEmail()).isEqualTo("test@example.com");
        assertThat(response.getRole()).isEqualTo("TOURIST");
        assertThat(response.getId()).isNotNull();
        assertThat(response.getCreatedAt()).isNotNull();

        // Assert - Verify database persistence
        Optional<User> savedUser = userRepository.findByEmail("test@example.com");
        assertThat(savedUser).isPresent();
        assertThat(savedUser.get().getEmail()).isEqualTo("test@example.com");
        assertThat(savedUser.get().getId()).isNotNull();
    }

    @Test
    @DisplayName("Should throw UserAlreadyExistsException when registering with duplicate email")
    void testRegisterUserWithDuplicateEmail() {
        // Arrange - Register first user
        RegisterRequestDto firstRequest = new RegisterRequestDto("duplicate@example.com", "password123");
        userService.registerUser(firstRequest);

        // Act & Assert - Try to register second user with same email
        RegisterRequestDto secondRequest = new RegisterRequestDto("duplicate@example.com", "differentpassword");
        assertThatThrownBy(() -> userService.registerUser(secondRequest))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("already exists");

        // Assert - Verify only one user exists in database
        assertThat(userRepository.findByEmail("duplicate@example.com")).isPresent();
        assertThat(userRepository.findAll().size()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should successfully persist user data to database on registration")
    void testUserDataPersistence() {
        // Arrange
        RegisterRequestDto request = new RegisterRequestDto("persist@example.com", "password123");

        // Act
        RegisterResponseDto response = userService.registerUser(request);

        // Assert - Verify user is in database with correct data
        Optional<User> savedUser = userRepository.findByEmail("persist@example.com");
        assertThat(savedUser).isPresent();

        User user = savedUser.get();
        assertThat(user.getEmail()).isEqualTo("persist@example.com");
        assertThat(user.getPasswordHash()).isNotEqualTo("password123"); // Password should be hashed
        assertThat(user.getPasswordHash()).isNotBlank();
        assertThat(user.getRole().toString()).isEqualTo("TOURIST");
        assertThat(user.getCreatedAt()).isNotNull();
        assertThat(user.getId()).isEqualTo(response.getId());
    }

    @Test
    @DisplayName("Should handle multiple user registrations and maintain data integrity")
    void testMultipleUserRegistrations() {
        // Arrange
        RegisterRequestDto request1 = new RegisterRequestDto("user1@example.com", "password123");
        RegisterRequestDto request2 = new RegisterRequestDto("user2@example.com", "password456");
        RegisterRequestDto request3 = new RegisterRequestDto("user3@example.com", "password789");

        // Act
        RegisterResponseDto response1 = userService.registerUser(request1);
        RegisterResponseDto response2 = userService.registerUser(request2);
        RegisterResponseDto response3 = userService.registerUser(request3);

        // Assert
        assertThat(response1.getId()).isNotNull();
        assertThat(response2.getId()).isNotNull();
        assertThat(response3.getId()).isNotNull();

        // Verify all users are in database
        assertThat(userRepository.findAll().size()).isEqualTo(3);
        assertThat(userRepository.findByEmail("user1@example.com")).isPresent();
        assertThat(userRepository.findByEmail("user2@example.com")).isPresent();
        assertThat(userRepository.findByEmail("user3@example.com")).isPresent();
    }

    @Test
    @DisplayName("Password should be hashed and not stored in plain text")
    void testPasswordIsHashedNotPlainText() {
        // Arrange
        String plainPassword = "plainTextPassword";
        RegisterRequestDto request = new RegisterRequestDto("hash@example.com", plainPassword);

        // Act
        RegisterResponseDto response = userService.registerUser(request);

        // Assert
        Optional<User> savedUser = userRepository.findByEmail("hash@example.com");
        assertThat(savedUser).isPresent();
        assertThat(savedUser.get().getPasswordHash()).isNotEqualTo(plainPassword);
        assertThat(savedUser.get().getPasswordHash()).doesNotContain(plainPassword);
        assertThat(savedUser.get().getPasswordHash()).isNotBlank();
    }

    // ============================================================
    // LOGIN TESTS
    // ============================================================

    @Test
    @DisplayName("Should successfully login with valid credentials and return JWT token")
    void testLoginUserSuccessfully() {
        // Arrange - Register a user first
        RegisterRequestDto registerRequest = new RegisterRequestDto("login@example.com", "password123");
        userService.registerUser(registerRequest);

        // Act - Login with correct credentials
        LoginRequestDto loginRequest = new LoginRequestDto("login@example.com", "password123");
        LoginResponseDto loginResponse = userService.loginUser(loginRequest);

        // Assert
        assertThat(loginResponse.getToken()).isNotNull();
        assertThat(loginResponse.getToken()).isNotBlank();
        assertThat(loginResponse.getToken()).contains("."); // JWT format check (header.payload.signature)
    }

    @Test
    @DisplayName("Should throw InvalidCredentialsException when login with incorrect password")
    void testLoginUserWithIncorrectPassword() {
        // Arrange - Register a user first
        RegisterRequestDto registerRequest = new RegisterRequestDto("user@example.com", "correctpassword");
        userService.registerUser(registerRequest);

        // Act & Assert - Try to login with wrong password
        LoginRequestDto loginRequest = new LoginRequestDto("user@example.com", "wrongpassword");
        assertThatThrownBy(() -> userService.loginUser(loginRequest))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessageContaining("Invalid email or password");
    }

    @Test
    @DisplayName("Should throw InvalidCredentialsException when login with non-existent email")
    void testLoginUserWithNonExistentEmail() {
        // Arrange
        LoginRequestDto loginRequest = new LoginRequestDto("nonexistent@example.com", "password123");

        // Act & Assert
        assertThatThrownBy(() -> userService.loginUser(loginRequest))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessageContaining("Invalid email or password");
    }

    // ============================================================
    // END-TO-END TESTS
    // ============================================================

    @Test
    @DisplayName("End-to-end flow: Register user and then login successfully")
    void testEndToEndRegisterAndLogin() {
        // Arrange
        String email = "e2e@example.com";
        String password = "password123";
        RegisterRequestDto registerRequest = new RegisterRequestDto(email, password);

        // Act - Register
        RegisterResponseDto registerResponse = userService.registerUser(registerRequest);
        assertThat(registerResponse.getId()).isNotNull();

        // Act - Login
        LoginRequestDto loginRequest = new LoginRequestDto(email, password);
        LoginResponseDto loginResponse = userService.loginUser(loginRequest);

        // Assert
        assertThat(loginResponse.getToken()).isNotNull().isNotBlank();

        // Verify user is in database
        Optional<User> user = userRepository.findByEmail(email);
        assertThat(user).isPresent();
        assertThat(user.get().getId()).isEqualTo(registerResponse.getId());
    }

    // ============================================================
    // DATABASE VERIFICATION TESTS
    // ============================================================

    @Test
    @DisplayName("Should verify user exists by email in database")
    void testUserExistsByEmail() {
        // Arrange
        RegisterRequestDto request = new RegisterRequestDto("exists@example.com", "password123");
        userService.registerUser(request);

        // Act
        boolean exists = userService.userExists("exists@example.com");
        boolean notExists = userService.userExists("notexists@example.com");

        // Assert
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    @DisplayName("Should retrieve user by email from database")
    void testGetUserByEmail() {
        // Arrange
        RegisterRequestDto request = new RegisterRequestDto("retrieve@example.com", "password123");
        RegisterResponseDto response = userService.registerUser(request);

        // Act
        User retrievedUser = userService.getUserByEmail("retrieve@example.com");

        // Assert
        assertThat(retrievedUser).isNotNull();
        assertThat(retrievedUser.getEmail()).isEqualTo("retrieve@example.com");
        assertThat(retrievedUser.getId()).isEqualTo(response.getId());
    }

    // ============================================================
    // SECURITY AND DATA INTEGRITY TESTS
    // ============================================================

    @Test
    @DisplayName("Should maintain user role as TOURIST after registration")
    void testUserRoleIsTourist() {
        // Arrange
        RegisterRequestDto request = new RegisterRequestDto("role@example.com", "password123");

        // Act
        RegisterResponseDto response = userService.registerUser(request);

        // Assert
        assertThat(response.getRole()).isEqualTo("TOURIST");

        Optional<User> savedUser = userRepository.findByEmail("role@example.com");
        assertThat(savedUser).isPresent();
        assertThat(savedUser.get().getRole().toString()).isEqualTo("TOURIST");
    }

    @Test
    @DisplayName("Should have created_at timestamp on registration")
    void testCreatedAtTimestamp() {
        // Arrange
        RegisterRequestDto request = new RegisterRequestDto("timestamp@example.com", "password123");

        // Act
        RegisterResponseDto response = userService.registerUser(request);

        // Assert
        assertThat(response.getCreatedAt()).isNotNull();

        Optional<User> savedUser = userRepository.findByEmail("timestamp@example.com");
        assertThat(savedUser).isPresent();
        assertThat(savedUser.get().getCreatedAt()).isNotNull();
    }

    // ============================================================
    // CONCURRENCY AND STRESS TESTS
    // ============================================================

    @Test
    @DisplayName("Should handle rapid consecutive registrations")
    void testRapidConsecutiveRegistrations() {
        // Act & Assert
        for (int i = 0; i < 10; i++) {
            RegisterRequestDto request = new RegisterRequestDto("rapid" + i + "@example.com", "password123");
            RegisterResponseDto response = userService.registerUser(request);
            assertThat(response.getId()).isNotNull();
        }

        // Verify all users were created
        assertThat(userRepository.findAll().size()).isEqualTo(10);
    }
}

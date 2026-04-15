package com.example.travelappbe.service;

import com.example.travelappbe.dto.RegisterRequestDto;
import com.example.travelappbe.dto.RegisterResponseDto;
import com.example.travelappbe.entity.User;
import com.example.travelappbe.entity.UserRole;
import com.example.travelappbe.exception.UserAlreadyExistsException;
import com.example.travelappbe.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private RegisterRequestDto validRegisterRequest;
    private User testUser;

    @BeforeEach
    void setUp() {
        validRegisterRequest = new RegisterRequestDto("test@example.com", "password123");

        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setEmail("test@example.com");
        testUser.setPasswordHash("$2a$10$hashedPassword");
        testUser.setRole(UserRole.TOURIST);
    }

    @Test
    void testRegisterUserSuccessfully() {
        // Arrange
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("$2a$10$hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        RegisterResponseDto response = userService.registerUser(validRegisterRequest);

        // Assert
        assertNotNull(response);
        assertEquals("test@example.com", response.getEmail());
        assertEquals("TOURIST", response.getRole());
        assertNotNull(response.getId());
        assertNotNull(response.getCreatedAt());

        verify(userRepository, times(1)).existsByEmail("test@example.com");
        verify(passwordEncoder, times(1)).encode("password123");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testRegisterUserWithDuplicateEmail() {
        // Arrange
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        // Act & Assert
        UserAlreadyExistsException exception = assertThrows(
                UserAlreadyExistsException.class,
                () -> userService.registerUser(validRegisterRequest)
        );

        assertTrue(exception.getMessage().contains("already exists"));
        verify(userRepository, times(1)).existsByEmail("test@example.com");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testPasswordIsHashedBeforeSaving() {
        // Arrange
        String plainPassword = "password123";
        String hashedPassword = "$2a$10$hashedPassword";

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(plainPassword)).thenReturn(hashedPassword);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            assertEquals(hashedPassword, savedUser.getPasswordHash());
            return savedUser;
        });

        // Act
        userService.registerUser(validRegisterRequest);

        // Assert
        verify(passwordEncoder, times(1)).encode(plainPassword);
    }

    @Test
    void testUserExistsByEmail() {
        // Arrange
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);
        when(userRepository.existsByEmail("nonexistent@example.com")).thenReturn(false);

        // Act & Assert
        assertTrue(userService.userExists("existing@example.com"));
        assertFalse(userService.userExists("nonexistent@example.com"));

        verify(userRepository, times(2)).existsByEmail(anyString());
    }

    @Test
    void testGetUserByEmail() {
        // Arrange
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // Act & Assert
        User foundUser = userService.getUserByEmail("test@example.com");
        assertNotNull(foundUser);
        assertEquals("test@example.com", foundUser.getEmail());

        User notFoundUser = userService.getUserByEmail("nonexistent@example.com");
        assertNull(notFoundUser);

        verify(userRepository, times(2)).findByEmail(anyString());
    }

    @Test
    void testRegisteredUserHasTouristRoleByDefault() {
        // Arrange
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$hashedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            assertEquals(UserRole.TOURIST, savedUser.getRole());
            return savedUser;
        });

        // Act
        userService.registerUser(validRegisterRequest);

        // Assert
        verify(userRepository, times(1)).save(any(User.class));
    }
}

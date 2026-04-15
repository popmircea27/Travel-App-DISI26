package com.example.travelappbe.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
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

import com.example.travelappbe.dto.RegisterRequestDto;
import com.example.travelappbe.dto.RegisterResponseDto;
import com.example.travelappbe.entity.User;
import com.example.travelappbe.entity.UserRole;
import com.example.travelappbe.exception.UserAlreadyExistsException;
import com.example.travelappbe.repository.UserRepository;
import com.example.travelappbe.security.JwtTokenProvider;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private UserService userService;

    @Test
    void testRegisterUserWithDuplicateEmailThrowsException() {
        // Arrange
        RegisterRequestDto registerRequest = new RegisterRequestDto("test@example.com", "password123");
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
    void testPasswordIsHashedBeforeSaving() {
        // Arrange
        RegisterRequestDto registerRequest = new RegisterRequestDto("test@example.com", "password123");
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$hashedPassword");

        User savedUser = new User("test@example.com", "$2a$10$hashedPassword", UserRole.TOURIST);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // Act
        RegisterResponseDto response = userService.registerUser(registerRequest);

        // Assert
        assertTrue(response.getEmail().equals("test@example.com"));
        verify(passwordEncoder, times(1)).encode("password123");
    }
}


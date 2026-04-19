package com.example.travelappbe.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.travelappbe.dto.LoginRequestDto;
import com.example.travelappbe.dto.LoginResponseDto;
import com.example.travelappbe.dto.RegisterRequestDto;
import com.example.travelappbe.dto.RegisterResponseDto;
import com.example.travelappbe.dto.UserProfileDto;
import com.example.travelappbe.entity.User;
import com.example.travelappbe.entity.UserRole;
import com.example.travelappbe.exception.InvalidCredentialsException;
import com.example.travelappbe.exception.UserAlreadyExistsException;
import com.example.travelappbe.repository.UserRepository;
import com.example.travelappbe.security.JwtTokenProvider;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    /**
     * Registers a new user with the provided email and password.
     *
     * @param registerRequestDto the registration request containing email and password
     * @return RegisterResponseDto with user details
     * @throws UserAlreadyExistsException if a user with the same email already exists
     */
    @Transactional
    public RegisterResponseDto registerUser(RegisterRequestDto registerRequestDto) {
        // Check if user already exists
        if (userRepository.existsByEmail(registerRequestDto.getEmail())) {
            throw new UserAlreadyExistsException(
                    "User with email " + registerRequestDto.getEmail() + " already exists"
            );
        }

        // Hash the password
        String hashedPassword = passwordEncoder.encode(registerRequestDto.getPassword());

        // Create new user
        User user = new User(
                registerRequestDto.getEmail(),
                hashedPassword,
                UserRole.TOURIST
        );

        // Save user to database
        User savedUser = userRepository.save(user);

        // Return response DTO
        return new RegisterResponseDto(
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getRole().toString(),
                savedUser.getCreatedAt()
        );
    }

    /**
     * Checks if a user with the given email exists.
     *
     * @param email the email to check
     * @return true if user exists, false otherwise
     */
    public boolean userExists(String email) {
        return userRepository.existsByEmail(email);
    }

    /**
     * Retrieves a user by email.
     *
     * @param email the email to search for
     * @return User if found, null otherwise
     */
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    /**
     * Authenticates a user with email and password, returning a JWT token.
     *
     * @param loginRequestDto the login request containing email and password
     * @return LoginResponseDto with JWT token (containing email and role)
     * @throws InvalidCredentialsException if user not found or password is incorrect
     */
    @Transactional
    public LoginResponseDto loginUser(LoginRequestDto loginRequestDto) {
        // Find user by email
        User user = userRepository.findByEmail(loginRequestDto.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        // Verify password
        if (!passwordEncoder.matches(loginRequestDto.getPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        // Generate JWT token with email and role
        String token = jwtTokenProvider.generateToken(user.getEmail(), user.getRole().toString());

        // Return login response with only the token
        return new LoginResponseDto(token);
    }

    /**
     * Retrieves the profile of a user by their ID.
     *
     * @param userId the user ID
     * @return UserProfileDto with user details
     * @throws IllegalArgumentException if user not found
     */
    public UserProfileDto getUserProfile(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));
        return convertToProfileDto(user);
    }

    /**
     * Retrieves the profile of the authenticated user by email.
     *
     * @param email the user email
     * @return UserProfileDto with user details
     * @throws InvalidCredentialsException if user not found
     */
    public UserProfileDto getProfileByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidCredentialsException("User not found with this email"));
        return convertToProfileDto(user);
    }

    /**
     * Retrieves all users (ADMIN ONLY).
     *
     * @return List of UserProfileDto
     */
    public List<UserProfileDto> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::convertToProfileDto)
                .collect(Collectors.toList());
    }

    /**
     * Updates the profile of a user by their ID (ADMIN ONLY or self).
     *
     * @param userId the user ID
     * @param profileData the updated profile data
     * @return UserProfileDto with updated user details
     * @throws IllegalArgumentException if user not found
     */
    @Transactional
    public UserProfileDto updateUserProfile(UUID userId, UserProfileDto profileData) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

        // Only allow updating email if it's provided and different
        if (profileData.getEmail() != null && !profileData.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(profileData.getEmail())) {
                throw new UserAlreadyExistsException("Email is already in use");
            }
            user.setEmail(profileData.getEmail());
        }

        User updatedUser = userRepository.save(user);
        return convertToProfileDto(updatedUser);
    }

    /**
     * Convert User entity to UserProfileDto.
     */
    private UserProfileDto convertToProfileDto(User user) {
        return new UserProfileDto(
                user.getId(),
                user.getEmail(),
                user.getRole().toString(),
                user.getCreatedAt()
        );
    }
}

package com.example.travelappbe.controller;

import com.example.travelappbe.dto.UserProfileDto;
import com.example.travelappbe.exception.ErrorResponse;
import com.example.travelappbe.service.UserService;
import com.example.travelappbe.security.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST Controller for user-related operations (profile, user management, etc.)
 */
@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*", maxAge = 3600)
public class UserController {

    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;

    public UserController(UserService userService, JwtTokenProvider jwtTokenProvider) {
        this.userService = userService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    /**
     * Get the profile of the authenticated user.
     *
     * @param request the HTTP request containing the JWT token
     * @return ResponseEntity with the authenticated user's profile
     */
    @GetMapping("/me")
    public ResponseEntity<?> getProfile(HttpServletRequest request) {
        String email = extractEmailFromToken(request);
        if (email == null) {
            ErrorResponse error = new ErrorResponse(HttpStatus.UNAUTHORIZED.value(), "Invalid or missing authentication token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
        UserProfileDto profile = userService.getProfileByEmail(email);
        return ResponseEntity.ok(profile);
    }

    /**
     * Update the profile of the authenticated user.
     *
     * @param request the HTTP request containing the JWT token
     * @param profileData the updated profile data
     * @return ResponseEntity with the updated user profile
     */
    @PutMapping("/me")
    public ResponseEntity<?> updateProfile(HttpServletRequest request, @RequestBody UserProfileDto profileData) {
        String email = extractEmailFromToken(request);
        if (email == null) {
            ErrorResponse error = new ErrorResponse(HttpStatus.UNAUTHORIZED.value(), "Invalid or missing authentication token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
        UserProfileDto existingProfile = userService.getProfileByEmail(email);
        UserProfileDto updatedProfile = userService.updateUserProfile(existingProfile.getId(), profileData);
        return ResponseEntity.ok(updatedProfile);
    }

    /**
     * Get all users (ADMIN ONLY).
     *
     * @return ResponseEntity with list of all user profiles
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserProfileDto>> getAllUsers() {
        List<UserProfileDto> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    /**
     * Get a specific user by ID (ADMIN ONLY).
     *
     * @param id the user ID
     * @return ResponseEntity with the user profile
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getUserById(@PathVariable UUID id) {
        try {
            UserProfileDto user = userService.getUserProfile(id);
            return ResponseEntity.ok(user);
        } catch (IllegalArgumentException e) {
            ErrorResponse error = new ErrorResponse(HttpStatus.NOT_FOUND.value(), "User not found with id: " + id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    /**
     * Extract email from JWT token in the Authorization header.
     *
     * @param request the HTTP request
     * @return email if token is valid, null otherwise
     */
    private String extractEmailFromToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                return jwtTokenProvider.validateAndGetEmail(token);
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }
}

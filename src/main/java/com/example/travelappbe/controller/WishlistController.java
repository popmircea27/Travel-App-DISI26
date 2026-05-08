package com.example.travelappbe.controller;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.travelappbe.dto.WishlistAddRequestDto;
import com.example.travelappbe.dto.WishlistResponseDto;
import com.example.travelappbe.repository.UserRepository;
import com.example.travelappbe.security.JwtTokenProvider;
import com.example.travelappbe.service.WishlistService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/wishlist")
@CrossOrigin(origins = "*", maxAge = 3600)
public class WishlistController {

    private final WishlistService wishlistService;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    public WishlistController(WishlistService wishlistService, JwtTokenProvider jwtTokenProvider,
                            UserRepository userRepository) {
        this.wishlistService = wishlistService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userRepository = userRepository;
    }

    /**
     * Add a location to the user's wishlist
     * POST /api/wishlist
     */
    @PostMapping
    public ResponseEntity<?> addToWishlist(
            HttpServletRequest request,
            @Valid @RequestBody WishlistAddRequestDto dto) {

        UUID userId = extractUserIdFromToken(request);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Unauthorized"));
        }

        try {
            WishlistResponseDto response = wishlistService.addToWishlist(userId, dto.getLocationId());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Remove a location from the user's wishlist
     * DELETE /api/wishlist/{locationId}
     */
    @DeleteMapping("/{locationId}")
    public ResponseEntity<?> removeFromWishlist(
            HttpServletRequest request,
            @PathVariable UUID locationId) {

        UUID userId = extractUserIdFromToken(request);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Unauthorized"));
        }

        try {
            wishlistService.removeFromWishlist(userId, locationId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get all wishlist items for the logged-in user
     * GET /api/wishlist
     */
    @GetMapping
    public ResponseEntity<?> getWishlist(HttpServletRequest request) {

        UUID userId = extractUserIdFromToken(request);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Unauthorized"));
        }

        try {
            List<WishlistResponseDto> wishlist = wishlistService.getWishlist(userId);
            return ResponseEntity.ok(wishlist);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Check if a location is in the user's wishlist
     * GET /api/wishlist/check/{locationId}
     */
    @GetMapping("/check/{locationId}")
    public ResponseEntity<?> isInWishlist(
            HttpServletRequest request,
            @PathVariable UUID locationId) {

        UUID userId = extractUserIdFromToken(request);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Unauthorized"));
        }

        try {
            boolean isInWishlist = wishlistService.isInWishlist(userId, locationId);
            return ResponseEntity.ok(Map.of("inWishlist", isInWishlist));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Extract user ID from JWT token in the Authorization header
     */
    private UUID extractUserIdFromToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                String email = jwtTokenProvider.validateAndGetEmail(token);
                return userRepository.findByEmail(email)
                        .map(user -> user.getId())
                        .orElse(null);
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }
}

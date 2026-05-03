package com.example.travelappbe.controller;

import java.util.List;
import java.util.UUID;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.travelappbe.dto.ReviewRequestDto;
import com.example.travelappbe.dto.ReviewResponseDto;
import com.example.travelappbe.entity.User;
import com.example.travelappbe.security.JwtTokenProvider;
import com.example.travelappbe.service.ReviewService;
import com.example.travelappbe.service.UserService;

@RestController
@RequestMapping("/api/locations")
public class ReviewController {

    private final ReviewService reviewService;
    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;

    public ReviewController(ReviewService reviewService, UserService userService, JwtTokenProvider jwtTokenProvider) {
        this.reviewService = reviewService;
        this.userService = userService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @PostMapping("/{locationId}/reviews")
    public ResponseEntity<ReviewResponseDto> addReview(
            @PathVariable("locationId") String locationIdStr,
            HttpServletRequest request,
            @Valid @RequestBody ReviewRequestDto reviewRequestDto) {

        String email = extractEmailFromToken(request);
        if (email == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            UUID locationId = parseLocationId(locationIdStr);
            User user = userService.getUserByEmail(email);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            ReviewResponseDto response = reviewService.addReview(locationId, user, reviewRequestDto);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{locationId}/reviews")
    public ResponseEntity<List<ReviewResponseDto>> getReviews(
            @PathVariable("locationId") String locationIdStr,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            UUID locationId = parseLocationId(locationIdStr);
            Pageable pageable = PageRequest.of(page, size);
            List<ReviewResponseDto> reviews = reviewService.getReviewsByLocation(locationId, pageable);
            return ResponseEntity.ok(reviews);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

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

    private UUID parseLocationId(String idStr) {
        try {
            return UUID.fromString(idStr);
        } catch (IllegalArgumentException e) {
            if (idStr.matches("\\d+")) {
                String padded = String.format("%012d", Long.parseLong(idStr));
                return UUID.fromString("c0000000-0000-0000-0000-" + padded);
            }
            throw new IllegalArgumentException("Invalid ID format");
        }
    }
}
package com.example.travelappbe.controller;

import java.util.List;
import java.util.UUID;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
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
            @PathVariable UUID locationId,
            HttpServletRequest request,
            @Valid @RequestBody ReviewRequestDto reviewRequestDto) {

        String email = extractEmailFromToken(request);
        if (email == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
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
    public ResponseEntity<Page<ReviewResponseDto>> getReviews(
            @PathVariable UUID locationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<ReviewResponseDto> reviewPage = reviewService.getReviewsByLocation(locationId, pageable);
            return ResponseEntity.ok(reviewPage);
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
}
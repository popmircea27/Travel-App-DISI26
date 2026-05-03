package com.example.travelappbe.controller;

import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.travelappbe.dto.ReviewRequestDto;
import com.example.travelappbe.dto.ReviewResponseDto;
import com.example.travelappbe.entity.User;
import com.example.travelappbe.service.ReviewService;

@RestController
@RequestMapping("/api/locations")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping("/{locationId}/reviews")
    public ResponseEntity<ReviewResponseDto> addReview(
            @PathVariable UUID locationId,
            @AuthenticationPrincipal User user,
            @Valid @RequestBody ReviewRequestDto reviewRequestDto) {
        try {
            ReviewResponseDto response = reviewService.addReview(locationId, user, reviewRequestDto);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{locationId}/reviews")
    public ResponseEntity<List<ReviewResponseDto>> getReviews(@PathVariable UUID locationId) {
        try {
            List<ReviewResponseDto> reviews = reviewService.getReviewsByLocation(locationId);
            return ResponseEntity.ok(reviews);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
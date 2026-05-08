package com.example.travelappbe.controller;

import com.example.travelappbe.dto.NotificationCreateDto;
import com.example.travelappbe.dto.NotificationDto;
import com.example.travelappbe.entity.User;
import com.example.travelappbe.entity.UserRole;
import com.example.travelappbe.repository.UserRepository;
import com.example.travelappbe.security.JwtTokenProvider;
import com.example.travelappbe.service.NotificationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "*", maxAge = 3600)
public class NotificationController {

    private final NotificationService notificationService;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    public NotificationController(NotificationService notificationService, JwtTokenProvider jwtTokenProvider, UserRepository userRepository) {
        this.notificationService = notificationService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userRepository = userRepository;
    }

    @PostMapping("/broadcast")
    public ResponseEntity<?> broadcastNotification(HttpServletRequest request, @Valid @RequestBody NotificationCreateDto dto) {
        User user = getAuthenticatedUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Unauthorized"));
        }
        if (user.getRole() != UserRole.ADMIN) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Admin access required"));
        }

        notificationService.broadcastNotification(dto.getTitle(), dto.getMessage());
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "Notifications broadcasted successfully"));
    }

    @GetMapping
    public ResponseEntity<?> getMyNotifications(HttpServletRequest request) {
        User user = getAuthenticatedUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Unauthorized"));
        }

        List<NotificationDto> notifications = notificationService.getUserNotifications(user.getId());
        return ResponseEntity.ok(notifications);
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<?> markNotificationAsRead(HttpServletRequest request, @PathVariable UUID id) {
        User user = getAuthenticatedUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Unauthorized"));
        }

        try {
            notificationService.markAsRead(id, user.getId());
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    private User getAuthenticatedUser(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                String email = jwtTokenProvider.validateAndGetEmail(token);
                return userRepository.findByEmail(email).orElse(null);
            } catch (Exception ignored) {}
        }
        return null;
    }
}
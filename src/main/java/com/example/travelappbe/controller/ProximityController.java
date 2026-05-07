package com.example.travelappbe.controller;

import com.example.travelappbe.document.UserProfileDocument;
import com.example.travelappbe.dto.NearbyFriendResponseDto;
import com.example.travelappbe.dto.SetLocationRequestDto;
import com.example.travelappbe.dto.UserLocationResponseDto;
import com.example.travelappbe.dto.UserProfileResponseDto;
import com.example.travelappbe.entity.User;
import com.example.travelappbe.exception.UserNotFoundException;
import com.example.travelappbe.repository.UserProfileMongoRepository;
import com.example.travelappbe.service.UserLocationService;
import com.example.travelappbe.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/proximity")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ProximityController {

    private final UserProfileMongoRepository userProfileMongoRepository;
    private final UserLocationService userLocationService;
    private final UserService userService;

    public ProximityController(UserProfileMongoRepository userProfileMongoRepository,
                               UserLocationService userLocationService,
                               UserService userService) {
        this.userProfileMongoRepository = userProfileMongoRepository;
        this.userLocationService = userLocationService;
        this.userService = userService;
    }

    @GetMapping("/profiles")
    public ResponseEntity<List<UserProfileResponseDto>> getAllProfiles() {
        List<UserProfileResponseDto> profiles = userProfileMongoRepository.findAll().stream()
                .map(this::mapToProfileResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(profiles);
    }

    @GetMapping("/locations")
    public ResponseEntity<List<UserLocationResponseDto>> getAllLocations() {
        return ResponseEntity.ok(userLocationService.getAllLocations());
    }

    @GetMapping("/me/location")
    public ResponseEntity<UserLocationResponseDto> getMyLocation(@RequestParam(required = false) String userId) {
        UUID currentUserId = resolveCurrentUserId(userId);
        return ResponseEntity.ok(userLocationService.getMyLocation(currentUserId));
    }

    @PostMapping("/me/location")
    public ResponseEntity<UserLocationResponseDto> setMyLocation(@Valid @RequestBody SetLocationRequestDto requestDto,
                                                                 @RequestParam(required = false) String userId) {
        UUID currentUserId = resolveCurrentUserId(userId);
        return ResponseEntity.ok(userLocationService.setMyLocation(currentUserId, requestDto.getLatitude(), requestDto.getLongitude()));
    }

    @GetMapping("/friends/nearby")
    public ResponseEntity<List<NearbyFriendResponseDto>> getNearbyFriends(@RequestParam double radiusKm,
                                                                          @RequestParam(required = false) String userId) {
        if (radiusKm <= 0) {
            throw new IllegalArgumentException("radiusKm must be greater than 0");
        }
        UUID currentUserId = resolveCurrentUserId(userId);
        return ResponseEntity.ok(userLocationService.getNearbyFriends(currentUserId, radiusKm));
    }

    private UUID resolveCurrentUserId(String userIdParam) {
        if (userIdParam != null && !userIdParam.isBlank()) {
            return UUID.fromString(userIdParam);
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof String) {
                User user = userService.getUserByEmail((String) principal);
                if (user != null) {
                    return user.getId();
                }
            }
        }

        throw new UserNotFoundException("Unable to resolve current user. Provide a valid JWT or ?userId parameter.");
    }

    private UserProfileResponseDto mapToProfileResponse(UserProfileDocument document) {
        return new UserProfileResponseDto(
                document.getUserId(),
                document.getEmail(),
                document.getDisplayName(),
                document.getProfileType(),
                document.getCreatedAt(),
                document.getUpdatedAt()
        );
    }
}

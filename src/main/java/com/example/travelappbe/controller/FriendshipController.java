package com.example.travelappbe.controller;

import com.example.travelappbe.dto.FriendRequestDto;
import com.example.travelappbe.dto.FriendshipResponseDto;
import com.example.travelappbe.entity.User;
import com.example.travelappbe.exception.UserNotFoundException;
import com.example.travelappbe.service.FriendshipService;
import com.example.travelappbe.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/friendships")
@CrossOrigin(origins = "*", maxAge = 3600)
public class FriendshipController {

    private final FriendshipService friendshipService;
    private final UserService userService;

    public FriendshipController(FriendshipService friendshipService, UserService userService) {
        this.friendshipService = friendshipService;
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<FriendshipResponseDto>> getAllFriendships() {
        return ResponseEntity.ok(friendshipService.getAllFriendships());
    }

    @GetMapping("/me")
    public ResponseEntity<List<FriendshipResponseDto>> getMyFriendships(@RequestParam(required = false) String userId) {
        UUID currentUserId = resolveCurrentUserId(userId);
        return ResponseEntity.ok(friendshipService.getMyFriendships(currentUserId));
    }

    @GetMapping("/me/friends")
    public ResponseEntity<List<FriendshipResponseDto>> getMyFriends(@RequestParam(required = false) String userId) {
        UUID currentUserId = resolveCurrentUserId(userId);
        return ResponseEntity.ok(friendshipService.getMyFriends(currentUserId));
    }

    @PostMapping("/request")
    public ResponseEntity<FriendshipResponseDto> sendFriendRequest(@Valid @RequestBody FriendRequestDto requestDto,
                                                                   @RequestParam(required = false) String userId) {
        UUID currentUserId = resolveCurrentUserId(userId);
        FriendshipResponseDto response = friendshipService.sendFriendRequest(currentUserId, requestDto.getReceiverId());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/{friendshipId}/accept")
    public ResponseEntity<FriendshipResponseDto> acceptFriendRequest(@PathVariable UUID friendshipId,
                                                                     @RequestParam(required = false) String userId) {
        UUID currentUserId = resolveCurrentUserId(userId);
        FriendshipResponseDto response = friendshipService.acceptFriendRequest(friendshipId, currentUserId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{friendshipId}/reject")
    public ResponseEntity<FriendshipResponseDto> rejectFriendRequest(@PathVariable UUID friendshipId,
                                                                     @RequestParam(required = false) String userId) {
        UUID currentUserId = resolveCurrentUserId(userId);
        FriendshipResponseDto response = friendshipService.rejectFriendRequest(friendshipId, currentUserId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/block")
    public ResponseEntity<FriendshipResponseDto> blockUser(@Valid @RequestBody FriendRequestDto requestDto,
                                                           @RequestParam(required = false) String userId) {
        UUID currentUserId = resolveCurrentUserId(userId);
        FriendshipResponseDto response = friendshipService.blockUser(currentUserId, requestDto.getReceiverId());
        return ResponseEntity.ok(response);
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
}

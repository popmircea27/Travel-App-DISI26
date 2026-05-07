package com.example.travelappbe.service;

import com.example.travelappbe.dto.FriendshipResponseDto;
import com.example.travelappbe.entity.Friendship;
import com.example.travelappbe.entity.FriendshipStatus;
import com.example.travelappbe.entity.User;
import com.example.travelappbe.exception.FriendshipAlreadyExistsException;
import com.example.travelappbe.exception.FriendshipNotFoundException;
import com.example.travelappbe.exception.InvalidFriendshipActionException;
import com.example.travelappbe.exception.UserNotFoundException;
import com.example.travelappbe.repository.FriendshipRepository;
import com.example.travelappbe.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class FriendshipService {

    private final FriendshipRepository friendshipRepository;
    private final UserRepository userRepository;

    public FriendshipService(FriendshipRepository friendshipRepository, UserRepository userRepository) {
        this.friendshipRepository = friendshipRepository;
        this.userRepository = userRepository;
    }

    public FriendshipResponseDto sendFriendRequest(UUID requesterId, UUID receiverId) {
        if (requesterId.equals(receiverId)) {
            throw new InvalidFriendshipActionException("A user cannot send a friend request to themself");
        }

        User requester = findUser(requesterId);
        User receiver = findUser(receiverId);

        if (friendshipRepository.findByUsers(requesterId, receiverId).isPresent()) {
            throw new FriendshipAlreadyExistsException("Friendship already exists between these users");
        }

        Friendship friendship = new Friendship(requesterId, receiverId, FriendshipStatus.PENDING);
        Friendship saved = friendshipRepository.save(friendship);
        return mapToResponse(saved, requester, receiver);
    }

    public FriendshipResponseDto acceptFriendRequest(UUID friendshipId, UUID receiverId) {
        Friendship friendship = friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new FriendshipNotFoundException("Friendship request not found"));

        if (!friendship.getReceiverId().equals(receiverId)) {
            throw new InvalidFriendshipActionException("Only the receiver can accept the friend request");
        }

        if (friendship.getStatus() != FriendshipStatus.PENDING) {
            throw new InvalidFriendshipActionException("Only pending friendship requests can be accepted");
        }

        friendship.setStatus(FriendshipStatus.ACCEPTED);
        Friendship updated = friendshipRepository.save(friendship);
        return mapToResponse(updated);
    }

    public FriendshipResponseDto rejectFriendRequest(UUID friendshipId, UUID receiverId) {
        Friendship friendship = friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new FriendshipNotFoundException("Friendship request not found"));

        if (!friendship.getReceiverId().equals(receiverId)) {
            throw new InvalidFriendshipActionException("Only the receiver can reject the friend request");
        }

        if (friendship.getStatus() != FriendshipStatus.PENDING) {
            throw new InvalidFriendshipActionException("Only pending friendship requests can be rejected");
        }

        friendship.setStatus(FriendshipStatus.REJECTED);
        Friendship updated = friendshipRepository.save(friendship);
        return mapToResponse(updated);
    }

    public FriendshipResponseDto blockUser(UUID requesterId, UUID receiverId) {
        if (requesterId.equals(receiverId)) {
            throw new InvalidFriendshipActionException("A user cannot block themself");
        }

        findUser(requesterId);
        findUser(receiverId);

        Friendship friendship = friendshipRepository.findByUsers(requesterId, receiverId)
                .orElseGet(() -> new Friendship(requesterId, receiverId, FriendshipStatus.BLOCKED));

        friendship.setRequesterId(requesterId);
        friendship.setReceiverId(receiverId);
        friendship.setStatus(FriendshipStatus.BLOCKED);

        Friendship saved = friendshipRepository.save(friendship);
        return mapToResponse(saved);
    }

    public List<FriendshipResponseDto> getAllFriendships() {
        return friendshipRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<FriendshipResponseDto> getMyFriendships(UUID userId) {
        return friendshipRepository.findByRequesterIdOrReceiverId(userId, userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<FriendshipResponseDto> getMyFriends(UUID userId) {
        return friendshipRepository.findByRequesterIdOrReceiverId(userId, userId).stream()
                .filter(friendship -> friendship.getStatus() == FriendshipStatus.ACCEPTED)
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private User findUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
    }

    private FriendshipResponseDto mapToResponse(Friendship friendship) {
        User requester = findUser(friendship.getRequesterId());
        User receiver = findUser(friendship.getReceiverId());
        return mapToResponse(friendship, requester, receiver);
    }

    private FriendshipResponseDto mapToResponse(Friendship friendship, User requester, User receiver) {
        return new FriendshipResponseDto(
                friendship.getId(),
                friendship.getRequesterId(),
                friendship.getReceiverId(),
                requester.getEmail(),
                receiver.getEmail(),
                friendship.getStatus().name(),
                friendship.getCreatedAt()
        );
    }
}

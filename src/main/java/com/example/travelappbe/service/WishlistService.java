package com.example.travelappbe.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.travelappbe.dto.WishlistResponseDto;
import com.example.travelappbe.entity.Location;
import com.example.travelappbe.entity.User;
import com.example.travelappbe.entity.Wishlist;
import com.example.travelappbe.repository.LocationRepository;
import com.example.travelappbe.repository.UserRepository;
import com.example.travelappbe.repository.WishlistRepository;

@Service
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final UserRepository userRepository;
    private final LocationRepository locationRepository;

    public WishlistService(WishlistRepository wishlistRepository, UserRepository userRepository,
                          LocationRepository locationRepository) {
        this.wishlistRepository = wishlistRepository;
        this.userRepository = userRepository;
        this.locationRepository = locationRepository;
    }

    /**
     * Add a location to the user's wishlist
     */
    @Transactional
    public WishlistResponseDto addToWishlist(UUID userId, UUID locationId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        Location location = locationRepository.findById(locationId)
                .orElseThrow(() -> new IllegalArgumentException("Location not found with ID: " + locationId));

        // Check if already exists
        if (wishlistRepository.existsByUserAndLocation(user, location)) {
            throw new IllegalArgumentException("Location already exists in wishlist");
        }

        Wishlist wishlist = new Wishlist(user, location);
        Wishlist savedWishlist = wishlistRepository.save(wishlist);

        return convertToDto(savedWishlist);
    }

    /**
     * Remove a location from the user's wishlist
     */
    @Transactional
    public void removeFromWishlist(UUID userId, UUID locationId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        Location location = locationRepository.findById(locationId)
                .orElseThrow(() -> new IllegalArgumentException("Location not found with ID: " + locationId));

        wishlistRepository.deleteByUserAndLocation(user, location);
    }

    /**
     * Get all wishlist items for a user
     */
    @Transactional(readOnly = true)
    public List<WishlistResponseDto> getWishlist(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        List<Wishlist> wishlists = wishlistRepository.findByUser(user);
        return wishlists.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Check if a location is in the user's wishlist
     */
    @Transactional(readOnly = true)
    public boolean isInWishlist(UUID userId, UUID locationId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        Location location = locationRepository.findById(locationId)
                .orElseThrow(() -> new IllegalArgumentException("Location not found with ID: " + locationId));

        return wishlistRepository.existsByUserAndLocation(user, location);
    }

    /**
     * Convert Wishlist entity to WishlistResponseDto
     */
    private WishlistResponseDto convertToDto(Wishlist wishlist) {
        return new WishlistResponseDto(
                wishlist.getId(),
                wishlist.getUser().getId(),
                wishlist.getUser().getEmail(),
                wishlist.getLocation().getId(),
                wishlist.getLocation().getName(),
                wishlist.getLocation().getDescription(),
                wishlist.getLocation().getCategory(),
                wishlist.getLocation().getPrice(),
                wishlist.getLocation().getLocationName(),
                wishlist.getCreatedAt(),
                wishlist.getUpdatedAt()
        );
    }
}

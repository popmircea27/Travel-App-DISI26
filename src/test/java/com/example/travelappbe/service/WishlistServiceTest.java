package com.example.travelappbe.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.travelappbe.dto.WishlistResponseDto;
import com.example.travelappbe.entity.Location;
import com.example.travelappbe.entity.User;
import com.example.travelappbe.entity.UserRole;
import com.example.travelappbe.entity.Wishlist;
import com.example.travelappbe.repository.LocationRepository;
import com.example.travelappbe.repository.UserRepository;
import com.example.travelappbe.repository.WishlistRepository;

/**
 * Unit tests for WishlistService
 * Tests business logic for wishlist management operations
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Wishlist Service Unit Tests (US1 - SCRUM-68)")
class WishlistServiceTest {

    @Mock
    private WishlistRepository wishlistRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private LocationRepository locationRepository;

    @InjectMocks
    private WishlistService wishlistService;

    private UUID userId;
    private UUID locationId;
    private User testUser;
    private Location testLocation;
    private Wishlist testWishlist;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        locationId = UUID.randomUUID();

        // Create test user
        testUser = new User("test@example.com", "hashedPassword", UserRole.TOURIST);
        testUser.setId(userId);

        // Create test location
        testLocation = new Location("Test Location", "Description", "Category", 10.0, "City");
        testLocation.setId(locationId);

        // Create test wishlist
        testWishlist = new Wishlist(testUser, testLocation);
    }

    // ============================================================
    // ADD TO WISHLIST - SUCCESS SCENARIOS
    // ============================================================

    @Test
    @DisplayName("addToWishlist - Should successfully add location to wishlist")
    void testAddToWishlist_Success() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(locationRepository.findById(locationId)).thenReturn(Optional.of(testLocation));
        when(wishlistRepository.existsByUserAndLocation(testUser, testLocation)).thenReturn(false);
        when(wishlistRepository.save(any(Wishlist.class))).thenReturn(testWishlist);

        // Act
        WishlistResponseDto result = wishlistService.addToWishlist(userId, locationId);

        // Assert
        assertEquals(testUser.getId(), result.getUserId());
        assertEquals(testLocation.getId(), result.getLocationId());
        verify(wishlistRepository, times(1)).save(any(Wishlist.class));
    }

    // ============================================================
    // ADD TO WISHLIST - FAILURE SCENARIOS
    // ============================================================

    @Test
    @DisplayName("addToWishlist - Should throw exception for non-existent user")
    void testAddToWishlist_UserNotFound() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            wishlistService.addToWishlist(userId, locationId);
        });
        verify(wishlistRepository, never()).save(any());
    }

    @Test
    @DisplayName("addToWishlist - Should throw exception for non-existent location")
    void testAddToWishlist_LocationNotFound() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(locationRepository.findById(locationId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            wishlistService.addToWishlist(userId, locationId);
        });
        verify(wishlistRepository, never()).save(any());
    }

    @Test
    @DisplayName("addToWishlist - Should throw exception if location already in wishlist")
    void testAddToWishlist_AlreadyExists() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(locationRepository.findById(locationId)).thenReturn(Optional.of(testLocation));
        when(wishlistRepository.existsByUserAndLocation(testUser, testLocation)).thenReturn(true);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            wishlistService.addToWishlist(userId, locationId);
        });
        verify(wishlistRepository, never()).save(any());
    }

    // ============================================================
    // REMOVE FROM WISHLIST - SUCCESS SCENARIOS
    // ============================================================

    @Test
    @DisplayName("removeFromWishlist - Should successfully remove location from wishlist")
    void testRemoveFromWishlist_Success() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(locationRepository.findById(locationId)).thenReturn(Optional.of(testLocation));

        // Act
        wishlistService.removeFromWishlist(userId, locationId);

        // Assert
        verify(wishlistRepository, times(1)).deleteByUserAndLocation(testUser, testLocation);
    }

    // ============================================================
    // REMOVE FROM WISHLIST - FAILURE SCENARIOS
    // ============================================================

    @Test
    @DisplayName("removeFromWishlist - Should throw exception for non-existent user")
    void testRemoveFromWishlist_UserNotFound() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            wishlistService.removeFromWishlist(userId, locationId);
        });
        verify(wishlistRepository, never()).deleteByUserAndLocation(any(), any());
    }

    @Test
    @DisplayName("removeFromWishlist - Should throw exception for non-existent location")
    void testRemoveFromWishlist_LocationNotFound() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(locationRepository.findById(locationId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            wishlistService.removeFromWishlist(userId, locationId);
        });
        verify(wishlistRepository, never()).deleteByUserAndLocation(any(), any());
    }

    // ============================================================
    // GET WISHLIST - SUCCESS SCENARIOS
    // ============================================================

    @Test
    @DisplayName("getWishlist - Should return empty list for user with no wishlisted items")
    void testGetWishlist_Empty() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(wishlistRepository.findByUser(testUser)).thenReturn(List.of());

        // Act
        List<WishlistResponseDto> result = wishlistService.getWishlist(userId);

        // Assert
        assertEquals(0, result.size());
        verify(wishlistRepository, times(1)).findByUser(testUser);
    }

    @Test
    @DisplayName("getWishlist - Should return all wishlisted items for user")
    void testGetWishlist_WithItems() {
        // Arrange
        List<Wishlist> wishlists = List.of(testWishlist);
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(wishlistRepository.findByUser(testUser)).thenReturn(wishlists);

        // Act
        List<WishlistResponseDto> result = wishlistService.getWishlist(userId);

        // Assert
        assertEquals(1, result.size());
        assertEquals(testLocation.getId(), result.get(0).getLocationId());
        verify(wishlistRepository, times(1)).findByUser(testUser);
    }

    // ============================================================
    // GET WISHLIST - FAILURE SCENARIOS
    // ============================================================

    @Test
    @DisplayName("getWishlist - Should throw exception for non-existent user")
    void testGetWishlist_UserNotFound() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            wishlistService.getWishlist(userId);
        });
        verify(wishlistRepository, never()).findByUser(any());
    }

    // ============================================================
    // IS IN WISHLIST - SUCCESS SCENARIOS
    // ============================================================

    @Test
    @DisplayName("isInWishlist - Should return true if location is in wishlist")
    void testIsInWishlist_Present() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(locationRepository.findById(locationId)).thenReturn(Optional.of(testLocation));
        when(wishlistRepository.existsByUserAndLocation(testUser, testLocation)).thenReturn(true);

        // Act
        boolean result = wishlistService.isInWishlist(userId, locationId);

        // Assert
        assertTrue(result);
        verify(wishlistRepository, times(1)).existsByUserAndLocation(testUser, testLocation);
    }

    @Test
    @DisplayName("isInWishlist - Should return false if location is not in wishlist")
    void testIsInWishlist_NotPresent() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(locationRepository.findById(locationId)).thenReturn(Optional.of(testLocation));
        when(wishlistRepository.existsByUserAndLocation(testUser, testLocation)).thenReturn(false);

        // Act
        boolean result = wishlistService.isInWishlist(userId, locationId);

        // Assert
        assertFalse(result);
        verify(wishlistRepository, times(1)).existsByUserAndLocation(testUser, testLocation);
    }

    // ============================================================
    // IS IN WISHLIST - FAILURE SCENARIOS
    // ============================================================

    @Test
    @DisplayName("isInWishlist - Should throw exception for non-existent user")
    void testIsInWishlist_UserNotFound() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            wishlistService.isInWishlist(userId, locationId);
        });
        verify(wishlistRepository, never()).existsByUserAndLocation(any(), any());
    }

    @Test
    @DisplayName("isInWishlist - Should throw exception for non-existent location")
    void testIsInWishlist_LocationNotFound() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(locationRepository.findById(locationId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            wishlistService.isInWishlist(userId, locationId);
        });
        verify(wishlistRepository, never()).existsByUserAndLocation(any(), any());
    }
}

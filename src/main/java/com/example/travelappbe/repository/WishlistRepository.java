package com.example.travelappbe.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.travelappbe.entity.Location;
import com.example.travelappbe.entity.User;
import com.example.travelappbe.entity.Wishlist;

@Repository
public interface WishlistRepository extends JpaRepository<Wishlist, UUID> {
    
    /**
     * Find all wishlist items for a specific user
     */
    List<Wishlist> findByUser(User user);
    
    /**
     * Check if a location exists in the wishlist for a specific user
     */
    Optional<Wishlist> findByUserAndLocation(User user, Location location);
    
    /**
     * Check if a user has a specific location in their wishlist
     */
    boolean existsByUserAndLocation(User user, Location location);
    
    /**
     * Delete a specific wishlist item for a user
     */
    void deleteByUserAndLocation(User user, Location location);
}

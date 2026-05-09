package com.example.travelappbe.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.travelappbe.entity.Location;
import com.example.travelappbe.entity.Review;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface ReviewRepository extends JpaRepository<Review, UUID> {
    List<Review> findByLocation(Location location);
    Page<Review> findByLocation(Location location, Pageable pageable);
    
    // Analytics queries
    @Query("SELECT COUNT(r) FROM Review r WHERE r.location = :location")
    long countByLocation(@Param("location") Location location);
    
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.location = :location")
    Double getAverageRatingByLocation(@Param("location") Location location);
    
    @Query("SELECT COUNT(r) FROM Review r")
    long countTotalReviews();
    
    List<Review> findAll();
}




package com.example.travelappbe.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.travelappbe.entity.Location;
import com.example.travelappbe.entity.Review;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface ReviewRepository extends JpaRepository<Review, UUID> {
    List<Review> findByLocation(Location location);
    Page<Review> findByLocation(Location location, Pageable pageable);
}




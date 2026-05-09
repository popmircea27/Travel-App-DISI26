package com.example.travelappbe.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.travelappbe.entity.Location;

@Repository
public interface LocationRepository extends JpaRepository<Location, UUID> {
    boolean existsByName(String name);
    
    // Analytics queries
    @Query("SELECT COUNT(DISTINCT l.category) FROM Location l")
    long countDistinctCategories();
    
    @Query("SELECT COUNT(l) FROM Location l WHERE l.category = :category")
    long countByCategory(@Param("category") String category);
    
    @Query("SELECT l.category FROM Location l GROUP BY l.category")
    List<String> findAllCategories();
    
    List<Location> findByCategory(String category);
}

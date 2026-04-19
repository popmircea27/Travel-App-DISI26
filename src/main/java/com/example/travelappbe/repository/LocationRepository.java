package com.example.travelappbe.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.travelappbe.entity.Location;

@Repository
public interface LocationRepository extends JpaRepository<Location, UUID> {
    boolean existsByName(String name);
}

package com.example.travelappbe.repository;

import com.example.travelappbe.entity.AnalyticsVisit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/** Repository for storing and retrieving analytics visits. */
@Repository
public interface AnalyticsVisitRepository extends JpaRepository<AnalyticsVisit, UUID> {
    long countByObjectiveId(UUID objectiveId);
}
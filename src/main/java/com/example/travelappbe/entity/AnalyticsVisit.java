package com.example.travelappbe.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

/** Entity for storing objective analytics visits. */
@Entity
@Table(name = "analytics_visits")
public class AnalyticsVisit {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "objective_id", nullable = false)
    private UUID objectiveId;

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "visit_timestamp", nullable = false)
    private LocalDateTime visitTimestamp;

    public AnalyticsVisit() {
        this.visitTimestamp = LocalDateTime.now();
    }

    public AnalyticsVisit(UUID objectiveId, UUID userId) {
        this.objectiveId = objectiveId;
        this.userId = userId;
        this.visitTimestamp = LocalDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getObjectiveId() {
        return objectiveId;
    }

    public void setObjectiveId(UUID objectiveId) {
        this.objectiveId = objectiveId;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public LocalDateTime getVisitTimestamp() {
        return visitTimestamp;
    }

    public void setVisitTimestamp(LocalDateTime visitTimestamp) {
        this.visitTimestamp = visitTimestamp;
    }
}
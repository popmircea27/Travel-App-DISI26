package com.example.travelappbe.dto;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonAlias;

import jakarta.validation.constraints.NotNull;

public class NewVisitRequestDto {

    @NotNull(message = "objective ID is required")
    @JsonAlias({"objective_id", "objectiveId"})
    private UUID objectiveId;

    @JsonAlias({"user_id", "userId"})
    private UUID userId;

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
}
package com.example.travelappbe.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public class FriendRequestDto {

    @NotNull(message = "receiverId is required")
    private UUID receiverId;

    public FriendRequestDto() {
    }

    public FriendRequestDto(UUID receiverId) {
        this.receiverId = receiverId;
    }

    public UUID getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(UUID receiverId) {
        this.receiverId = receiverId;
    }
}

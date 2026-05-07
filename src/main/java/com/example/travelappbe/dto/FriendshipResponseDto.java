package com.example.travelappbe.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public class FriendshipResponseDto {

    private UUID id;
    private UUID requesterId;
    private UUID receiverId;
    private String requesterEmail;
    private String receiverEmail;
    private String status;
    private LocalDateTime createdAt;

    public FriendshipResponseDto() {
    }

    public FriendshipResponseDto(UUID id, UUID requesterId, UUID receiverId, String requesterEmail, String receiverEmail, String status, LocalDateTime createdAt) {
        this.id = id;
        this.requesterId = requesterId;
        this.receiverId = receiverId;
        this.requesterEmail = requesterEmail;
        this.receiverEmail = receiverEmail;
        this.status = status;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getRequesterId() {
        return requesterId;
    }

    public void setRequesterId(UUID requesterId) {
        this.requesterId = requesterId;
    }

    public UUID getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(UUID receiverId) {
        this.receiverId = receiverId;
    }

    public String getRequesterEmail() {
        return requesterEmail;
    }

    public void setRequesterEmail(String requesterEmail) {
        this.requesterEmail = requesterEmail;
    }

    public String getReceiverEmail() {
        return receiverEmail;
    }

    public void setReceiverEmail(String receiverEmail) {
        this.receiverEmail = receiverEmail;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

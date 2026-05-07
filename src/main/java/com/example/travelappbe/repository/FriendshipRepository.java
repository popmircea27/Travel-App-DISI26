package com.example.travelappbe.repository;

import com.example.travelappbe.entity.Friendship;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FriendshipRepository extends JpaRepository<Friendship, UUID> {

    List<Friendship> findAll();

    List<Friendship> findByRequesterId(UUID requesterId);

    List<Friendship> findByReceiverId(UUID receiverId);

    List<Friendship> findByRequesterIdOrReceiverId(UUID userId1, UUID userId2);

    Optional<Friendship> findByRequesterIdAndReceiverId(UUID requesterId, UUID receiverId);

    @Query("SELECT f FROM Friendship f WHERE (f.requesterId = :userId1 AND f.receiverId = :userId2) OR (f.requesterId = :userId2 AND f.receiverId = :userId1)")
    Optional<Friendship> findByUsers(@Param("userId1") UUID userId1, @Param("userId2") UUID userId2);
}

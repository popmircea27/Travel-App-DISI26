package com.example.travelappbe.repository;

import com.example.travelappbe.document.UserLocationDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserLocationMongoRepository extends MongoRepository<UserLocationDocument, String> {
    Optional<UserLocationDocument> findByUserId(String userId);
    List<UserLocationDocument> findByUserIdIn(List<String> userIds);
}

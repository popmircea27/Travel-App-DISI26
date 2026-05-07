package com.example.travelappbe.repository;

import com.example.travelappbe.document.UserProfileDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserProfileMongoRepository extends MongoRepository<UserProfileDocument, String> {
    Optional<UserProfileDocument> findByUserId(String userId);
    boolean existsByUserId(String userId);
}

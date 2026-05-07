package com.example.travelappbe.service;

import com.example.travelappbe.document.UserProfileDocument;
import com.example.travelappbe.entity.User;
import com.example.travelappbe.repository.UserProfileMongoRepository;
import com.example.travelappbe.repository.UserRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserProfileSyncService {

    private final UserRepository userRepository;
    private final UserProfileMongoRepository userProfileMongoRepository;

    public UserProfileSyncService(UserRepository userRepository, UserProfileMongoRepository userProfileMongoRepository) {
        this.userRepository = userRepository;
        this.userProfileMongoRepository = userProfileMongoRepository;
    }

    @Transactional
    public void createProfileForUser(User user) {
        String userId = user.getId().toString();
        if (userProfileMongoRepository.existsByUserId(userId)) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        UserProfileDocument profile = new UserProfileDocument(
                userId,
                user.getEmail(),
                user.getEmail(),
                "TOURIST_PROFILE",
                now,
                now
        );

        userProfileMongoRepository.save(profile);
    }

    @Scheduled(fixedRateString = "300000")
    public void syncMissingProfiles() {
        List<User> users = userRepository.findAll();
        users.forEach(this::createProfileForUser);
    }
}

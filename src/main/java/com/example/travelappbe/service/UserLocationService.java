package com.example.travelappbe.service;

import com.example.travelappbe.document.UserLocationDocument;
import com.example.travelappbe.dto.NearbyFriendResponseDto;
import com.example.travelappbe.dto.UserLocationResponseDto;
import com.example.travelappbe.entity.Friendship;
import com.example.travelappbe.entity.FriendshipStatus;
import com.example.travelappbe.entity.User;
import com.example.travelappbe.exception.InvalidFriendshipActionException;
import com.example.travelappbe.exception.UserLocationNotFoundException;
import com.example.travelappbe.exception.UserNotFoundException;
import com.example.travelappbe.repository.FriendshipRepository;
import com.example.travelappbe.repository.UserLocationMongoRepository;
import com.example.travelappbe.repository.UserRepository;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.geo.Metrics;
import org.springframework.data.geo.Point;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.NearQuery;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserLocationService {

    private final UserLocationMongoRepository userLocationMongoRepository;
    private final UserRepository userRepository;
    private final FriendshipRepository friendshipRepository;
    private final GeoHashService geoHashService;
    private final MongoTemplate mongoTemplate;

    public UserLocationService(UserLocationMongoRepository userLocationMongoRepository,
                               UserRepository userRepository,
                               FriendshipRepository friendshipRepository,
                               GeoHashService geoHashService,
                               MongoTemplate mongoTemplate) {
        this.userLocationMongoRepository = userLocationMongoRepository;
        this.userRepository = userRepository;
        this.friendshipRepository = friendshipRepository;
        this.geoHashService = geoHashService;
        this.mongoTemplate = mongoTemplate;
    }

    public UserLocationResponseDto setMyLocation(UUID userId, double latitude, double longitude) {
        validateLatitude(latitude);
        validateLongitude(longitude);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));

        String userIdString = userId.toString();
        LocalDateTime now = LocalDateTime.now();
        String geohash = geoHashService.encode(latitude, longitude);
        GeoJsonPoint coordinates = new GeoJsonPoint(longitude, latitude);

        UserLocationDocument location = userLocationMongoRepository.findByUserId(userIdString)
                .map(existing -> {
                    existing.setLatitude(latitude);
                    existing.setLongitude(longitude);
                    existing.setGeohash(geohash);
                    existing.setCoordinates(coordinates);
                    existing.setUpdatedAt(now);
                    return existing;
                })
                .orElseGet(() -> new UserLocationDocument(
                        userIdString,
                        user.getEmail(),
                        latitude,
                        longitude,
                        geohash,
                        coordinates,
                        now
                ));

        UserLocationDocument saved = userLocationMongoRepository.save(location);
        return mapToResponse(saved);
    }

    public UserLocationResponseDto getMyLocation(UUID userId) {
        return getLocationByUserId(userId);
    }

    public List<UserLocationResponseDto> getAllLocations() {
        return userLocationMongoRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public UserLocationResponseDto getLocationByUserId(UUID userId) {
        String userIdString = userId.toString();
        UserLocationDocument location = userLocationMongoRepository.findByUserId(userIdString)
                .orElseThrow(() -> new UserLocationNotFoundException("Location not found for user: " + userId));
        return mapToResponse(location);
    }

    public List<NearbyFriendResponseDto> getNearbyFriends(UUID userId, double radiusKm) {
        if (radiusKm <= 0) {
            throw new InvalidFriendshipActionException("radiusKm must be greater than 0");
        }

        findUser(userId); // validate user exists

        List<String> friendIds = friendshipRepository.findByRequesterIdOrReceiverId(userId, userId).stream()
                .filter(friendship -> friendship.getStatus() == FriendshipStatus.ACCEPTED)
                .map(friendship -> friendship.getRequesterId().equals(userId)
                        ? friendship.getReceiverId().toString()
                        : friendship.getRequesterId().toString())
                .distinct()
                .collect(Collectors.toList());

        if (friendIds.isEmpty()) {
            return List.of();
        }

        UserLocationDocument myLocation = userLocationMongoRepository.findByUserId(userId.toString())
                .orElseThrow(() -> new UserLocationNotFoundException("Current user location not found"));

        Point point = new Point(myLocation.getCoordinates().getX(), myLocation.getCoordinates().getY());
        NearQuery nearQuery = NearQuery.near(point)
                .maxDistance(new Distance(radiusKm, Metrics.KILOMETERS));
        Query query = new Query(Criteria.where("userId").in(friendIds));
        nearQuery.query(query);

        GeoResults<UserLocationDocument> geoResults = mongoTemplate.geoNear(nearQuery, UserLocationDocument.class);

        return geoResults.getContent().stream()
                .map(result -> {
                    UserLocationDocument doc = result.getContent();
                    double distanceKm = result.getDistance() != null ? result.getDistance().getValue() : 0.0;
                    return new NearbyFriendResponseDto(
                            doc.getUserId(),
                            doc.getEmail(),
                            doc.getLatitude(),
                            doc.getLongitude(),
                            distanceKm,
                            doc.getUpdatedAt()
                    );
                })
                .collect(Collectors.toList());
    }

    private void validateLatitude(double latitude) {
        if (latitude < -90 || latitude > 90) {
            throw new InvalidFriendshipActionException("Latitude must be between -90 and 90");
        }
    }

    private void validateLongitude(double longitude) {
        if (longitude < -180 || longitude > 180) {
            throw new InvalidFriendshipActionException("Longitude must be between -180 and 180");
        }
    }

    private User findUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
    }

    private UserLocationResponseDto mapToResponse(UserLocationDocument document) {
        return new UserLocationResponseDto(
                document.getUserId(),
                document.getEmail(),
                document.getLatitude(),
                document.getLongitude(),
                document.getGeohash(),
                document.getUpdatedAt()
        );
    }
}

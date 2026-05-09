package com.example.travelappbe.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.travelappbe.dto.AnalyticsResponseDto;
import com.example.travelappbe.dto.CategoryStatsDto;
import com.example.travelappbe.dto.LocationVisitStatsDto;
import com.example.travelappbe.dto.VisitFrequencyDto;
import com.example.travelappbe.entity.AnalyticsVisit;
import com.example.travelappbe.entity.Location;
import com.example.travelappbe.entity.Review;
import com.example.travelappbe.entity.User;
import com.example.travelappbe.entity.UserRole;
import com.example.travelappbe.repository.AnalyticsVisitRepository;
import com.example.travelappbe.repository.LocationRepository;
import com.example.travelappbe.repository.ReviewRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("AnalyticsService Unit Tests (Analytics Feature)")
class AnalyticsServiceTest {

    @Mock
    private LocationRepository locationRepository;

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private AnalyticsVisitRepository analyticsVisitRepository;

    @InjectMocks
    private AnalyticsService analyticsService;

    private Location testLocation1;
    private Location testLocation2;
    private Location testLocation3;
    private User testUser;
    private Review testReview1;
    private Review testReview2;
    private AnalyticsVisit testVisit1;
    private AnalyticsVisit testVisit2;

    @BeforeEach
    void setUp() {
        // Setup users
        testUser = new User("tourist@test.com", "hash", UserRole.TOURIST);
        testUser.setId(UUID.randomUUID());

        // Setup locations
        testLocation1 = new Location("Statue of Liberty", "Famous landmark", "Monument", 0.0, "New York", 40.6892, -74.0445);
        testLocation1.setId(UUID.randomUUID());
        
        testLocation2 = new Location("Brooklyn Bridge", "Historic bridge", "Monument", 0.0, "New York", 40.7061, -73.9969);
        testLocation2.setId(UUID.randomUUID());
        
        testLocation3 = new Location("Central Park", "Large public park", "Park", 0.0, "New York", 40.7851, -73.9683);
        testLocation3.setId(UUID.randomUUID());

        // Setup reviews
        testReview1 = new Review();
        testReview1.setId(UUID.randomUUID());
        testReview1.setLocation(testLocation1);
        testReview1.setUser(testUser);
        testReview1.setRating(5);
        testReview1.setComment("Amazing!");
        testReview1.setCreatedAt(LocalDateTime.now());

        testReview2 = new Review();
        testReview2.setId(UUID.randomUUID());
        testReview2.setLocation(testLocation1);
        testReview2.setUser(testUser);
        testReview2.setRating(4);
        testReview2.setComment("Great!");
        testReview2.setCreatedAt(LocalDateTime.now());

        // Setup visits
        testVisit1 = new AnalyticsVisit(testLocation1.getId(), testUser.getId());
        testVisit1.setVisitTimestamp(LocalDateTime.now());

        testVisit2 = new AnalyticsVisit(testLocation1.getId(), testUser.getId());
        testVisit2.setVisitTimestamp(LocalDateTime.now().plusHours(1));
    }

    @Test
    @DisplayName("Should get most visited locations successfully")
    void testGetMostVisitedLocations() {
        // Arrange
        when(locationRepository.findAll()).thenReturn(List.of(testLocation1, testLocation2, testLocation3));
        when(analyticsVisitRepository.countByObjectiveId(testLocation1.getId())).thenReturn(5L);
        when(analyticsVisitRepository.countByObjectiveId(testLocation2.getId())).thenReturn(3L);
        when(analyticsVisitRepository.countByObjectiveId(testLocation3.getId())).thenReturn(0L);
        when(reviewRepository.getAverageRatingByLocation(testLocation1)).thenReturn(4.5);
        when(reviewRepository.getAverageRatingByLocation(testLocation2)).thenReturn(4.0);

        // Act
        List<LocationVisitStatsDto> result = analyticsService.getMostVisitedLocations();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size()); // Only 2 locations have visits
        assertEquals(5L, result.get(0).getTotalVisits());
        assertEquals(3L, result.get(1).getTotalVisits());
        assertEquals("Statue of Liberty", result.get(0).getLocationName());
    }

    @Test
    @DisplayName("Should get popular categories successfully")
    void testGetPopularCategories() {
        // Arrange
        when(locationRepository.findAllCategories()).thenReturn(List.of("Monument", "Park"));
        when(locationRepository.countByCategory("Monument")).thenReturn(2L);
        when(locationRepository.countByCategory("Park")).thenReturn(1L);
        when(locationRepository.findByCategory("Monument")).thenReturn(List.of(testLocation1, testLocation2));
        when(locationRepository.findByCategory("Park")).thenReturn(List.of(testLocation3));
        
        when(analyticsVisitRepository.countByObjectiveId(testLocation1.getId())).thenReturn(5L);
        when(analyticsVisitRepository.countByObjectiveId(testLocation2.getId())).thenReturn(3L);
        when(analyticsVisitRepository.countByObjectiveId(testLocation3.getId())).thenReturn(0L);
        
        when(reviewRepository.getAverageRatingByLocation(testLocation1)).thenReturn(4.5);
        when(reviewRepository.getAverageRatingByLocation(testLocation2)).thenReturn(4.0);
        when(reviewRepository.getAverageRatingByLocation(testLocation3)).thenReturn(null);

        // Act
        List<CategoryStatsDto> result = analyticsService.getPopularCategories();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Monument", result.get(0).getCategory());
        assertEquals(8L, result.get(0).getTotalVisits()); // 5 + 3
        assertEquals("Park", result.get(1).getCategory());
        assertEquals(0L, result.get(1).getTotalVisits());
    }

    @Test
    @DisplayName("Should get visit frequency by time successfully")
    void testGetVisitFrequencyByTime() {
        // Arrange
        when(analyticsVisitRepository.findAll()).thenReturn(List.of(testVisit1, testVisit2));

        // Act
        List<VisitFrequencyDto> result = analyticsService.getVisitFrequencyByTime();

        // Assert
        assertNotNull(result);
        assertTrue(result.size() > 0);
        // Both reviews are from today, so there should be 1 entry for today
        assertEquals(1, result.size());
        assertEquals(2L, result.get(0).getTotalVisits());
    }

    @Test
    @DisplayName("Should get analytics overview successfully")
    void testGetAnalytics() {
        // Arrange
        when(locationRepository.findAll()).thenReturn(List.of(testLocation1, testLocation2));
        when(locationRepository.findAllCategories()).thenReturn(List.of("Monument", "Park"));
        when(locationRepository.count()).thenReturn(2L);
        when(locationRepository.countDistinctCategories()).thenReturn(2L);
        when(locationRepository.countByCategory("Monument")).thenReturn(2L);
        when(locationRepository.countByCategory("Park")).thenReturn(0L);
        when(locationRepository.findByCategory("Monument")).thenReturn(List.of(testLocation1, testLocation2));
        when(locationRepository.findByCategory("Park")).thenReturn(List.of());
        
        when(analyticsVisitRepository.countByObjectiveId(testLocation1.getId())).thenReturn(5L);
        when(analyticsVisitRepository.countByObjectiveId(testLocation2.getId())).thenReturn(3L);
        when(reviewRepository.getAverageRatingByLocation(testLocation1)).thenReturn(4.5);
        when(reviewRepository.getAverageRatingByLocation(testLocation2)).thenReturn(4.0);
        when(analyticsVisitRepository.count()).thenReturn(8L);
        when(analyticsVisitRepository.findAll()).thenReturn(List.of(testVisit1, testVisit2));

        // Act
        AnalyticsResponseDto result = analyticsService.getAnalytics();

        // Assert
        assertNotNull(result);
        assertEquals(8L, result.getTotalVisits());
        assertEquals(2L, result.getTotalLocations());
        assertEquals(2L, result.getTotalCategories());
        assertNotNull(result.getMostVisitedLocations());
        assertNotNull(result.getPopularCategories());
        assertNotNull(result.getVisitFrequencyByTime());
    }

    @Test
    @DisplayName("Should handle empty location list")
    void testGetMostVisitedLocationsEmptyList() {
        // Arrange
        when(locationRepository.findAll()).thenReturn(List.of());

        // Act
        List<LocationVisitStatsDto> result = analyticsService.getMostVisitedLocations();

        // Assert
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    @DisplayName("Should get daily visit frequency")
    void testGetVisitFrequencyByDay() {
        // Arrange
        LocalDateTime today = LocalDateTime.now();
        AnalyticsVisit visit1 = new AnalyticsVisit(testLocation1.getId(), testUser.getId());
        visit1.setVisitTimestamp(today);
        AnalyticsVisit visit2 = new AnalyticsVisit(testLocation1.getId(), testUser.getId());
        visit2.setVisitTimestamp(today.plusHours(1));
        
        when(analyticsVisitRepository.findAll()).thenReturn(List.of(visit1, visit2));

        // Act
        List<VisitFrequencyDto> result = analyticsService.getVisitFrequencyByDay();

        // Assert
        assertNotNull(result);
        assertTrue(result.size() >= 1);
        assertEquals(2L, result.get(0).getTotalVisits());
    }

    @Test
    @DisplayName("Should get weekly visit frequency")
    void testGetVisitFrequencyByWeek() {
        // Arrange
        when(analyticsVisitRepository.findAll()).thenReturn(List.of(testVisit1, testVisit2));

        // Act
        List<VisitFrequencyDto> result = analyticsService.getVisitFrequencyByWeek();

        // Assert
        assertNotNull(result);
        assertTrue(result.size() >= 1);
    }
}

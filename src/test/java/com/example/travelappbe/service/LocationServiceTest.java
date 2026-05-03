package com.example.travelappbe.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.example.travelappbe.dto.LocationDetailsResponseDto;
import com.example.travelappbe.dto.LocationRequestDto;
import com.example.travelappbe.dto.LocationResponseDto;
import com.example.travelappbe.entity.Location;
import com.example.travelappbe.entity.Review;
import com.example.travelappbe.entity.User;
import com.example.travelappbe.repository.LocationRepository;
import com.example.travelappbe.repository.ReviewRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("LocationService Unit Tests (US7 - SCRUM-?)")
class LocationServiceTest {

    @Mock
    private LocationRepository locationRepository;

    @Mock
    private ReviewRepository reviewRepository;

    @InjectMocks
    private LocationService locationService;

    private Location testLocation;
    private LocationRequestDto requestDto;
    private UUID locationId;

    @BeforeEach
    void setUp() {
        locationId = UUID.randomUUID();
        
        testLocation = new Location(
                "Central Park",
                "Large public park in NYC",
                40.785091,
                -73.968285
        );
        testLocation.setId(locationId);
        testLocation.setCountry("USA");
        testLocation.setCity("New York");
        testLocation.setCategory("Park");
        testLocation.setImageUrl("https://example.com/park.jpg");
        testLocation.setCreatedAt(LocalDateTime.now());
        testLocation.setUpdatedAt(LocalDateTime.now());

        requestDto = new LocationRequestDto();
        requestDto.setName("Central Park");
        requestDto.setDescription("Large public park in NYC");
        requestDto.setLatitude(40.785091);
        requestDto.setLongitude(-73.968285);
        requestDto.setCountry("USA");
        requestDto.setCity("New York");
        requestDto.setCategory("Park");
        requestDto.setImageUrl("https://example.com/park.jpg");
    }

    @Test
    @DisplayName("Should return location by ID")
    void testGetLocationById() {
        when(locationRepository.findById(locationId)).thenReturn(Optional.of(testLocation));

        LocationResponseDto response = locationService.getLocationById(locationId);

        assertNotNull(response);
        assertEquals(locationId, response.getId());
        assertEquals("Central Park", response.getName());
    }

    @Test
    @DisplayName("Should throw exception when location not found by ID")
    void testGetLocationById_NotFound() {
        when(locationRepository.findById(locationId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> locationService.getLocationById(locationId));
    }

    @Test
    @DisplayName("Should create new location")
    void testCreateLocation() {
        when(locationRepository.save(any(Location.class))).thenReturn(testLocation);

        LocationResponseDto response = locationService.createLocation(requestDto);

        assertNotNull(response);
        assertEquals("Central Park", response.getName());
        verify(locationRepository, times(1)).save(any(Location.class));
    }

    @Test
    @DisplayName("Should update existing location")
    void testUpdateLocation() {
        when(locationRepository.findById(locationId)).thenReturn(Optional.of(testLocation));
        when(locationRepository.save(any(Location.class))).thenReturn(testLocation);

        LocationRequestDto updateRequest = new LocationRequestDto();
        updateRequest.setName("Updated Park");

        LocationResponseDto response = locationService.updateLocation(locationId, updateRequest);

        assertNotNull(response);
        verify(locationRepository, times(1)).findById(locationId);
        verify(locationRepository, times(1)).save(any(Location.class));
    }

    @Test
    @DisplayName("Should delete location")
    void testDeleteLocation() {
        when(locationRepository.findById(locationId)).thenReturn(Optional.of(testLocation));

        locationService.deleteLocation(locationId);

        verify(locationRepository, times(1)).findById(locationId);
        verify(locationRepository, times(1)).delete(testLocation);
    }

    @Test
    @DisplayName("Should get locations with pagination (no filters)")
    void testGetLocations_NoFilters() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Location> page = new PageImpl<>(List.of(testLocation));

        when(locationRepository.findAll(pageable)).thenReturn(page);

        Page<LocationResponseDto> result = locationService.getLocations(null, null, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Central Park", result.getContent().get(0).getName());
        verify(locationRepository, times(1)).findAll(pageable);
    }

    @Test
    @DisplayName("Should get locations with filters and pagination")
    void testGetLocations_WithFilters() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Location> page = new PageImpl<>(List.of(testLocation));

        when(locationRepository.findAll(any(Example.class), eq(pageable))).thenReturn(page);

        Page<LocationResponseDto> result = locationService.getLocations("Park", "New York", pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(locationRepository, times(1)).findAll(any(Example.class), eq(pageable));
    }

    @Test
    @DisplayName("Should return location details including reviews and rating")
    void testGetLocationDetails() {
        when(locationRepository.findById(locationId)).thenReturn(Optional.of(testLocation));
        
        User user = new User("user@test.com", "hash");
        user.setId(UUID.randomUUID());
        
        Review review = new Review();
        review.setId(UUID.randomUUID());
        review.setRating(5);
        review.setComment("Great place!");
        review.setUser(user);
        review.setLocation(testLocation);
        
        when(reviewRepository.findByLocation(testLocation)).thenReturn(List.of(review));

        LocationDetailsResponseDto details = locationService.getLocationDetails(locationId);

        assertNotNull(details);
        assertEquals("Central Park", details.getName());
        assertEquals(5.0, details.getAverageRating());
        assertEquals(1, details.getTotalReviews());
        assertEquals(1, details.getReviews().size());
    }
}
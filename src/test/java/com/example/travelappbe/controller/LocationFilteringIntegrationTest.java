package com.example.travelappbe.controller;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import com.example.travelappbe.entity.Location;
import com.example.travelappbe.entity.User;
import com.example.travelappbe.entity.UserRole;
import com.example.travelappbe.repository.LocationRepository;
import com.example.travelappbe.repository.UserRepository;

import jakarta.servlet.Filter;

/**
 * Integration tests for Location Filtering & Pagination (US2 - SCRUM-46)
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("Location Filtering Integration Tests (US2 - SCRUM-46)")
class LocationFilteringIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private Filter springSecurityFilterChain;

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .addFilters(springSecurityFilterChain)
                .build();

        locationRepository.deleteAll();
        userRepository.deleteAll();
        User admin = new User("admin@test.com", "hash", UserRole.ADMIN);
        admin = userRepository.save(admin);

        Location loc1 = new Location("Eiffel Tower", "Description", "Monument", 20.0, "Paris", 48.8584, 2.2945);
        loc1.setAdmin(admin);
        locationRepository.save(loc1);

        Location loc2 = new Location("Louvre Museum", "Description", "Museum", 15.0, "Paris", 48.8606, 2.3376);
        loc2.setAdmin(admin);
        locationRepository.save(loc2);

        Location loc3 = new Location("Colosseum", "Description", "Monument", 30.0, "Rome", 41.8902, 12.4922);
        loc3.setAdmin(admin);
        locationRepository.save(loc3);
    }

    @Test
    @DisplayName("GET /locations - filter by city")
    void testFilterByCity() throws Exception {
        mockMvc.perform(get("/api/locations?locationName=Paris"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].location_name", equalTo("Paris")))
                .andExpect(jsonPath("$[1].location_name", equalTo("Paris")));
    }

    @Test
    @DisplayName("GET /locations - filter by category")
    void testFilterByCategory() throws Exception {
        mockMvc.perform(get("/api/locations?category=Museum"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].category", equalTo("Museum")));
    }

    @Test
    @DisplayName("GET /locations - filter by city and category")
    void testFilterByCityAndCategory() throws Exception {
        mockMvc.perform(get("/api/locations?locationName=Paris&category=Monument"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", equalTo("Eiffel Tower")));
    }

    @Test
    @DisplayName("GET /locations - pagination")
    void testPagination() throws Exception {
        mockMvc.perform(get("/api/locations?page=0&size=2&sort=name,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    @DisplayName("GET /locations - filter by category (case-insensitive search)")
    void testFilterByCategory_CaseInsensitive() throws Exception {
        mockMvc.perform(get("/api/locations?category=mUsEuM"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].category", equalTo("Museum")));
    }

    @Test
    @DisplayName("GET /locations - filter by city (partial match search)")
    void testFilterByCity_PartialMatch() throws Exception {
        // The search query "ari" should match "Paris" correctly
        mockMvc.perform(get("/api/locations?locationName=ari"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].location_name", equalTo("Paris")))
                .andExpect(jsonPath("$[1].location_name", equalTo("Paris")));
    }

    @Test
    @DisplayName("GET /locations - filter with no matching results")
    void testFilter_NoResults() throws Exception {
        mockMvc.perform(get("/api/locations?locationName=London&category=Park"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("GET /locations - pagination second page")
    void testPagination_SecondPage() throws Exception {
        mockMvc.perform(get("/api/locations?page=1&size=2&sort=name,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }
}
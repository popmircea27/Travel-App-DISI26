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
import com.example.travelappbe.repository.LocationRepository;

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

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .addFilters(springSecurityFilterChain)
                .build();

        locationRepository.deleteAll();

        Location loc1 = new Location("Eiffel Tower", "Description", 48.8584, 2.2945);
        loc1.setCity("Paris");
        loc1.setCategory("Monument");
        locationRepository.save(loc1);

        Location loc2 = new Location("Louvre Museum", "Description", 48.8606, 2.3376);
        loc2.setCity("Paris");
        loc2.setCategory("Museum");
        locationRepository.save(loc2);

        Location loc3 = new Location("Colosseum", "Description", 41.8902, 12.4922);
        loc3.setCity("Rome");
        loc3.setCategory("Monument");
        locationRepository.save(loc3);
    }

    @Test
    @DisplayName("GET /locations - filter by city")
    void testFilterByCity() throws Exception {
        mockMvc.perform(get("/api/locations?city=Paris"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].city", equalTo("Paris")))
                .andExpect(jsonPath("$.content[1].city", equalTo("Paris")));
    }

    @Test
    @DisplayName("GET /locations - filter by category")
    void testFilterByCategory() throws Exception {
        mockMvc.perform(get("/api/locations?category=Museum"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].category", equalTo("Museum")));
    }

    @Test
    @DisplayName("GET /locations - filter by city and category")
    void testFilterByCityAndCategory() throws Exception {
        mockMvc.perform(get("/api/locations?city=Paris&category=Monument"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].name", equalTo("Eiffel Tower")));
    }

    @Test
    @DisplayName("GET /locations - pagination")
    void testPagination() throws Exception {
        mockMvc.perform(get("/api/locations?page=0&size=2&sort=name,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.totalElements", equalTo(3)))
                .andExpect(jsonPath("$.totalPages", equalTo(2)));
    }

    @Test
    @DisplayName("GET /locations - filter by category (case-insensitive search)")
    void testFilterByCategory_CaseInsensitive() throws Exception {
        mockMvc.perform(get("/api/locations?category=mUsEuM"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].category", equalTo("Museum")));
    }

    @Test
    @DisplayName("GET /locations - filter by city (partial match search)")
    void testFilterByCity_PartialMatch() throws Exception {
        // The search query "ari" should match "Paris" correctly
        mockMvc.perform(get("/api/locations?city=ari"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].city", equalTo("Paris")))
                .andExpect(jsonPath("$.content[1].city", equalTo("Paris")));
    }

    @Test
    @DisplayName("GET /locations - filter with no matching results")
    void testFilter_NoResults() throws Exception {
        mockMvc.perform(get("/api/locations?city=London&category=Park"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)))
                .andExpect(jsonPath("$.totalElements", equalTo(0)));
    }

    @Test
    @DisplayName("GET /locations - pagination second page")
    void testPagination_SecondPage() throws Exception {
        mockMvc.perform(get("/api/locations?page=1&size=2&sort=name,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)));
    }
}
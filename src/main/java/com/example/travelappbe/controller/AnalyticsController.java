package com.example.travelappbe.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.travelappbe.dto.AnalyticsResponseDto;
import com.example.travelappbe.dto.CategoryStatsDto;
import com.example.travelappbe.dto.LocationVisitStatsDto;
import com.example.travelappbe.dto.VisitFrequencyDto;
import com.example.travelappbe.service.AnalyticsService;

/**
 * REST Controller for analytics-related operations (admin only)
 * Provides statistical data about locations, categories, and visit patterns
 */
@RestController
@RequestMapping("/api/analytics")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    /**
     * Get comprehensive analytics about locations.
     * Includes most visited locations, popular categories, and visit frequency.
     * Only admins can access this endpoint.
     *
     * @return AnalyticsResponseDto containing all analytics data
     */
    @GetMapping("/overview")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AnalyticsResponseDto> getAnalyticsOverview() {
        AnalyticsResponseDto analytics = analyticsService.getAnalytics();
        return ResponseEntity.ok(analytics);
    }

    /**
     * Get the most visited locations (top 10).
     * Only admins can access this endpoint.
     *
     * @return list of LocationVisitStatsDto sorted by visit count
     */
    @GetMapping("/most-visited-locations")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<LocationVisitStatsDto>> getMostVisitedLocations() {
        List<LocationVisitStatsDto> mostVisited = analyticsService.getMostVisitedLocations();
        return ResponseEntity.ok(mostVisited);
    }

    /**
     * Get popular categories with statistics.
     * Only admins can access this endpoint.
     *
     * @return list of CategoryStatsDto sorted by total visits
     */
    @GetMapping("/popular-categories")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<CategoryStatsDto>> getPopularCategories() {
        List<CategoryStatsDto> categories = analyticsService.getPopularCategories();
        return ResponseEntity.ok(categories);
    }

    /**
     * Get visit frequency aggregated by month.
     * Only admins can access this endpoint.
     *
     * @return list of VisitFrequencyDto grouped by month
     */
    @GetMapping("/visit-frequency/monthly")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<VisitFrequencyDto>> getMonthlyVisitFrequency(@RequestParam(required = false) String year) {
        List<VisitFrequencyDto> frequency = analyticsService.getVisitFrequencyByTime(year);
        return ResponseEntity.ok(frequency);
    }

    /**
     * Get visit frequency aggregated by week (last 90 days).
     * Only admins can access this endpoint.
     *
     * @return list of VisitFrequencyDto grouped by week
     */
    @GetMapping("/visit-frequency/weekly")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<VisitFrequencyDto>> getWeeklyVisitFrequency() {
        List<VisitFrequencyDto> frequency = analyticsService.getVisitFrequencyByWeek();
        return ResponseEntity.ok(frequency);
    }

    /**
     * Get visit frequency aggregated by day (last 30 days).
     * Only admins can access this endpoint.
     *
     * @return list of VisitFrequencyDto grouped by day
     */
    @GetMapping("/visit-frequency/daily")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<VisitFrequencyDto>> getDailyVisitFrequency(@RequestParam(required = false) String month) {
        List<VisitFrequencyDto> frequency = analyticsService.getVisitFrequencyByDay(month);
        return ResponseEntity.ok(frequency);
    }

    /**
     * Get visit frequency aggregated by hour of the day.
     * Only admins can access this endpoint.
     *
     * @return list of VisitFrequencyDto grouped by hour
     */
    @GetMapping("/visit-frequency/hourly")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<VisitFrequencyDto>> getHourlyVisitFrequency(@RequestParam(required = false) String date) {
        List<VisitFrequencyDto> frequency = analyticsService.getVisitFrequencyByHour(date);
        return ResponseEntity.ok(frequency);
    }
}

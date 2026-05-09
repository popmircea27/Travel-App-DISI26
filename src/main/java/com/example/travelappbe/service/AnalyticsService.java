package com.example.travelappbe.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.travelappbe.dto.AnalyticsResponseDto;
import com.example.travelappbe.dto.CategoryStatsDto;
import com.example.travelappbe.dto.LocationVisitStatsDto;
import com.example.travelappbe.dto.VisitFrequencyDto;
import com.example.travelappbe.entity.AnalyticsVisit;
import com.example.travelappbe.entity.Location;
import com.example.travelappbe.entity.Review;
import com.example.travelappbe.repository.AnalyticsVisitRepository;
import com.example.travelappbe.repository.LocationRepository;
import com.example.travelappbe.repository.ReviewRepository;

@Service
@Transactional(readOnly = true)
public class AnalyticsService {

    private final ReviewRepository reviewRepository;
    private final LocationRepository locationRepository;
    private final AnalyticsVisitRepository analyticsVisitRepository;

    public AnalyticsService(ReviewRepository reviewRepository, LocationRepository locationRepository, AnalyticsVisitRepository analyticsVisitRepository) {
        this.reviewRepository = reviewRepository;
        this.locationRepository = locationRepository;
        this.analyticsVisitRepository = analyticsVisitRepository;
    }

    /**
     * Get comprehensive analytics about locations
     */
    public AnalyticsResponseDto getAnalytics() {
        List<LocationVisitStatsDto> mostVisitedLocations = getMostVisitedLocations();
        List<CategoryStatsDto> popularCategories = getPopularCategories();
        List<VisitFrequencyDto> visitFrequencyByTime = getVisitFrequencyByTime();
        
        long totalVisits = analyticsVisitRepository.count();
        long totalLocations = locationRepository.count();
        long totalCategories = locationRepository.countDistinctCategories();

        return new AnalyticsResponseDto(
            mostVisitedLocations,
            popularCategories,
            visitFrequencyByTime,
            totalVisits,
            totalLocations,
            totalCategories
        );
    }

    /**
     * Get most visited locations (top 10)
     */
    public List<LocationVisitStatsDto> getMostVisitedLocations() {
        List<Location> allLocations = locationRepository.findAll();
        
        return allLocations.stream()
            .map(location -> {
                long visitCount = analyticsVisitRepository.countByObjectiveId(location.getId());
                if (visitCount == 0) {
                    return null; // Skip locations with no visits
                }
                
                Double avgRating = reviewRepository.getAverageRatingByLocation(location);
                return new LocationVisitStatsDto(
                    location.getId(),
                    location.getName(),
                    location.getCategory(),
                    visitCount,
                    avgRating != null ? avgRating : 0.0
                );
            })
            .filter(Objects::nonNull)
            .sorted((a, b) -> Long.compare(b.getTotalVisits(), a.getTotalVisits()))
            .limit(10)
            .collect(Collectors.toList());
    }

    /**
     * Get popular categories with statistics
     */
    public List<CategoryStatsDto> getPopularCategories() {
        List<String> allCategories = locationRepository.findAllCategories();
        
        return allCategories.stream()
            .map(category -> {
                long locationCount = locationRepository.countByCategory(category);
                List<Location> locationsInCategory = locationRepository.findByCategory(category);
                
                long totalVisits = locationsInCategory.stream()
                    .mapToLong(location -> analyticsVisitRepository.countByObjectiveId(location.getId()))
                    .sum();
                
                Double avgRating = locationsInCategory.stream()
                    .map(location -> reviewRepository.getAverageRatingByLocation(location))
                    .filter(Objects::nonNull)
                    .mapToDouble(Double::doubleValue)
                    .average()
                    .orElse(0.0);
                
                return new CategoryStatsDto(
                    category,
                    locationCount,
                    totalVisits,
                    avgRating
                );
            })
            .sorted((a, b) -> Long.compare(b.getTotalVisits(), a.getTotalVisits()))
            .collect(Collectors.toList());
    }

    /**
     * Get visit frequency by time period (daily, weekly, monthly)
     */
    public List<VisitFrequencyDto> getVisitFrequencyByTime() {
        return getVisitFrequencyByTime(null);
    }

    /**
     * Get visit frequency by time period - monthly aggregation for a specific year
     */
    public List<VisitFrequencyDto> getVisitFrequencyByTime(String yearStr) {
        List<AnalyticsVisit> allVisits = analyticsVisitRepository.findAll();
        java.util.stream.Stream<AnalyticsVisit> stream = allVisits.stream();
        
        if (yearStr != null && !yearStr.isEmpty()) {
            stream = stream.filter(visit -> visit.getVisitTimestamp().format(DateTimeFormatter.ofPattern("yyyy")).equals(yearStr));
        }
        
        // Group by month
        Map<String, Long> monthlyVisits = new TreeMap<>();
        DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("yyyy-MM");
        
        stream.collect(Collectors.groupingBy(
                visit -> visit.getVisitTimestamp().format(monthFormatter),
                Collectors.counting()
            ))
            .forEach((month, count) -> monthlyVisits.put(month, count));
        
        // Convert to VisitFrequencyDto list, sorted by time period
        return monthlyVisits.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .map(entry -> new VisitFrequencyDto(entry.getKey(), entry.getValue()))
            .collect(Collectors.toList());
    }

    /**
     * Get visit frequency by time period - daily aggregation (last 30 days)
     */
    public List<VisitFrequencyDto> getVisitFrequencyByDay() {
        return getVisitFrequencyByDay(null);
    }

    /**
     * Get visit frequency by time period - daily aggregation for a specific month
     */
    public List<VisitFrequencyDto> getVisitFrequencyByDay(String monthStr) {
        List<AnalyticsVisit> allVisits = analyticsVisitRepository.findAll();
        java.util.stream.Stream<AnalyticsVisit> stream = allVisits.stream();
        
        if (monthStr != null && !monthStr.isEmpty()) {
            stream = stream.filter(visit -> visit.getVisitTimestamp().format(DateTimeFormatter.ofPattern("yyyy-MM")).equals(monthStr));
        } else {
            LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
            stream = stream.filter(visit -> visit.getVisitTimestamp().isAfter(thirtyDaysAgo));
        }
        
        // Group by day
        Map<String, Long> dailyVisits = new TreeMap<>();
        DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        
        stream.collect(Collectors.groupingBy(
                visit -> visit.getVisitTimestamp().format(dayFormatter),
                Collectors.counting()
            ))
            .forEach((day, count) -> dailyVisits.put(day, count));
        
        // Convert to VisitFrequencyDto list, sorted by time period
        return dailyVisits.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .map(entry -> new VisitFrequencyDto(entry.getKey(), entry.getValue()))
            .collect(Collectors.toList());
    }

    /**
     * Get visit frequency by time period - weekly aggregation
     */
    public List<VisitFrequencyDto> getVisitFrequencyByWeek() {
        List<AnalyticsVisit> allVisits = analyticsVisitRepository.findAll();
        LocalDateTime ninetyDaysAgo = LocalDateTime.now().minusDays(90);
        
        // Group by week (ISO week number)
        Map<String, Long> weeklyVisits = new TreeMap<>();
        DateTimeFormatter weekFormatter = DateTimeFormatter.ofPattern("yyyy-'W'ww");
        
        allVisits.stream()
            .filter(visit -> visit.getVisitTimestamp().isAfter(ninetyDaysAgo))
            .collect(Collectors.groupingBy(
                visit -> visit.getVisitTimestamp().format(weekFormatter),
                Collectors.counting()
            ))
            .forEach((week, count) -> weeklyVisits.put(week, count));
        
        // Convert to VisitFrequencyDto list, sorted by time period
        return weeklyVisits.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .map(entry -> new VisitFrequencyDto(entry.getKey(), entry.getValue()))
            .collect(Collectors.toList());
    }

    /**
     * Get visit frequency by time period - hourly aggregation (distribution within a day)
     */
    public List<VisitFrequencyDto> getVisitFrequencyByHour() {
        return getVisitFrequencyByHour(null);
    }

    /**
     * Get visit frequency by time period - hourly aggregation for a specific day
     */
    public List<VisitFrequencyDto> getVisitFrequencyByHour(String dateStr) {
        List<AnalyticsVisit> allVisits = analyticsVisitRepository.findAll();
        java.util.stream.Stream<AnalyticsVisit> stream = allVisits.stream();
        
        if (dateStr != null && !dateStr.isEmpty()) {
            stream = stream.filter(visit -> visit.getVisitTimestamp().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")).equals(dateStr));
        }
        
        // Group by hour of the day (00:00 - 23:00)
        Map<String, Long> hourlyVisits = new TreeMap<>();
        DateTimeFormatter hourFormatter = DateTimeFormatter.ofPattern("HH:00");
        
        stream.collect(Collectors.groupingBy(
                visit -> visit.getVisitTimestamp().format(hourFormatter),
                Collectors.counting()
            ))
            .forEach((hour, count) -> hourlyVisits.put(hour, count));
        
        // Convert to VisitFrequencyDto list, sorted by time period
        return hourlyVisits.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .map(entry -> new VisitFrequencyDto(entry.getKey(), entry.getValue()))
            .collect(Collectors.toList());
    }
}

package com.example.travelappbe.dto;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

public class AnalyticsResponseDto {

    @JsonProperty("most_visited_locations")
    private List<LocationVisitStatsDto> mostVisitedLocations;

    @JsonProperty("popular_categories")
    private List<CategoryStatsDto> popularCategories;

    @JsonProperty("visit_frequency_by_time")
    private List<VisitFrequencyDto> visitFrequencyByTime;

    @JsonProperty("total_visits")
    private long totalVisits;

    @JsonProperty("total_locations")
    private long totalLocations;

    @JsonProperty("total_categories")
    private long totalCategories;

    // Constructors
    public AnalyticsResponseDto() {
    }

    public AnalyticsResponseDto(List<LocationVisitStatsDto> mostVisitedLocations,
                                List<CategoryStatsDto> popularCategories,
                                List<VisitFrequencyDto> visitFrequencyByTime,
                                long totalVisits, long totalLocations, long totalCategories) {
        this.mostVisitedLocations = mostVisitedLocations;
        this.popularCategories = popularCategories;
        this.visitFrequencyByTime = visitFrequencyByTime;
        this.totalVisits = totalVisits;
        this.totalLocations = totalLocations;
        this.totalCategories = totalCategories;
    }

    // Getters and Setters
    public List<LocationVisitStatsDto> getMostVisitedLocations() {
        return mostVisitedLocations;
    }

    public void setMostVisitedLocations(List<LocationVisitStatsDto> mostVisitedLocations) {
        this.mostVisitedLocations = mostVisitedLocations;
    }

    public List<CategoryStatsDto> getPopularCategories() {
        return popularCategories;
    }

    public void setPopularCategories(List<CategoryStatsDto> popularCategories) {
        this.popularCategories = popularCategories;
    }

    public List<VisitFrequencyDto> getVisitFrequencyByTime() {
        return visitFrequencyByTime;
    }

    public void setVisitFrequencyByTime(List<VisitFrequencyDto> visitFrequencyByTime) {
        this.visitFrequencyByTime = visitFrequencyByTime;
    }

    public long getTotalVisits() {
        return totalVisits;
    }

    public void setTotalVisits(long totalVisits) {
        this.totalVisits = totalVisits;
    }

    public long getTotalLocations() {
        return totalLocations;
    }

    public void setTotalLocations(long totalLocations) {
        this.totalLocations = totalLocations;
    }

    public long getTotalCategories() {
        return totalCategories;
    }

    public void setTotalCategories(long totalCategories) {
        this.totalCategories = totalCategories;
    }
}

package com.example.travelappbe.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class VisitFrequencyDto {

    @JsonProperty("time_period")
    private String timePeriod;

    @JsonProperty("total_visits")
    private long totalVisits;

    // Constructors
    public VisitFrequencyDto() {
    }

    public VisitFrequencyDto(String timePeriod, long totalVisits) {
        this.timePeriod = timePeriod;
        this.totalVisits = totalVisits;
    }

    // Getters and Setters
    public String getTimePeriod() {
        return timePeriod;
    }

    public void setTimePeriod(String timePeriod) {
        this.timePeriod = timePeriod;
    }

    public long getTotalVisits() {
        return totalVisits;
    }

    public void setTotalVisits(long totalVisits) {
        this.totalVisits = totalVisits;
    }
}

package com.codaily.management.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class CalendarResponse {
    private LocalDate baseDate;
    private LocalDate startDate;
    private LocalDate endDate;
    private List<CalendarEvent> events;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CalendarEvent {
        private Long projectId;
        private Long scheduleId;
        private Long featureId;
        private String featureTitle;
        private String featureDescription;
        private LocalDate scheduleDate;
        private Double allocatedHours;
        private String category;
        private Integer priorityLevel;
        private String status;
        private Boolean withinProjectPeriod;
    }
}

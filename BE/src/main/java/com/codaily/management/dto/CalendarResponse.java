package com.codaily.management.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
        private Long scheduleId;
        private Long featureId;
        private String featureTitle;
        private String featureDescription;
        private LocalDate scheduleDate;
        private Integer allocatedHours;
        private String category;
        private Integer priorityLevel;
        private String status;
    }
}

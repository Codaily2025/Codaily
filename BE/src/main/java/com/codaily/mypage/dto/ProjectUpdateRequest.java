package com.codaily.mypage.dto;

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
public class ProjectUpdateRequest {
    private String title;
    private LocalDate startDate;
    private LocalDate endDate;

    private List<LocalDate> scheduledDates;
    private List<DaysOfWeekRequest> daysOfWeek;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DaysOfWeekRequest {
        private String dateName; // "MONDAY", "TUESDAY" 등
        private Double hours;
    }
}

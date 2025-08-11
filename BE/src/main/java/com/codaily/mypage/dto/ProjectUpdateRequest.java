package com.codaily.mypage.dto;

import com.codaily.project.entity.Project;
import lombok.*;

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
        private Integer hours;
    }
}

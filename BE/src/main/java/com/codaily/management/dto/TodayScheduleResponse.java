package com.codaily.management.dto;

import com.codaily.project.entity.FeatureItem;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class TodayScheduleResponse {
    private LocalDate date;
    private List<TodayTask> tasks;

    @Data
    @Builder
    public static class TodayTask {
        private Long projectId;
        private Long scheduleId;
        private Long featureId;
        private String featureTitle;
        private String featureDescription;
        private Double allocatedHours;
        private String category;
        private String status;
    }
}

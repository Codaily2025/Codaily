package com.codaily.project.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectProgressResponse {
    private boolean success;
    private ProjectProgressData data;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ProjectProgressData {
        private OverallProgress overallProgress;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OverallProgress {
        private Double percentage;
        private Integer completedTasks;
        private Integer totalTasks;
        private LocalDate estimatedCompletion;
    }
}
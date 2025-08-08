package com.codaily.project.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CalendarFeatureResponse {
    private boolean success;
    private CalendarFeatureData data;
    private String message;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CalendarFeatureData {
        private LocalDate date;
        private List<FeatureInfo> features;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FeatureInfo {
        private Long featureId;
        private String title;
        private String description;
        private String field;
        private String category;
        private String status;
        private Integer priorityLevel;
        private Double estimatedTime;
        private String parentFeatureTitle; // 상위 기능 제목 (하위 기능인 경우)
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private LocalDateTime completedAt;
    }
}
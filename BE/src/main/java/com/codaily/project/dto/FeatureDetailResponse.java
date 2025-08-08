package com.codaily.project.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeatureDetailResponse {
    private boolean success;
    private FeatureDetailData data;
    private String message;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FeatureDetailData {
        private Long featureId;
        private String title;
        private String description;
        private String field;
        private String category;
        private String status;
        private Integer priorityLevel;
        private Double estimatedTime;
        private Boolean isSelected;
        private Boolean isCustom;
        private Boolean isReduced;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private LocalDateTime completedAt;

        // 상위 기능 정보 (하위 기능인 경우)
        private ParentFeatureInfo parentFeature;

        // 하위 기능들 (상위 기능인 경우)
        private List<SubFeatureInfo> subFeatures;

        // 기능 메트릭
        private FeatureMetrics metrics;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ParentFeatureInfo {
        private Long featureId;
        private String title;
        private String description;
        private String field;
        private String category;
        private Integer priorityLevel;
        private Double estimatedTime;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SubFeatureInfo {
        private Long featureId;
        private String title;
        private String description;
        private String category;
        private String status;
        private Integer priorityLevel;
        private Double estimatedTime;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private LocalDateTime completedAt;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FeatureMetrics {
        private Integer totalSubFeatures;
        private Integer completedSubFeatures;
        private Double progressPercentage;
        private Integer recentCommitsCount;
        private LocalDateTime lastActivityAt;
    }
}
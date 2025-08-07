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
public class TaskDetailResponse {
    private boolean success;
    private TaskDetailData data;
    private String message;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TaskDetailData {
        private Long taskId;
        private String title;
        private String description;
        private String status;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private LocalDateTime completedAt;

        // 연관된 기능 정보
        private FeatureInfo feature;

        // 같은 기능의 다른 작업들 (하위 작업들)
        private List<SubTaskInfo> subTasks;

        // 작업 메트릭
        private TaskMetrics metrics;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FeatureInfo {
        private Long featureId;
        private String title;
        private String description;
        private String category;
        private Integer priorityLevel;
        private Integer estimatedTime;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SubTaskInfo {
        private Long taskId;
        private String title;
        private String description;
        private String status;
        private LocalDateTime createdAt;
        private LocalDateTime completedAt;
        private boolean isMainTask; // 현재 조회중인 작업인지
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TaskMetrics {
        private Integer totalTasksInFeature;
        private Integer completedTasksInFeature;
        private Double featureProgressPercentage;
        private Integer recentCommitsCount;
        private LocalDateTime lastActivityAt;
    }
}
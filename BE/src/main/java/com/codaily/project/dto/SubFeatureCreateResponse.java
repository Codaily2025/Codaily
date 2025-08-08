package com.codaily.project.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubFeatureCreateResponse {
    private boolean success;
    private SubFeatureData data;
    private String message;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SubFeatureData {
        private Long featureId;
        private String title;
        private String description;
        private String status;
        private LocalDateTime createdAt;
        private String category;
        private Integer priorityLevel;
        private Double estimatedTime;
        private Long parentFeatureId;
    }
}
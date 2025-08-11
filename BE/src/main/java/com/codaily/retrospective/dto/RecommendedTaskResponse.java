package com.codaily.retrospective.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RecommendedTaskResponse {
    private Long featureId;
    private String title;
    private String description;
    private String field;
    private String category;
    private Integer priorityLevel;
    private Double estimatedTime;
    private String reason; // 추천 이유
}
package com.codaily.project.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubFeatureCreateRequest {
    private String title;
    private String description;
    private String category;
    private String status; // TODO, IN_PROGRESS, DONE (기본값: TODO)
    private Integer priorityLevel;
    private Double estimatedTime;
}

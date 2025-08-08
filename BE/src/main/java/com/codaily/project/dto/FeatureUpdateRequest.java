package com.codaily.project.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeatureUpdateRequest {
    private String title;
    private String description;
    private String field;
    private String category;
    private String status; // TODO, IN_PROGRESS, DONE
    private Integer priorityLevel;
    private Double estimatedTime;
    private Boolean isSelected;
    private Boolean isReduced;
}
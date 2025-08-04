package com.codaily.project.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FeatureSaveItem {
    private Long id;
    private String title;
    private String description;
    private Integer estimatedTime;
    private Integer priorityLevel;
}

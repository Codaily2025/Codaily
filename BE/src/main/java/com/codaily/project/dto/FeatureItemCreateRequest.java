package com.codaily.project.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FeatureItemCreateRequest {
    private String title;
    private String description;
    private String field;
    private String category;
    private Integer priorityLevel;
    private Double estimatedTime;
    private Boolean isCustom;
    private Long projectId;
    private Long parentFeatureId;
}
package com.codaily.project.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeatureItemResponse {
    private Long featureId;
    private String title;
    private String description;
    private String field;
    private String category;
    private String status;
    private Integer priorityLevel;
    private Integer estimatedTime;
    private Boolean isSelected;
    private Boolean isCustom;
    private Boolean isReduced;
    private Long projectId;
    private Long specificationId;
    private Long parentFeatureId;
    private List<FeatureItemResponse> childFeatures;
}

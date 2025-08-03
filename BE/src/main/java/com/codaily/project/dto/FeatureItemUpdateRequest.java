package com.codaily.project.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FeatureItemUpdate {
    private String title;
    private String description;
    private String field;
    private String category;
    private String status;
    private Integer priorityLevel;
    private Double estimatedTime;
    private Boolean isSelected;
    private Boolean isReduced;
    private Long parentFeatureId;
}

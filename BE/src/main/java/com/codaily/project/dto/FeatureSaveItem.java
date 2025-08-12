package com.codaily.project.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FeatureSaveItem {
    private Long id;
    @JsonProperty("isReduced")
    private boolean isReduced;
    private String title;
    private String description;
    private Double estimatedTime;
    private Integer priorityLevel;
}

package com.codaily.project.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class SubFeatureSaveRequest {

    private Long featureId; // 상위 주기능 ID

    private String field;

    @JsonProperty("subFeature")
    private SubFeatureDto subFeature;

    @Data
    public static class SubFeatureDto {
        @JsonProperty("title")
        private String title;

        private String description;

        @JsonProperty("estimated_time")
        private Double estimatedTime;

        @JsonProperty("priority_level")
        private Integer priorityLevel;
    }
}


package com.codaily.project.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class FeatureSaveRequest {

    @JsonProperty("field")
    private String field;

    @JsonProperty("main_feature")
    private MainFeature mainFeature;

    @JsonProperty("sub_feature")
    private List<SubFeature> subFeature;

    @Data
    public static class MainFeature {
        private String title;
        private String description;
    }

    @Data
    public static class SubFeature {
        private String title;
        private String description;

        @JsonProperty("estimated_time")
        private Double estimatedTime;

        @JsonProperty("priority_level")
        private Integer priorityLevel;
    }
}

package com.codaily.project.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class FeatureSaveContent {
    private Long projectId;
    private Long specId;
    private String field;
    @JsonProperty("isReduced")
    private boolean isReduced;
    private FeatureSaveItem mainFeature;
    private List<FeatureSaveItem> subFeature;
}
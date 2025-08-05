package com.codaily.project.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class FeatureSaveContent {
    private Long projectId;
    private Long specId;
    private FeatureSaveItem mainFeature;
    private List<FeatureSaveItem> subFeature;
}
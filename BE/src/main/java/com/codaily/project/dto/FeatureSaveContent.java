package com.codaily.project.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class FeatureSaveContent {
    private FeatureSaveItem mainFeature;
    private List<FeatureSaveItem> subFeature;
}
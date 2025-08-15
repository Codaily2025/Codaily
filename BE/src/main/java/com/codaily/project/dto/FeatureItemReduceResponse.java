package com.codaily.project.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class FeatureItemReduceResponse {
    private Double totalEstimatedTime;
    private Double totalAvailableTime;
    private int reducedCount;
    private int keptCount;
    private List<FeatureItemReduceItem> features;
}

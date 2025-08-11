package com.codaily.codereview.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
@Builder
public class CodeReviewAllResponseDto {
    private Long projectId;
    private Long featureId;
    private String featureName;
    private String featureField;
    private Double qualityScore;
    private Map<String, Integer> severityCount;
}

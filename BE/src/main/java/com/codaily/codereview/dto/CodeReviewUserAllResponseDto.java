package com.codaily.codereview.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Builder
public class CodeReviewUserAllResponseDto {
    private Long projectId;
    private String projectName;
    private LocalDateTime createdAt;
    private int commitCounts;
    private Long featureId;
    private String featureName;
    private String featureField;
    private Double qualityScore;
    private Map<String, Integer> severityCount;
}

package com.codaily.codereview.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeatureReviewSummaryRequestDto {
    private Long featureId;
    private String featureName;
    private Map<String, List<String>> categorizedReviews;
}


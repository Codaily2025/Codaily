package com.codaily.codereview.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeatureReviewSummaryDto {
    private Long projectId;
    private Long featureId;
    private String featureName;

    private Double overallScore;         // ✔️ 100점 만점 기준
    private String summary;              // ✔️ 전체 요약
    private String convention;           // 코딩 컨벤션
    private String refactorSuggestion;   // 리팩터링 제안
    private String complexity;           // 복잡도
    private String bugRisk;              // 버그 위험
    private String securityRisk;         // 보안 위험
}

package com.codaily.codereview.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class CodeReviewSummaryResponseDto {
    private String summary;
    private Double qualityScore;
    private String convention;
    private String performance;
    private String refactorSuggestion;
    private String complexity;
    private String bugRisk;
    private String securityRisk;
    private LocalDateTime createdAt;
}

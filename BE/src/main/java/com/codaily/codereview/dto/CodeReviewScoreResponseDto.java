package com.codaily.codereview.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CodeReviewScoreResponseDto {
    private Double qualityScore;
}

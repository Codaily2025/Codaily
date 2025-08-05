package com.codaily.codereview.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CodeReviewRunResponseDto {
    private String status;
    private String featureName;
    private Boolean implementsFeature;
    private Integer reviewCount;
}

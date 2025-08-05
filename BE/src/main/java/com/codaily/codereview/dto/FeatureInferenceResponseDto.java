package com.codaily.codereview.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeatureInferenceResponseDto {
    private Long projectId;
    private Long userId;
    private Long commitId;
    private String commitHash;
    private String featureName;
}


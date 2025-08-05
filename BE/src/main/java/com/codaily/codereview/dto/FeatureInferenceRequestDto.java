package com.codaily.codereview.dto;

import lombok.Builder;

import java.util.List;

@Builder
public class FeatureInferenceRequestDto {
    private Long projectId;
    private Long userId;
    private Long commitId;
    private String commitHash;
    private List<DiffFile> diffFiles;
    private List<String> availableFeatures;
}


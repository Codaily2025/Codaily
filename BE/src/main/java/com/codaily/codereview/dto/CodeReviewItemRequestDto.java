package com.codaily.codereview.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CodeReviewItemRequestDto {
    private Long featureId;
    private String featureName;
    private Boolean implementsFeature;
    private Map<String, Boolean> checklistEvaluation;
    private Map<String, List<String>> checklistFileMap;
    private List<FullFile> fullFiles;
}

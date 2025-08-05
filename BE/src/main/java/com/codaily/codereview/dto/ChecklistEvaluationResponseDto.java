package com.codaily.codereview.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ChecklistEvaluationResponseDto {
    private Long featureId;
    private String featureName;
    private boolean implementsFeature;
    private Map<String, Boolean> checklistEvaluation;
    private List<String> extraImplemented;
    private Map<String, List<String>> checklistFileMap;
}

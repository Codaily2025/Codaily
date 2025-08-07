package com.codaily.codereview.service;

import com.codaily.codereview.dto.*;
import com.codaily.codereview.entity.FeatureItemChecklist;

import java.util.List;
import java.util.Map;

public interface CodeReviewService {

//    void runCodeReviewAsync(CodeReviewRunRequestDto requestDto);

//    void handleFeatureInferenceResult(FeatureInferenceResponseDto response);

//    void handleChecklistEvaluationResult(ChecklistEvaluationResponseDto response);

//    void handleCodeReviewItems(FeatureReviewResultDto dto);

//    void saveCodeReviewSummary(FeatureReviewSummaryDto dto);

    void saveCodeReviewResult(CodeReviewResultRequest request);
    void saveChecklistReviewItems(CodeReviewResultRequest request);
    void saveFeatureName(Long projectId, List<String> featureNames, Long commitId);
    void updateChecklistEvaluation(Long featureId, Map<String, Boolean> checklistEvaluation, List<String> extraImplemented);
    void addChecklistFilePaths(Long featureId, Map<String, List<String>> checklistFieldMap);
}

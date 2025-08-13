package com.codaily.codereview.service;

import com.codaily.codereview.dto.*;
import com.codaily.codereview.entity.FeatureItemChecklist;

import java.util.List;
import java.util.Map;

public interface CodeReviewService {
    void saveCodeReviewResult(CodeReviewResultRequest request);

    void saveChecklistReviewItems(CodeReviewResultRequest request);

    void saveFeatureName(Long projectId, String featureName, Long commitId);

//    void updateChecklistEvaluation(Long featureId, Map<String, Boolean> checklistEvaluation, List<String> extraImplemented);

    void updateChecklistEvaluation(Long projectId, Map<String, Boolean> checklistEvaluation, List<String> extraImplemented, String featureName);

    void addChecklistFilePaths(Long featureId, Map<String, List<String>> checklistFieldMap);

    CodeReviewSummaryResponseDto getCodeReviewSummary(Long featureId);

    List<CodeReviewItemResponseDto> getCodeReviewItems(Long featureId);

    ChecklistStatusResponseDto getChecklistStatus(Long featureId);

    CodeReviewScoreResponseDto getQualityScore(Long featureId);

    SeverityByCategoryResponseDto getSeverityByCategory(Long featureId);

    List<Map<String, Object>> getAllCodeReviews(Long projectId);

    List<CodeReviewAllResponseDto> getCodeReviewsAllSummary(Long userId);
    public List<CodeReviewUserAllResponseDto> getUserAllCodeReviews(Long userId);

    public List<CodeReviewItemDto> getCodeReviewItemsAll(Long projectId, String featureName);

//    public void handleReview(CodeReviewResultRequest p);


    }

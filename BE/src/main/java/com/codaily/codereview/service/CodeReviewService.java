package com.codaily.codereview.service;

import com.codaily.codereview.dto.*;

public interface CodeReviewService {

    void runCodeReviewAsync(CodeReviewRunRequestDto requestDto);

    void handleFeatureInferenceResult(FeatureInferenceResponseDto response);

    void handleChecklistEvaluationResult(ChecklistEvaluationResponseDto response);

    void handleCodeReviewItems(FeatureReviewResultDto dto);

    void saveCodeReviewSummary(FeatureReviewSummaryDto dto);
}

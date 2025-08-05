package com.codaily.codereview.service;

import com.codaily.codereview.dto.ChecklistEvaluationResponseDto;
import com.codaily.codereview.entity.FeatureItemChecklist;

import java.util.List;

public interface FeatureItemChecklistService {

    List<FeatureItemChecklist> getChecklistByFeatureId(Long featureId);

    FeatureItemChecklist addChecklistItem(Long featureId, String item, String description);

    void saveChecklistEvaluation(ChecklistEvaluationResponseDto response);
}
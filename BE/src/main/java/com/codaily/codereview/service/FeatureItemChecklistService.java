package com.codaily.codereview.service;

import com.codaily.codereview.dto.ChecklistEvaluationResponseDto;
import com.codaily.codereview.entity.FeatureItemChecklist;

import java.util.List;
import java.util.Optional;

public interface FeatureItemChecklistService {

    FeatureItemChecklist addChecklistItem(Long featureId, String item, String description);

    void saveChecklistEvaluation(ChecklistEvaluationResponseDto response);

    List<FeatureItemChecklist> findByFeatureItem_FeatureId(Long featureId);

    FeatureItemChecklist findByFeatureItem_FeatureIdAndItem(Long featureId, String item);

    boolean existsByFeatureItem_FeatureIdAndItem(Long featureId, String item);

}
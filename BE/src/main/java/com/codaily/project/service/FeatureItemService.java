package com.codaily.project.service;

import com.codaily.codereview.entity.FeatureItemChecklist;
import com.codaily.project.dto.FeatureSaveItem;
import com.codaily.project.dto.FeatureSaveRequest;
import com.codaily.project.dto.FeatureSaveResponse;
import com.codaily.project.entity.FeatureItem;

import java.util.List;

public interface FeatureItemService {
    FeatureSaveResponse saveSpecChunk(FeatureSaveRequest chunk, Long projectId, Long specId);
    void updateFeatureItem(FeatureSaveItem request);
    FeatureSaveResponse regenerateSpec(FeatureSaveRequest chunk, Long projectId, Long specId);
    void deleteBySpecId(Long specId);
    int calculateTotalEstimatedTime(Long specId);
    FeatureItem findByProjectIdAndTitle(Long projectId, String featureName);
    FeatureItem findById(Long featureId);
    void generateFeatureItemChecklist(Long projectId);
}

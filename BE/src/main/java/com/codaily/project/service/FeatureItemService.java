package com.codaily.project.service;

import com.codaily.project.dto.FeatureSaveItem;
import com.codaily.project.dto.FeatureSaveRequest;
import com.codaily.project.dto.FeatureSaveResponse;

public interface FeatureItemService {
    FeatureSaveResponse saveSpecChunk(FeatureSaveRequest chunk, Long projectId, Long specId);
    void updateFeatureItem(FeatureSaveItem request);
    FeatureSaveResponse regenerateSpec(FeatureSaveRequest chunk, Long projectId, Long specId);
    void deleteBySpecId(Long specId);
    int calculateTotalEstimatedTime(Long specId);
}

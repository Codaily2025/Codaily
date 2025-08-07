package com.codaily.project.service;

import com.codaily.project.dto.*;

public interface FeatureItemService {
    FeatureItemResponse createFeature(Long projectId, FeatureItemCreateRequest createDto);

    FeatureItemResponse getFeature(Long projectId, Long featureId);

    FeatureItemResponse updateFeature(Long projectId, Long featureId, FeatureItemUpdateRequest update);

    void deleteFeature(Long projectId, Long featureId);

    void rescheduleProject(Long projectId);

    void scheduleProjectInitially(Long projectId);

    void updateDailyStatus();

    FeatureSaveResponse saveSpecChunk(FeatureSaveRequest chunk, Long projectId, Long specId);

//    void updateFeatureItem(FeatureSaveItem request);

    FeatureSaveResponse regenerateSpec(FeatureSaveRequest chunk, Long projectId, Long specId);

    void deleteBySpecId(Long specId);

    int calculateTotalEstimatedTime(Long specId);
}

package com.codaily.project.service;

import com.codaily.project.dto.FeatureItemCreateRequest;
import com.codaily.project.dto.FeatureItemResponse;
import com.codaily.project.dto.FeatureItemUpdateRequest;

public interface FeatureItemService {
    FeatureItemResponse createFeature(Long projectId, FeatureItemCreateRequest createDto);

    FeatureItemResponse getFeature(Long projectId, Long featureId);

    FeatureItemResponse updateFeature(Long projectId, Long featureId, FeatureItemUpdateRequest update);

    void deleteFeature(Long projectId, Long featureId);

    void rescheduleProject(Long projectId);

    void scheduleProjectInitially(Long projectId);

    void updateDailyStatus();
}

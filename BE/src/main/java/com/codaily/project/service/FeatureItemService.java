package com.codaily.project.service;

import com.codaily.project.dto.FeatureSaveItem;
import com.codaily.project.dto.FeatureSaveRequest;
import com.codaily.project.dto.FeatureSaveResponse;
import com.codaily.management.dto.FeatureScheduleResponse;
import com.codaily.project.dto.FeatureItemCreate;
import com.codaily.project.dto.FeatureItemResponse;
import com.codaily.project.dto.FeatureItemUpdate;

import java.time.LocalDate;
import java.util.List;

public interface FeatureItemService {
    FeatureItemResponse createFeature(Long projectId, FeatureItemCreate createDto);

    FeatureItemResponse getFeature(Long projectId, Long featureId);

    FeatureItemResponse updateFeature(Long projectId, Long featureId, FeatureItemUpdate update);

    void deleteFeature(Long projectId, Long featureId);

    List<FeatureScheduleResponse> getFeatureSchedules(Long projectId, Long featureId);

    List<FeatureScheduleResponse> getSchedulesByDate(Long projectId, LocalDate date);

    public void rescheduleProject(Long projectId);
    FeatureSaveResponse saveSpecChunk(FeatureSaveRequest chunk, Long projectId, Long specId);
    void updateFeatureItem(FeatureSaveItem request);
    FeatureSaveResponse regenerateSpec(FeatureSaveRequest chunk, Long projectId, Long specId);
    void deleteBySpecId(Long specId);
    int calculateTotalEstimatedTime(Long specId);
}

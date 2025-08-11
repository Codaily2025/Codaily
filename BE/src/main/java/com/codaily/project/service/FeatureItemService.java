package com.codaily.project.service;

import com.codaily.project.dto.*;
import com.codaily.project.entity.FeatureItem;

import java.util.List;
import com.codaily.project.dto.FeatureSaveRequest;
import com.codaily.project.dto.FeatureSaveResponse;


public interface FeatureItemService {
    FeatureItemResponse createFeature(Long projectId, FeatureItemCreateRequest createDto);

    FeatureItemResponse getFeature(Long projectId, Long featureId);

    FeatureItemResponse getFeature(Long featureId);

    FeatureItemResponse updateFeature(Long projectId, Long featureId, FeatureItemUpdateRequest update);

    void deleteFeature(Long projectId, Long featureId);

    FeatureSaveResponse saveSpecChunk(FeatureSaveRequest chunk, Long projectId, Long specId, String type);

//    void updateFeatureItem(FeatureSaveItem request);

    FeatureSaveResponse regenerateSpec(FeatureSaveRequest chunk, Long projectId, Long specId);

    void deleteBySpecId(Long specId);

    int calculateTotalEstimatedTime(Long specId);

    List<FeatureItem> getAllMainFeature(Long projectId);

    SubFeatureSaveResponse saveSubFeatureChunk(SubFeatureSaveRequest request, Long projectId, Long specId);

    FeatureItem findByProjectIdAndTitle(Long projectId, String featureName);

    FeatureItem findById(Long featureId);

    void generateFeatureItemChecklist(Long projectId);

    void generateExtraFeatureItemChecklist(Long featureId);

    ParentFeatureListResponse getParentFeatures(Long projectId);
}

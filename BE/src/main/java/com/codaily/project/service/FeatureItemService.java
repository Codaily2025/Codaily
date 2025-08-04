package com.codaily.project.service;

import com.codaily.project.dto.FeatureSaveItem;
import com.codaily.project.dto.FeatureSaveRequest;
import com.codaily.project.dto.FeatureSaveResponse;

public interface FeatureItemService {
    FeatureSaveResponse saveSpecChunk(FeatureSaveRequest chunk, Long projectId, Long specId);
    void updateFeatureItem(FeatureSaveItem request);
}

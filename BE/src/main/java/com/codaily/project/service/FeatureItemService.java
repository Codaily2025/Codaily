package com.codaily.project.service;

import com.codaily.project.dto.FeatureSaveRequest;

public interface FeatureItemService {
    void saveSpecChunk(FeatureSaveRequest chunk, Long projectId, Long specId);
}

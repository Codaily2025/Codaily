package com.codaily.project.service;

import com.codaily.project.dto.FeatureItemCreate;
import com.codaily.project.dto.FeatureItemResponse;

public interface FeatureItemService {
    public FeatureItemResponse createFeature(Long projectId, FeatureItemCreate createDto);
}

package com.codaily.project.service;

import com.codaily.project.entity.FeatureItem;

import java.util.List;
import java.util.Map;

public interface FeatureFieldService {
    //field 탭 목록
    public List<String> getFieldTabs(Long projectId);

    //field & status별 분류
    public Map<String, List<FeatureItem>> getFeaturesByFieldAndStatus(Long projectId, String field);

    public FeatureItem updateFeatureStatus(Long featureId, String newStatus);
}

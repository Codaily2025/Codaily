package com.codaily.codereview.service;

import com.codaily.codereview.entity.FeatureItemChecklist;

import java.util.List;
import java.util.Optional;

public interface FeatureItemChecklistService {

    List<FeatureItemChecklist> findByFeatureItem_FeatureId(Long featureId);

    FeatureItemChecklist findByFeatureItem_FeatureIdAndItem(Long featureId, String item);

    boolean existsByFeatureItem_FeatureIdAndItem(Long featureId, String item);

    void deleteFeatureChecklist(Long featureId);

}
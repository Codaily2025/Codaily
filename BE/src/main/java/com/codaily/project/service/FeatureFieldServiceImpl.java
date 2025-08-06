package com.codaily.project.service;

import com.codaily.project.entity.FeatureItem;
import com.codaily.project.repository.FeatureItemRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class FeatureFieldServiceImpl implements FeatureFieldService{
    private final FeatureItemRepository featureItemRepository;

    @Override
    public List<String> getFieldTabs(Long projectId) {
        return featureItemRepository.findDistinctFieldsByProjectId(projectId);
    }

    @Override
    public Map<String, List<FeatureItem>> getFeaturesByFieldAndStatus(Long projectId, String field) {
        List<FeatureItem> features = featureItemRepository.findByProjectIdAndField(projectId, field);

        Map<String, List<FeatureItem>> statusGroups = new LinkedHashMap<>();
        statusGroups.put("TODO", new ArrayList<>());
        statusGroups.put("IN_PROGRESS", new ArrayList<>());
        statusGroups.put("DONE", new ArrayList<>());

        for(FeatureItem feature : features){
            String status = feature.getStatus() != null ? feature.getStatus() : "TODO";
            statusGroups.computeIfAbsent(status, k -> new ArrayList<>()).add(feature);
        }
        return statusGroups;
    }

    @Override
    public FeatureItem updateFeatureStatus(Long featureId, String newStatus) {
        FeatureItem feature = featureItemRepository.findByFeatureId(featureId)
                .orElseThrow(() -> new RuntimeException("해당 기능은 존재하지 않습니다."));

        feature.setStatus(newStatus);
        FeatureItem savedFeature = featureItemRepository.save(feature);
        return savedFeature;
    }


}

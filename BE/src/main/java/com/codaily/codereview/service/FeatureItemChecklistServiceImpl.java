package com.codaily.codereview.service;

import com.codaily.codereview.entity.FeatureItemChecklist;
import com.codaily.codereview.repository.FeatureItemChecklistRepository;
import com.codaily.project.repository.FeatureItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FeatureItemChecklistServiceImpl implements FeatureItemChecklistService {

    private final FeatureItemChecklistRepository checklistRepository;

    @Override
    public List<FeatureItemChecklist> findByFeatureItem_FeatureId(Long featureId) {
        return checklistRepository.findByFeatureItem_FeatureId(featureId)
                .orElseThrow(() -> new IllegalArgumentException("체크리스트를 찾을 수 없습니다."));
    }

    @Override
    public FeatureItemChecklist findByFeatureItem_FeatureIdAndItem(Long featureId, String item) {
        return checklistRepository.findByFeatureItem_FeatureIdAndItem(featureId, item)
                .orElseThrow(() -> new IllegalArgumentException(featureId + " 체크리스트 항목을 찾을 수 없습니다. " + item));
    }

    @Override
    public boolean existsByFeatureItem_FeatureIdAndItem(Long featureId, String item) {
        return checklistRepository.existsByFeatureItem_FeatureIdAndItem(featureId, item);
    }


}

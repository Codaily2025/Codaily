package com.codaily.codereview.service;

import com.codaily.codereview.entity.FeatureItemChecklist;
import com.codaily.codereview.repository.FeatureItemChecklistRepository;
import com.codaily.project.entity.FeatureItem;
import com.codaily.project.repository.FeatureItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FeatureItemChecklistServiceImpl implements FeatureItemChecklistService {

    private final FeatureItemChecklistRepository checklistRepository;
    private final FeatureItemChecklistRepository featureItemChecklistRepository;
    private final FeatureItemRepository featureItemRepository;

    @Override
    public List<FeatureItemChecklist> findByFeatureItem_FeatureId(Long featureId) {
        return checklistRepository.findByFeatureItem_FeatureId(featureId);
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

    @Override
    public void deleteFeatureChecklist(Long featureId) {

        FeatureItem featureItem = featureItemRepository.getFeatureItemByFeatureId(featureId);

        // 사용자가 기능을 삭제했을 때, 기능에 해당하는 체크리스트 삭제
        List<FeatureItemChecklist> checklists = featureItemChecklistRepository.findByFeatureItem_FeatureId(featureId);

        if(checklists != null && !checklists.isEmpty()) {
            for(FeatureItemChecklist item : checklists) {
                featureItemChecklistRepository.delete(item);
            }
        }
    }


}

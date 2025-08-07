package com.codaily.codereview.service;

import com.codaily.codereview.dto.ChecklistEvaluationResponseDto;
import com.codaily.project.entity.FeatureItem;
import com.codaily.codereview.entity.FeatureItemChecklist;
import com.codaily.codereview.repository.FeatureItemChecklistRepository;
import com.codaily.project.repository.FeatureItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class FeatureItemChecklistServiceImpl implements FeatureItemChecklistService {

    private final FeatureItemChecklistRepository checklistRepository;
    private final FeatureItemRepository featureItemRepository;

    @Override
    public FeatureItemChecklist addChecklistItem(Long featureId, String item, String description) {
        FeatureItem feature = featureItemRepository.findById(featureId)
                .orElseThrow(() -> new IllegalArgumentException("해당 기능이 존재하지 않습니다"));

        FeatureItemChecklist checklist = FeatureItemChecklist.builder()
                .featureItem(feature)
                .item(item)
                .description(description)
                .build();

        return checklistRepository.save(checklist);
    }

    @Override
    @Transactional
    public void saveChecklistEvaluation(ChecklistEvaluationResponseDto dto) {
        Long featureId = dto.getFeatureId();
        Map<String, Boolean> evaluationMap = dto.getChecklistEvaluation();

        for (Map.Entry<String, Boolean> entry : evaluationMap.entrySet()) {
            String item = entry.getKey();
            boolean isDone = entry.getValue();

            FeatureItemChecklist checklist = checklistRepository
                    .findByFeatureItem_IdAndItem(featureId, item)
                    .orElseThrow(() -> new IllegalArgumentException("해당 checklist 항목 없음: " + item));

            checklist.updateDone(isDone); // ✅ 상태 업데이트
        }
    }

    @Override
    public List<FeatureItemChecklist> findByFeatureItem_FeatureId(Long featureId) {
        return checklistRepository.findByFeatureItem_FeatureId(featureId)
                .orElseThrow(() -> new IllegalArgumentException("체크리스트를 찾을 수 없습니다."));
    }

    @Override
    public FeatureItemChecklist findByFeatureItem_FeatureIdAndItem(Long featureId, String item) {
        return checklistRepository.findByFeatureItem_IdAndItem(featureId, item)
                .orElseThrow(() -> new IllegalArgumentException("체크리스트 항목을 찾을 수 없습니다."));
    }

    @Override
    public boolean existsByFeatureItem_FeatureIdAndItem(Long featureId, String item) {
        return checklistRepository.existsByFeatureItem_FeatureIdAndItem(featureId, item);
    }


}

package com.codaily.project.service;

import com.codaily.project.dto.FeatureSaveContent;
import com.codaily.project.dto.FeatureSaveItem;
import com.codaily.project.dto.FeatureSaveRequest;
import com.codaily.project.dto.FeatureSaveResponse;
import com.codaily.project.entity.FeatureItem;
import com.codaily.project.entity.Project;
import com.codaily.project.entity.Specification;
import com.codaily.project.repository.FeatureItemRepository;
import com.codaily.project.repository.ProjectRepository;
import com.codaily.project.repository.SpecificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FeatureItemServiceImpl implements FeatureItemService {
    private final ProjectRepository projectRepository;
    private final SpecificationRepository specificationRepository;
    private final FeatureItemRepository featureItemRepository;

    @Override
    @Transactional
    public FeatureSaveResponse saveSpecChunk(FeatureSaveRequest chunk, Long projectId, Long specId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid projectId"));
        Specification spec = specificationRepository.findById(specId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid specId"));

        // 1. 상세 기능들의 예상시간 총합 계산
        int totalEstimatedTime = chunk.getSubFeature().stream()
                .mapToInt(sub -> sub.getEstimatedTime() != null ? sub.getEstimatedTime() : 0)
                .sum();

        // 2. 주 기능 저장
        FeatureItem mainFeature = FeatureItem.builder()
                .title(chunk.getMainFeature().getTitle())
                .description(chunk.getMainFeature().getDescription())
                .field(chunk.getField())
                .project(project)
                .specification(spec)
                .estimatedTime(totalEstimatedTime)
                .isCustom(false)
                .build();

        FeatureItem savedMain = featureItemRepository.save(mainFeature);

        FeatureSaveItem mainFeatureDto = FeatureSaveItem.builder()
                .id(savedMain.getFeatureId())
                .title(savedMain.getTitle())
                .description(savedMain.getDescription())
                .estimatedTime(savedMain.getEstimatedTime())
                .priorityLevel(null)
                .build();

        // 3. 상세 기능 저장
        List<FeatureSaveItem> subFeatureDtos = chunk.getSubFeature().stream().map(sub -> {
            FeatureItem subFeature = FeatureItem.builder()
                    .title(sub.getTitle())
                    .description(sub.getDescription())
                    .field(chunk.getField())
                    .project(project)
                    .specification(spec)
                    .priorityLevel(sub.getPriorityLevel())
                    .parentFeature(savedMain)
                    .estimatedTime(sub.getEstimatedTime())
                    .isCustom(false)
                    .build();
            FeatureItem savedSub = featureItemRepository.save(subFeature);

            return FeatureSaveItem.builder()
                    .id(savedSub.getFeatureId())
                    .title(savedSub.getTitle())
                    .description(savedSub.getDescription())
                    .estimatedTime(savedSub.getEstimatedTime())
                    .priorityLevel(savedSub.getPriorityLevel())
                    .build();
        }).toList();

        FeatureSaveContent content = FeatureSaveContent.builder()
                .mainFeature(mainFeatureDto)
                .subFeature(subFeatureDtos)
                .build();

        return FeatureSaveResponse.builder()
                .type("spec")
                .content(content)
                .build();
    }

    @Override
    @Transactional
    public void updateFeatureItem(FeatureSaveItem request) {
        FeatureItem item = featureItemRepository.findById(request.getId())
                .orElseThrow(() -> new IllegalArgumentException("해당 기능이 존재하지 않습니다."));

        item.setTitle(request.getTitle());
        item.setDescription(request.getDescription());
        item.setEstimatedTime(request.getEstimatedTime());
        item.setPriorityLevel(request.getPriorityLevel());
    }

    @Override
    @Transactional
    public FeatureSaveResponse regenerateSpec(FeatureSaveRequest chunk, Long projectId, Long specId) {
        // 1. 기존 명세 항목 전부 삭제
        featureItemRepository.deleteBySpecification_SpecId(specId);

        // 2. 새로 들어온 chunk 저장
        return saveSpecChunk(chunk, projectId, specId); // 기존 저장 메서드 재사용
    }

    @Override
    @Transactional
    public void deleteBySpecId(Long specId) {
        featureItemRepository.deleteBySpecification_SpecId(specId);
    }

    @Override
    @Transactional
    public int calculateTotalEstimatedTime(Long specId) {
        Integer total = featureItemRepository.getTotalEstimatedTimeBySpecId(specId);
        return total != null ? total : 0;
    }
}

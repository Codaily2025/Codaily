package com.codaily.project.service;

import com.codaily.project.dto.FeatureSaveRequest;
import com.codaily.project.entity.FeatureItem;
import com.codaily.project.entity.Project;
import com.codaily.project.entity.Specification;
import com.codaily.project.repository.FeatureItemRepository;
import com.codaily.project.repository.ProjectRepository;
import com.codaily.project.repository.SpecificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FeatureItemServiceImpl implements FeatureItemService {
    private final ProjectRepository projectRepository;
    private final SpecificationRepository specificationRepository;
    private final FeatureItemRepository featureItemRepository;

    @Override
    @Transactional
    public void saveSpecChunk(FeatureSaveRequest chunk, Long projectId, Long specId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid projectId"));
        Specification spec = specificationRepository.findById(specId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid specId"));

        // 1. 하위 기능들의 예상시간 총합 계산
        int totalEstimatedTime = chunk.getSubFunctions().stream()
                .mapToInt(sub -> sub.getEstimatedTime() != null ? sub.getEstimatedTime() : 0)
                .sum();

        // 2. main 기능 저장
        FeatureItem mainFeature = FeatureItem.builder()
                .title(chunk.getMainFunction().getTitle())
                .description(chunk.getMainFunction().getDescription())
                .field(chunk.getFunctionGroup())
                .project(project)
                .specification(spec)
                .estimatedTime(totalEstimatedTime) // 총합 예상시간 저장
                .isCustom(false)
                .build();

        featureItemRepository.save(mainFeature);

        // 3. 하위 기능 저장
        for (FeatureSaveRequest.SubFunction sub : chunk.getSubFunctions()) {
            FeatureItem subFeature = FeatureItem.builder()
                    .title(sub.getTitle())
                    .description(sub.getDescription())
                    .field(chunk.getFunctionGroup())
                    .project(project)
                    .specification(spec)
                    .priorityLevel(sub.getPriorityLevel())
                    .parentFeature(mainFeature)
                    .estimatedTime(sub.getEstimatedTime())
                    .isCustom(false)
                    .build();

            featureItemRepository.save(subFeature);
        }
    }

}

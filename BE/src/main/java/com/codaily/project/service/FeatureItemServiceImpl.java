package com.codaily.project.service;

import com.codaily.global.exception.ProjectNotFoundException;
import com.codaily.project.dto.FeatureItemCreate;
import com.codaily.project.dto.FeatureItemResponse;
import com.codaily.project.entity.FeatureItem;
import com.codaily.project.entity.Project;
import com.codaily.project.entity.Specification;
import com.codaily.project.exception.FeatureNotFoundException;
import com.codaily.project.exception.SpecificationNotFoundException;
import com.codaily.project.repository.FeatureItemRepository;
import com.codaily.project.repository.ProjectRepository;
import com.codaily.project.repository.SpecificationRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class FeatureItemServiceImpl implements FeatureItemService {

    private final ProjectRepository projectRepository;
    private final SpecificationRepository specificationRepository;
    private final FeatureItemRepository featureItemRepository;

    @Override
    public FeatureItemResponse createFeature(Long projectId, FeatureItemCreate featureItem) {
        if (projectId == null || featureItem == null) {
            throw new IllegalArgumentException("프로젝트 ID와 생성 정보는 필수입니다.");
        }
        if (featureItem.getTitle() == null || featureItem.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("기능 제목은 필수입니다.");
        }

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException(projectId));

        FeatureItem feature = FeatureItem.builder()
                .title(featureItem.getTitle().trim())
                .description(featureItem.getDescription())
                .field(featureItem.getField())
                .category(featureItem.getCategory())
                .priorityLevel(featureItem.getPriorityLevel())
                .estimatedTime(featureItem.getEstimatedTime())
                .isCustom(featureItem.getIsCustom())
                .isSelected(true)
                .isReduced(false)
                .project(project)
                .build();

        // 스펙 설정
        if (featureItem.getSpecificationId() != null) {
            Specification specification = specificationRepository.findById(featureItem.getSpecificationId())
                    .orElseThrow(() -> new SpecificationNotFoundException(featureItem.getSpecificationId()));
            feature.setSpecification(specification);
        }

        // 부모 기능 설정
        if (featureItem.getParentFeatureId() != null) {
            FeatureItem parentFeature = featureItemRepository.findByProject_ProjectIdAndFeatureId(
                            projectId, featureItem.getParentFeatureId())
                    .orElseThrow(() -> new FeatureNotFoundException(featureItem.getParentFeatureId()));
            feature.setParentFeature(parentFeature);
        }

        FeatureItem savedFeature = featureItemRepository.save(feature);
        log.info("기능 생성 완료 - 프로젝트 ID: {}, 기능 ID: {}", projectId, savedFeature.getFeatureId());

        return convertToResponseDto(feature);
    }

    private FeatureItemResponse convertToResponseDto(FeatureItem feature) {
        return FeatureItemResponse.builder()
                .featureId(feature.getFeatureId())
                .title(feature.getTitle())
                .description(feature.getDescription())
                .field(feature.getField())
                .category(feature.getCategory())
                .status(feature.getStatus())
                .priorityLevel(feature.getPriorityLevel())
                .estimatedTime(feature.getEstimatedTime())
                .isSelected(feature.getIsSelected())
                .isCustom(feature.getIsCustom())
                .isReduced(feature.getIsReduced())
                .projectId(feature.getProject().getProjectId())
                .specificationId(feature.getSpecification() != null ? feature.getSpecification().getSpecId() : null)
                .parentFeatureId(feature.getParentFeature() != null ? feature.getParentFeature().getFeatureId() : null)
                .build();
    }
}

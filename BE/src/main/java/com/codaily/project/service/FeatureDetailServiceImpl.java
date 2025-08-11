package com.codaily.project.service;

import com.codaily.codereview.repository.CodeCommitRepository;
import com.codaily.project.dto.*;
import com.codaily.project.entity.*;
import com.codaily.project.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FeatureDetailServiceImpl implements FeatureDetailService {

    private final FeatureItemRepository featureItemRepository;
    private final CodeCommitRepository codeCommitRepository;
    private final ProjectRepository projectRepository;

    @Autowired
    private ProductivityEventService productivityEventService;

    @Override
    public FeatureDetailResponse getFeatureDetail(Long projectId, Long featureId, Long userId) {
        try {
            log.info("기능 상세 조회 - projectId: {}, featureId: {}, userId: {}", projectId, featureId, userId);

            // 기능 조회 및 검증
            FeatureItem feature = validateFeatureAccess(projectId, featureId, userId);

            // 상위 기능 정보 조회
            FeatureDetailResponse.ParentFeatureInfo parentFeatureInfo = null;
            if (feature.getParentFeature() != null) {
                parentFeatureInfo = convertToParentFeatureInfo(feature.getParentFeature());
            }

            // 하위 기능들 조회
            List<FeatureDetailResponse.SubFeatureInfo> subFeatures = getSubFeatures(featureId);

            // 메트릭 계산
            FeatureDetailResponse.FeatureMetrics metrics = calculateFeatureMetrics(feature);

            // 응답 데이터 구성
            FeatureDetailResponse.FeatureDetailData data = FeatureDetailResponse.FeatureDetailData.builder()
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
                    .createdAt(feature.getCreatedAt())
                    .updatedAt(feature.getUpdatedAt())
                    .completedAt(feature.getCompletedAt())
                    .parentFeature(parentFeatureInfo)
                    .subFeatures(subFeatures)
                    .metrics(metrics)
                    .build();

            return FeatureDetailResponse.builder()
                    .success(true)
                    .data(data)
                    .build();

        } catch (Exception e) {
            log.error("기능 상세 조회 실패 - featureId: {}, error: {}", featureId, e.getMessage(), e);
            return FeatureDetailResponse.builder()
                    .success(false)
                    .message(e.getMessage())
                    .build();
        }
    }

    @Override
    @Transactional
    public FeatureDetailResponse updateFeature(Long projectId, Long featureId, FeatureUpdateRequest request, Long userId) {
        try {
            log.info("기능 수정 - projectId: {}, featureId: {}, userId: {}", projectId, featureId, userId);

            // 기능 조회 및 검증
            FeatureItem feature = validateFeatureAccess(projectId, featureId, userId);

            // 기능 정보 업데이트
            updateFeatureInfo(feature, request);
            featureItemRepository.save(feature);

            log.info("기능 수정 성공 - featureId: {}", featureId);

            // 업데이트된 정보 반환
            return getFeatureDetail(projectId, featureId, userId);

        } catch (Exception e) {
            log.error("기능 수정 실패 - featureId: {}, error: {}", featureId, e.getMessage(), e);
            return FeatureDetailResponse.builder()
                    .success(false)
                    .message(e.getMessage())
                    .build();
        }
    }

    @Override
    @Transactional
    public SubFeatureCreateResponse createSubFeature(Long projectId, Long parentFeatureId, SubFeatureCreateRequest request, Long userId) {
        try {
            log.info("하위 기능 생성 - projectId: {}, parentFeatureId: {}, userId: {}", projectId, parentFeatureId, userId);

            // 부모 기능 조회 및 검증
            FeatureItem parentFeature = validateFeatureAccess(projectId, parentFeatureId, userId);

            // 새로운 하위 기능 생성
            FeatureItem subFeature = FeatureItem.builder()
                    .project(parentFeature.getProject())
                    .specification(parentFeature.getSpecification())
                    .parentFeature(parentFeature)
                    .title(request.getTitle())
                    .description(request.getDescription())
                    .field(parentFeature.getField())
                    .category(request.getCategory())
                    .status(request.getStatus() != null ? request.getStatus() : "TODO")
                    .priorityLevel(request.getPriorityLevel())
                    .estimatedTime(request.getEstimatedTime())
                    .isCustom(true)
                    .isSelected(true)
                    .isReduced(false)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            FeatureItem savedSubFeature = featureItemRepository.save(subFeature);

            // 응답 데이터 구성
            SubFeatureCreateResponse.SubFeatureData data = SubFeatureCreateResponse.SubFeatureData.builder()
                    .featureId(savedSubFeature.getFeatureId())
                    .title(savedSubFeature.getTitle())
                    .description(savedSubFeature.getDescription())
                    .category(savedSubFeature.getCategory())
                    .status(savedSubFeature.getStatus())
                    .priorityLevel(savedSubFeature.getPriorityLevel())
                    .estimatedTime(savedSubFeature.getEstimatedTime())
                    .parentFeatureId(parentFeatureId)
                    .createdAt(savedSubFeature.getCreatedAt())
                    .build();

            return SubFeatureCreateResponse.builder()
                    .success(true)
                    .data(data)
                    .message("하위 기능이 성공적으로 생성되었습니다.")
                    .build();

        } catch (Exception e) {
            log.error("하위 기능 생성 실패 - parentFeatureId: {}, error: {}", parentFeatureId, e.getMessage(), e);
            return SubFeatureCreateResponse.builder()
                    .success(false)
                    .message(e.getMessage())
                    .build();
        }
    }

    @Override
    public CalendarFeatureResponse getCalendarFeatures(Long projectId, LocalDate date, Long userId) {
        try {
            log.info("캘린더 기능 조회 - projectId: {}, date: {}, userId: {}", projectId, date, userId);

            // 프로젝트 접근 권한 검증
            validateProjectAccess(projectId, userId);

            // 특정 날짜의 기능들 조회 (생성, 수정, 완료 날짜 기준)
            List<FeatureItem> features = featureItemRepository.findByProjectIdAndDate(projectId, date);

            // FeatureItem을 FeatureInfo로 변환
            List<CalendarFeatureResponse.FeatureInfo> featureInfos = features.stream()
                    .map(this::convertToFeatureInfo)
                    .collect(Collectors.toList());

            // 응답 데이터 구성
            CalendarFeatureResponse.CalendarFeatureData data = CalendarFeatureResponse.CalendarFeatureData.builder()
                    .date(date)
                    .features(featureInfos)
                    .build();

            return CalendarFeatureResponse.builder()
                    .success(true)
                    .data(data)
                    .build();

        } catch (Exception e) {
            log.error("캘린더 기능 조회 실패 - projectId: {}, date: {}, error: {}",
                    projectId, date, e.getMessage(), e);
            return CalendarFeatureResponse.builder()
                    .success(false)
                    .message(e.getMessage())
                    .build();
        }
    }

    @Override
    public SubFeatureListResponse getSubFeatures(Long projectId, Long featureId, Long userId) {
        try {
            log.info("하위 기능 목록 조회 - projectId: {}, featureId: {}, userId: {}", projectId, featureId, userId);

            // 기능 조회 및 검증
            FeatureItem feature = validateFeatureAccess(projectId, featureId, userId);

            // 하위 기능들 조회
            List<FeatureDetailResponse.SubFeatureInfo> subFeatures = getSubFeatures(featureId);

            SubFeatureListResponse.SubFeatureListData data = SubFeatureListResponse.SubFeatureListData.builder()
                    .parentFeatureId(featureId)
                    .parentFeatureTitle(feature.getTitle())
                    .subFeatures(subFeatures)
                    .build();

            return SubFeatureListResponse.builder()
                    .success(true)
                    .data(data)
                    .build();

        } catch (Exception e) {
            log.error("하위 기능 목록 조회 실패 - featureId: {}, error: {}", featureId, e.getMessage(), e);
            return SubFeatureListResponse.builder()
                    .success(false)
                    .message(e.getMessage())
                    .build();
        }
    }

    @Override
    @Transactional
    public void deleteFeature(Long projectId, Long featureId, Long userId) {
        log.info("기능 삭제 - projectId: {}, featureId: {}, userId: {}", projectId, featureId, userId);

        // 기능 조회 및 검증
        FeatureItem feature = validateFeatureAccess(projectId, featureId, userId);

        // 하위 기능들도 함께 삭제 (CASCADE 설정으로 자동 처리됨)
        featureItemRepository.delete(feature);

        log.info("기능 삭제 완료 - featureId: {}", featureId);
    }



    // === Helper Methods ===

    private FeatureItem validateFeatureAccess(Long projectId, Long featureId, Long userId) {
        FeatureItem feature = featureItemRepository.findById(featureId)
                .orElseThrow(() -> new IllegalArgumentException("기능을 찾을 수 없습니다."));

        if (!feature.getProject().getProjectId().equals(projectId)) {
            throw new IllegalArgumentException("해당 프로젝트의 기능이 아닙니다.");
        }

        validateProjectAccess(projectId, userId);
        return feature;
    }

    private void validateProjectAccess(Long projectId, Long userId) {
        boolean hasAccess = projectRepository.findById(projectId)
                .map(project -> project.getUser().getUserId().equals(userId))
                .orElseThrow(() -> new IllegalArgumentException("프로젝트를 찾을 수 없습니다."));

        if (!hasAccess) {
            throw new IllegalArgumentException("해당 프로젝트에 대한 접근 권한이 없습니다.");
        }
    }

    private FeatureDetailResponse.ParentFeatureInfo convertToParentFeatureInfo(FeatureItem parentFeature) {
        return FeatureDetailResponse.ParentFeatureInfo.builder()
                .featureId(parentFeature.getFeatureId())
                .title(parentFeature.getTitle())
                .description(parentFeature.getDescription())
                .field(parentFeature.getField())
                .category(parentFeature.getCategory())
                .priorityLevel(parentFeature.getPriorityLevel())
                .estimatedTime(parentFeature.getEstimatedTime())
                .build();
    }

    private List<FeatureDetailResponse.SubFeatureInfo> getSubFeatures(Long parentFeatureId) {
        List<FeatureItem> subFeatures = featureItemRepository.findSubFeaturesByParentId(parentFeatureId);

        return subFeatures.stream()
                .map(feature -> FeatureDetailResponse.SubFeatureInfo.builder()
                        .featureId(feature.getFeatureId())
                        .title(feature.getTitle())
                        .description(feature.getDescription())
                        .category(feature.getCategory())
                        .status(feature.getStatus())
                        .priorityLevel(feature.getPriorityLevel())
                        .estimatedTime(feature.getEstimatedTime())
                        .createdAt(feature.getCreatedAt())
                        .updatedAt(feature.getUpdatedAt())
                        .completedAt(feature.getCompletedAt())
                        .build())
                .collect(Collectors.toList());
    }

    private FeatureDetailResponse.FeatureMetrics calculateFeatureMetrics(FeatureItem feature) {
        // 하위 기능 관련 메트릭
        List<FeatureItem> subFeatures = featureItemRepository.findSubFeaturesByParentId(feature.getFeatureId());
        int totalSubFeatures = subFeatures.size();
        int completedSubFeatures = (int) subFeatures.stream()
                .mapToLong(f -> "DONE".equals(f.getStatus()) ? 1 : 0)
                .sum();

        double progressPercentage = totalSubFeatures > 0 ?
                (double) completedSubFeatures / totalSubFeatures * 100.0 : 0.0;

        // 커밋 수 계산 (최근 30일)
        int commitsCount = 0;
        LocalDateTime lastActivityAt = feature.getUpdatedAt();

        try {
            LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
            var commits = codeCommitRepository.findByProject_ProjectIdAndCommittedAtBetween(
                    feature.getProject().getProjectId(), thirtyDaysAgo, LocalDateTime.now());
            commitsCount = commits.size();

            // 최근 커밋 시간
            if (!commits.isEmpty()) {
                LocalDateTime lastCommitTime = commits.stream()
                        .map(commit -> commit.getCommittedAt())
                        .max(LocalDateTime::compareTo)
                        .orElse(null);

                if (lastCommitTime != null && lastCommitTime.isAfter(lastActivityAt)) {
                    lastActivityAt = lastCommitTime;
                }
            }
        } catch (Exception e) {
            log.warn("커밋 수 조회 실패 - featureId: {}", feature.getFeatureId());
        }

        return FeatureDetailResponse.FeatureMetrics.builder()
                .totalSubFeatures(totalSubFeatures)
                .completedSubFeatures(completedSubFeatures)
                .progressPercentage(Math.round(progressPercentage * 10.0) / 10.0)
                .recentCommitsCount(commitsCount)
                .lastActivityAt(lastActivityAt)
                .build();
    }

    private void updateFeatureInfo(FeatureItem feature, FeatureUpdateRequest request) {
        boolean isModified = false;

        if (request.getTitle() != null && !request.getTitle().trim().isEmpty()) {
            feature.setTitle(request.getTitle().trim());
            isModified = true;
        }

        if (request.getDescription() != null) {
            feature.setDescription(request.getDescription().trim());
            isModified = true;
        }

        if (request.getField() != null) {
            feature.setField(request.getField().trim());
            isModified = true;
        }

        if (request.getCategory() != null) {
            feature.setCategory(request.getCategory().trim());
            isModified = true;
        }

        if (request.getStatus() != null) {
            String oldStatus = feature.getStatus();
            feature.setStatus(request.getStatus());
            isModified = true;

            // 상태 변경에 따른 completedAt 처리
            if ("DONE".equals(request.getStatus()) && !"DONE".equals(oldStatus)) {
                feature.setCompletedAt(LocalDateTime.now());
                //생산성 즉시 업데이트
                productivityEventService.updateProductivityOnTaskComplete(
                        feature.getProject().getProjectId(),
                        feature.getProject().getUser().getUserId(),
                        LocalDate.now()
                );
            } else if (!"DONE".equals(request.getStatus()) && "DONE".equals(oldStatus)) {
                feature.setCompletedAt(null);
            }
        }

        if (request.getPriorityLevel() != null) {
            feature.setPriorityLevel(request.getPriorityLevel());
            isModified = true;
        }

        if (request.getEstimatedTime() != null) {
            feature.setEstimatedTime(request.getEstimatedTime());
            isModified = true;
        }

        if (request.getIsSelected() != null) {
            feature.setIsSelected(request.getIsSelected());
            isModified = true;
        }

        if (request.getIsReduced() != null) {
            feature.setIsReduced(request.getIsReduced());
            isModified = true;
        }

        if (isModified) {
            feature.setUpdatedAt(LocalDateTime.now());
        }
    }

    private CalendarFeatureResponse.FeatureInfo convertToFeatureInfo(FeatureItem feature) {
        String parentFeatureTitle = null;
        if (feature.getParentFeature() != null) {
            parentFeatureTitle = feature.getParentFeature().getTitle();
        }

        return CalendarFeatureResponse.FeatureInfo.builder()
                .featureId(feature.getFeatureId())
                .title(feature.getTitle())
                .description(feature.getDescription())
                .field(feature.getField())
                .category(feature.getCategory())
                .status(feature.getStatus())
                .priorityLevel(feature.getPriorityLevel())
                .estimatedTime(feature.getEstimatedTime())
                .parentFeatureTitle(parentFeatureTitle)
                .createdAt(feature.getCreatedAt())
                .updatedAt(feature.getUpdatedAt())
                .completedAt(feature.getCompletedAt())
                .build();
    }
}
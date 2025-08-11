// RecommendedTaskServiceImpl.java 수정된 버전
package com.codaily.retrospective.service;

import com.codaily.project.entity.FeatureItem;
import com.codaily.project.repository.FeatureItemRepository;
import com.codaily.retrospective.dto.RecommendedTaskListResponse;
import com.codaily.retrospective.dto.RecommendedTaskResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecommendedTaskServiceImpl implements RecommendedTaskService {

    private final FeatureItemRepository featureItemRepository;

    @Override
    public RecommendedTaskListResponse getRecommendedTasks(Long projectId, Integer limit) {
        if (limit == null || limit <= 0) {
            limit = 5; // 기본값
        }

        // Pageable을 사용하여 제한된 수의 결과만 조회
        Pageable pageable = PageRequest.of(0, limit);

        // 완료되거나 진행중인 작업을 제외하고 TODO 상태의 작업들을 우선순위 순으로 조회
        List<FeatureItem> candidateTasks = featureItemRepository.findCandidateTasksForRecommendation(
                projectId,
                pageable
        );

        if (candidateTasks.isEmpty()) {
            return RecommendedTaskListResponse.builder()
                    .projectId(projectId)
                    .totalRecommendedTasks(0)
                    .recommendedTasks(List.of())
                    .message("현재 추천할 수 있는 작업이 없습니다. 모든 작업이 완료되었거나 진행 중입니다.")
                    .build();
        }

        List<RecommendedTaskResponse> recommendedTasks = candidateTasks.stream()
                .map(this::convertToRecommendedTask)
                .collect(Collectors.toList());

        log.info("프로젝트 {}에 대해 {}개의 추천 작업을 조회했습니다.", projectId, recommendedTasks.size());

        return RecommendedTaskListResponse.builder()
                .projectId(projectId)
                .totalRecommendedTasks(recommendedTasks.size())
                .recommendedTasks(recommendedTasks)
                .message(null)
                .build();
    }

    private RecommendedTaskResponse convertToRecommendedTask(FeatureItem featureItem) {
        String reason = generateRecommendationReason(featureItem);

        return RecommendedTaskResponse.builder()
                .featureId(featureItem.getFeatureId())
                .title(featureItem.getTitle())
                .description(featureItem.getDescription())
                .field(featureItem.getField())
                .category(featureItem.getCategory())
                .priorityLevel(featureItem.getPriorityLevel())
                .estimatedTime(featureItem.getEstimatedTime())
                .reason(reason)
                .build();
    }

    private String generateRecommendationReason(FeatureItem featureItem) {
        StringBuilder reason = new StringBuilder();

        // 우선순위 기반 이유
        if (featureItem.getPriorityLevel() != null) {
            if (featureItem.getPriorityLevel() >= 4) {
                reason.append("낮은 우선순위로 시간이 있을 때 진행하기 좋은 작업입니다. ");
            } else if (featureItem.getPriorityLevel() == 3) {
                reason.append("중간 우선순위로 다음에 진행할 수 있는 작업입니다. ");
            }
        }

        // 예상 시간 기반 이유
        if (featureItem.getEstimatedTime() != null) {
            if (featureItem.getEstimatedTime() <= 2.0) {
                reason.append("짧은 시간에 완료할 수 있어 부담 없이 시작할 수 있습니다. ");
            } else if (featureItem.getEstimatedTime() <= 4.0) {
                reason.append("적당한 분량으로 체계적으로 진행하기 좋습니다. ");
            } else {
                reason.append("상당한 시간이 필요하므로 충분한 시간을 확보한 후 진행하세요. ");
            }
        }

        // 카테고리별 이유
        if (featureItem.getCategory() != null) {
            switch (featureItem.getCategory().toLowerCase()) {
                case "ui/ux":
                    reason.append("사용자 경험 개선에 도움이 됩니다.");
                    break;
                case "api":
                    reason.append("백엔드 기능 강화에 기여합니다.");
                    break;
                case "database":
                    reason.append("데이터 관리 개선에 필요합니다.");
                    break;
                case "testing":
                    reason.append("코드 품질 향상에 중요합니다.");
                    break;
                default:
                    reason.append("프로젝트 완성도 향상에 기여합니다.");
            }
        } else {
            reason.append("프로젝트 진행에 도움이 되는 작업입니다.");
        }

        return reason.toString().trim();
    }
}
package com.codaily.codereview.service;

import com.codaily.auth.entity.User;
import com.codaily.auth.service.UserService;
import com.codaily.codereview.controller.CodeReviewResponseMapper;
import com.codaily.codereview.dto.*;
import com.codaily.codereview.entity.CodeCommit;
import com.codaily.codereview.entity.CodeReview;
import com.codaily.codereview.entity.CodeReviewItem;
import com.codaily.codereview.entity.FeatureItemChecklist;
import com.codaily.codereview.repository.CodeReviewItemRepository;
import com.codaily.codereview.repository.CodeReviewRepository;
import com.codaily.codereview.repository.FeatureItemChecklistRepository;
import com.codaily.project.entity.FeatureItem;
import com.codaily.project.entity.Project;
import com.codaily.project.repository.FeatureItemRepository;
import com.codaily.project.repository.ProjectRepository;
import com.codaily.project.service.FeatureItemService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CodeReviewServiceImpl implements CodeReviewService {

    private final FeatureItemRepository featureItemRepository;
    private final FeatureItemChecklistRepository featureItemChecklistRepository;
    private final CodeReviewItemRepository codeReviewItemRepository;
    private final ProjectRepository projectRepository;
    private final CodeReviewRepository codeReviewRepository;
    private final FeatureItemChecklistService featureItemChecklistService;
    private final FeatureItemService featureItemService;
    private final CodeCommitService codeCommitService;
    private final CodeReviewItemService codeReviewItemService;
    private final CodeReviewResponseMapper codeReviewResponseMapper;
    private final UserService userService;


    @Override
    public void saveCodeReviewResult(CodeReviewResultRequest request) {
        Map<String, String> summary = request.getReviewSummary();

        CodeReview review = CodeReview.builder()
                .featureItem(featureItemRepository.getReferenceById(request.getFeatureId()))
                .project(projectRepository.getReferenceById(request.getProjectId()))
                .qualityScore(Double.parseDouble(summary.getOrDefault("점수", "0")))
                .summary(summary.getOrDefault("요약", ""))
                .convention(summary.getOrDefault("코딩 컨벤션", ""))
                .bugRisk(summary.getOrDefault("버그 가능성", ""))
                .securityRisk(summary.getOrDefault("보안 위험", ""))
                .complexity(summary.getOrDefault("성능 최적화", ""))
                .refactorSuggestion(summary.getOrDefault("리팩터링 제안", ""))
                .build();

        codeReviewRepository.save(review);
        FeatureItem featureItem = featureItemService.findById(request.getFeatureId());
        featureItem.setStatus("DONE");
    }

    // 체크리스트 항목 코드리뷰 저장
    @Override
    @Transactional
    public void saveChecklistReviewItems(CodeReviewResultRequest request) {
        Long featureId = request.getFeatureId();

        for (CodeReviewItemDto review : request.getCodeReviewItems()) {
            String checklistItem = review.getChecklistItem();
            String category = review.getCategory();

            List<ReviewItemDto> items = review.getItems();
            if (items.isEmpty()) continue;

            for (ReviewItemDto item : items) {
                String filePath = item.getFilePath();
                String lineRange = item.getLineRange();
                String severity = item.getSeverity();
                String message = item.getMessage();
                FeatureItem featureItem = featureItemRepository.getReferenceById(featureId);
                CodeReviewItem entity = CodeReviewItem.builder()
                        .featureItem(featureItem)
                        .featureItemChecklist(featureItemChecklistService.findByFeatureItem_FeatureIdAndItem(featureId, checklistItem))
                        .filePath(filePath)
                        .lineRange(lineRange)
                        .severity(severity)
                        .message(message)
                        .build();

                codeReviewItemRepository.save(entity);
            }
        }
    }


    @Override
    public void saveFeatureName(Long projectId, List<String> featureNames, Long commitId) {
        CodeCommit commit = codeCommitService.findById(commitId);

        for(String name : featureNames) {
            commit.addFeatureName(name);

            FeatureItem featureItem = featureItemService.findByProjectIdAndTitle(projectId, name);
            commit.addFeatureItem(featureItem);
        }

    }

    @Override
    @Transactional
    public void updateChecklistEvaluation(Long featureId, Map<String, Boolean> checklistEvaluation,
                                          List<String> extraImplemented) {
        for(Map.Entry<String, Boolean> entry : checklistEvaluation.entrySet()) {
            String item = entry.getKey();
            boolean isDone = entry.getValue();

            FeatureItemChecklist featureItemChecklist = featureItemChecklistService.findByFeatureItem_FeatureIdAndItem(featureId, item);
            featureItemChecklist.updateDone(isDone);
        }

        // 추가 구현 항목 처리
        for(String extra : extraImplemented) {
            boolean exists = featureItemChecklistService.existsByFeatureItem_FeatureIdAndItem(featureId, extra);
            FeatureItem featureItem = featureItemRepository.getReferenceById(featureId);

            if(!exists) {
                FeatureItemChecklist checklist = FeatureItemChecklist.builder()
                        .featureItem(featureItem)
                        .item(extra)
                        .description(extra)
                        .done(true)
                        .build();

                featureItemChecklistRepository.save(checklist);
            }
        }
    }

    @Override
    public void addChecklistFilePaths(Long featureId, Map<String, List<String>> checklistFieldMap) {
        List<FeatureItemChecklist> checklists = featureItemChecklistService.findByFeatureItem_FeatureId(featureId);

        for (Map.Entry<String, List<String>> entry : checklistFieldMap.entrySet()) {
            String item = entry.getKey();
            List<String> filePaths = entry.getValue();

            for (FeatureItemChecklist checklist : checklists) {
                if (checklist.getItem().equals(item)) {
                    checklist.updateFilePaths(filePaths);
                    continue;
                }
            }
        }

    }

    @Override
    public CodeReviewSummaryResponseDto getCodeReviewSummary(Long featureId) {
        CodeReview review = codeReviewRepository.findByFeatureItem_FeatureId(featureId)
                .orElseThrow(() -> new IllegalArgumentException("해당 기능에 대한 코드 리뷰 없음"));

        return CodeReviewSummaryResponseDto.builder()
                .summary(review.getSummary())
                .convention(review.getConvention())
                .refactorSuggestion(review.getRefactorSuggestion())
                .performance(review.getPerformance())
                .complexity(review.getComplexity())
                .bugRisk(review.getBugRisk())
                .securityRisk(review.getSecurityRisk())
                .qualityScore(review.getQualityScore())
                .createdAt(review.getCreatedAt())
                .build();
    }

    @Override
    public List<CodeReviewItemResponseDto> getCodeReviewItems(Long featureId) {
        List<CodeReviewItem> items = codeReviewItemRepository.findByFeatureItem_FeatureId(featureId);

        return items.stream().map(item -> CodeReviewItemResponseDto.builder()
                .category(item.getCategory())
                .filePath(item.getFilePath())
                .lineRange(item.getLineRange())
                .severity(item.getSeverity())
                .message(item.getMessage())
                .build()).toList();
    }

    @Override
    public ChecklistStatusResponseDto getChecklistStatus(Long featureId) {
        List<FeatureItemChecklist> checklists = featureItemChecklistService.findByFeatureItem_FeatureId(featureId);

        List<ChecklistItemStatusDto> items = checklists.stream()
                .map(item -> ChecklistItemStatusDto.builder()
                        .item(item.getItem()).done(item.isDone()).build()).toList();

        boolean allImplemented = items.stream().allMatch(ChecklistItemStatusDto::isDone);

        return ChecklistStatusResponseDto.builder()
                .allImplemented(allImplemented)
                .items(items).build();
    }

    @Override
    public CodeReviewScoreResponseDto getQualityScore(Long featureId) {
        CodeReview review = codeReviewRepository.findByFeatureItem_FeatureId(featureId)
                .orElseThrow(() -> new IllegalArgumentException("해당 기능에 대한 코드 리뷰 없음"));

        return CodeReviewScoreResponseDto.builder().qualityScore(review.getQualityScore()).build();
    }

    @Override
    public SeverityByCategoryResponseDto getSeverityByCategory(Long featureId) {
        List<CodeReviewItem> items = codeReviewItemService.getCodeReviewItemById(featureId);

        // Map<category, Map<severity, count>>
        Map<String, Map<String, Integer>> categorySeverityMap = new HashMap<>();

        for(CodeReviewItem item : items) {
            String category = item.getCategory();
            String severity = item.getSeverity();

            categorySeverityMap.computeIfAbsent(category, k -> new HashMap<>())
                    .merge(severity, 1, Integer::sum);
        }
        return SeverityByCategoryResponseDto.builder().categorySeverityCount(categorySeverityMap).build();
    }

    @Override
    public List<Map<String, Object>> getAllCodeReviews(Long projectId) {
        // 프로젝트 조회
        Project project = projectRepository.getProjectByProjectId(projectId);

        return featureItemRepository.findByProject_ProjectId(projectId)
                .stream()
                .map(feature -> {
                    CodeReviewSummaryResponseDto summaryDto = getCodeReviewSummary(feature.getFeatureId());
                    List<CodeReviewItemResponseDto> items = getCodeReviewItems(feature.getFeatureId());

                    return codeReviewResponseMapper.toDetailResponse(
                            feature.getFeatureId(),
                            summaryDto,
                            items
                    );
                })
                .toList();
    }

    @Override
    public List<CodeReviewAllResponseDto> getCodeReviewsAllSummary(Long projectId) {
        // 프로젝트 존재 확인 (선택)
        Project project = projectRepository.getProjectByProjectId(projectId);
        if (project == null) {
            throw new IllegalArgumentException("프로젝트가 존재하지 않습니다: " + projectId);
        }

        // 기능 목록 조회
        List<FeatureItem> featureItems = featureItemRepository.findByProject_ProjectId(projectId);

        return featureItems.stream()
                .map(feature -> {
                    Long featureId = feature.getFeatureId();
                    String featureTitle = feature.getTitle();
                    String featureField = feature.getField();

                    // 점수
                    CodeReviewScoreResponseDto scoreDto = getQualityScore(featureId);

                    // severity 합산(카테고리 무시하고 severity 기준으로만 카운트)
                    Map<String, Integer> severityCount = getSeverityCount(featureId);

                    return CodeReviewAllResponseDto.builder()
                            .projectId(projectId)
                            .featureId(featureId)
                            .featureName(featureTitle)
                            .featureField(featureField)
                            .qualityScore(scoreDto != null ? scoreDto.getQualityScore() : null)
                            .severityCount(severityCount)
                            .build();
                })
                .collect(java.util.stream.Collectors.toList());
    }


    /**
     * featureId에 속한 모든 CodeReviewItem의 severity를 합산해서
     * Map<severity, count> 형태로 반환
     * 예) {"낮음": 1, "중간": 2, "높음": 0}
     */
    private Map<String, Integer> getSeverityCount(Long featureId) {
        // 이름이 byId로 되어 있으면 혼동됨. 가능하면 byFeatureId로 바꾸는 걸 추천.
        List<CodeReviewItem> items = codeReviewItemService.getCodeReviewItemById(featureId);

        Map<String, Integer> severityCount = new HashMap<>();
        for (CodeReviewItem item : items) {
            String severity = item.getSeverity();
            if (severity == null) continue;
            severityCount.merge(severity, 1, Integer::sum);
        }
        return severityCount;
    }

    @Override
    public List<CodeReviewUserAllResponseDto> getUserAllCodeReviews(Long userId) {
        // 1) 유저 확인
        User user = userService.findById(userId);
        if (user == null) {
            throw new IllegalArgumentException("유저가 존재하지 않습니다: " + userId);
        }

        // 2) 유저의 모든 프로젝트 조회
        //   - 레포지토리에 아래 메서드가 있으면 사용: findByUser_UserId(Long userId)
        //   - 없으면 user.getProjects() 사용
        List<Project> projects = projectRepository.findByUser_UserId(userId);
        if (projects == null || projects.isEmpty()) {
            return java.util.Collections.emptyList();
        }

        // 3) 모든 프로젝트의 기능들 모아서 요약 생성
        return projects.stream()
                .flatMap(project -> {
                    List<FeatureItem> features = featureItemRepository.findByProject_ProjectId(project.getProjectId());
                    return features.stream();
                })
                .map(feature -> {
                    Long featureId = feature.getFeatureId();

                    // 점수 (리뷰 없을 수도 있으니 NPE/예외 방지)
                    Double qualityScore = getQualityScoreSafe(featureId);

                    // severity 합산(카테고리 무시, severity만 집계)
                    Map<String, Integer> severityCount = getSeverityCount(featureId);

                    // createdAt
                    CodeReview codeReview = codeReviewRepository.findByFeatureItem_FeatureId(featureId)
                            .orElseThrow(null);

                    int commitCounts = codeCommitService.findByFeature_FeatureIdOrderByCommittedAtDesc(featureId).size();

                    return CodeReviewUserAllResponseDto.builder()
                            .projectId(feature.getProjectId())
                            .projectName(feature.getProject().getTitle())
                            .featureId(featureId)
                            .commitCounts(commitCounts)
                            .createdAt(codeReview != null ? codeReview.getCreatedAt() : null)
                            .featureName(feature.getTitle())
                            .featureField(feature.getField())
                            .qualityScore(qualityScore != null ? qualityScore : 0)
                            .severityCount(severityCount)
                            .build();
                })
                .collect(java.util.stream.Collectors.toList());
    }

    /** 리뷰가 없으면 null 점수로 처리 */
    private Double getQualityScoreSafe(Long featureId) {
        try {
            CodeReviewScoreResponseDto dto = getQualityScore(featureId);
            return (dto != null) ? dto.getQualityScore() : null;
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}


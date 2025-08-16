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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
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
    private final com.github.benmanes.caffeine.cache.Cache<String, Boolean> idempotencyCache;



    @Override
    @Transactional
    public void saveCodeReviewResult(CodeReviewResultRequest request) {
        Map<String, String> summary = request.getReviewSummaries();

        // 이미 생성된 코드리뷰가 있다면, 삭제 후 저장
        Long projectId = request.getProjectId();
        String featureName = request.getFeatureName();
        Long featureId = featureItemService.findByProjectIdAndTitle(projectId, featureName).getFeatureId();

        if(codeReviewRepository.existsByFeatureItem_FeatureId(featureId)) {
            CodeReview codeReview = codeReviewRepository.findByFeatureItem_FeatureId(featureId)
                            .orElseThrow(() -> new IllegalArgumentException(featureId + "의 코드리뷰를 찾을 수 없습니다."));
            codeReviewRepository.delete(codeReview);
        }

        CodeReview review = CodeReview.builder()
                .featureItem(featureItemRepository.getReferenceById(request.getFeatureId()))
                .project(projectRepository.getReferenceById(request.getProjectId()))
                .qualityScore(Double.parseDouble(summary.getOrDefault("점수", "0")))
                .summary(summary.getOrDefault("요약", ""))
                .convention(summary.getOrDefault("코딩 컨벤션", ""))
                .bugRisk(summary.getOrDefault("버그 가능성", ""))
                .performance(summary.getOrDefault("성능 최적화", ""))
                .securityRisk(summary.getOrDefault("보안 위험", ""))
                .complexity(summary.getOrDefault("복잡도", ""))
                .refactorSuggestion(summary.getOrDefault("리팩토링 제안", ""))
                .build();

        codeReviewRepository.save(review);
        FeatureItem featureItem = featureItemService.findById(request.getFeatureId());
        featureItem.setStatus("DONE");

        List<FeatureItem> childFeatures = featureItemRepository.findByParentFeature(featureItem.getParentFeature());

        boolean allDone = childFeatures.stream()
                .allMatch(feature -> "DONE".equals(feature.getStatus()));

        if (allDone) {
            featureItem.getParentFeature().setStatus("DONE");
        }

        // 코드리뷰 주입
        List<CodeReviewItem> codeReviewItems = codeReviewItemRepository.findByFeatureItem_FeatureId(featureId);
        for(CodeReviewItem item : codeReviewItems) {
            item.addCodeReview(review);
        }

    }

    // 체크리스트 항목 코드리뷰 저장
    @Override
    @Transactional
    public void saveChecklistReviewItems(CodeReviewResultRequest request) {
        // 0) 필수값 가드
        Long featureId = request.getFeatureId();
        if (featureId == null || request.getFeatureName() == null || request.getFeatureName().isEmpty()) {
            log.error("[CodeReviewItem][SKIP] feature 식별 불가: featureId={}, featureName={}", featureId, request.getFeatureName());
            return;
        }

        List<CodeReviewItemDto> reviews = Optional.ofNullable(request.getCodeReviewItems()).orElse(List.of());
        if (reviews.isEmpty()) {
            log.warn("[CodeReviewItem][SKIP] codeReviewItems 비어있음: featureId={}", featureId);
            return;
        }

        // 1) 연관 엔티티 존재 검증 (getReferenceById 대신 findById로 즉시 검증)
        FeatureItem featureItem = featureItemRepository.findById(featureId)
                .orElseThrow(() -> new IllegalArgumentException("featureItem not found: " + featureId));

        List<CodeReviewItem> toSave = new ArrayList<>();
        int groupCount = 0;
        int itemCount  = 0;

        try {
            for (CodeReviewItemDto review : reviews) {
                groupCount++;
                String checklistItem = review.getChecklistItem();
                List<ReviewItemDto> items = Optional.ofNullable(review.getItems()).orElse(List.of());
                if (items.isEmpty()) {
                    log.debug("[CodeReviewItem][SKIP-GROUP] items empty: checklistItem={}", checklistItem);
                    continue;
                }

                // 2) 체크리스트 항목 엔티티 조회 (없으면 스킵 or 예외)
                var fic = featureItemChecklistService
                        .findByFeatureItem_FeatureIdAndItem(featureId, checklistItem);
                if (fic == null) {
                    log.warn("[CodeReviewItem][SKIP-GROUP] checklist 항목 없음: featureId={}, item={}", featureId, checklistItem);
                    continue; // 필요하면 여기서 throw 로 바꿔도 됨
                }

                for (ReviewItemDto item : items) {
                    itemCount++;
                    CodeReviewItem entity = CodeReviewItem.builder()
                            .featureItem(featureItem)
                            .featureItemChecklist(fic)
                            .category(review.getCategory())
                            .filePath(item.getFilePath())
                            .lineRange(item.getLineRange())
                            .severity(item.getSeverity())
                            .message(item.getMessage())
                            .build();
                    toSave.add(entity);
                }
            }

            if (toSave.isEmpty()) {
                log.warn("[CodeReviewItem][SKIP] 저장할 항목 없음: groups={}, items={}", groupCount, itemCount);
                return;
            }

            codeReviewItemRepository.saveAll(toSave);
            codeReviewItemRepository.flush();  // ← 예외를 지금 즉시 터뜨려서 롤백 원인 확인

            log.info("[CodeReviewItem][OK] saved: groups={}, itemsSaved={}", groupCount, toSave.size());

        } catch (Exception e) {
            log.error("[CodeReviewItem][FAIL] featureId={}, groups={}, builtItems={}, cause={}",
                    featureId, groupCount, toSave.size(), e.toString(), e);
            throw e; // 트랜잭션 롤백되도록 재던짐
        }
    }



    @Override
    public void saveFeatureName(Long projectId, String featureName, Long commitId) {
        CodeCommit commit = codeCommitService.findById(commitId);
        log.info(commitId + " 에 저장 중");
            if(!commit.getFeatureNames().contains(featureName)) {
                log.info(featureName + " 에 해당하는 기능명 아직 없음");
                commit.addFeatureName(featureName);

                FeatureItem featureItem = featureItemService.findByProjectIdAndTitle(projectId, featureName);
                commit.addFeatureItem(featureItem);
                log.info(commit.getFeatureItem().getFeatureId() + " db 에 기능명 저장 완료");
            }
    }



    @Override
    @Transactional
    public void updateChecklistEvaluation(Long projectId, Map<String, Boolean> checklistEvaluation, List<String> extraImplemented, String featureName) {
        FeatureItem featureItem = featureItemService.findByProjectIdAndTitle(projectId, featureName);
        Long featureId = featureItem.getFeatureId();

        for(Map.Entry<String, Boolean> entry : checklistEvaluation.entrySet()) {
            String item = entry.getKey();
            boolean isDone = entry.getValue();
            FeatureItemChecklist featureItemChecklist = featureItemChecklistService.findByFeatureItem_FeatureIdAndItem(featureId, item);
            featureItemChecklist.updateDone(isDone);
        }

        // 추가 구현 항목 처리
        for(String extra : extraImplemented) {
            boolean exists = featureItemChecklistService.existsByFeatureItem_FeatureIdAndItem(featureId, extra);
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
                .flatMap(project -> featureItemRepository.findByProject_ProjectId(project.getProjectId()).stream())
                .flatMap(feature ->
                        codeReviewRepository.findByFeatureItem_FeatureId(feature.getFeatureId()).stream()
                .map(codeReview -> {
                    Long featureId = feature.getFeatureId();

                    // 점수 (리뷰 없을 수도 있으니 NPE/예외 방지)
                    Double qualityScore = getQualityScoreSafe(featureId);

                    // severity 합산(카테고리 무시, severity만 집계)
                    Map<String, Integer> severityCount = getSeverityCount(featureId);

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
        )
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public List<CodeReviewItemDto> getCodeReviewItemsAll(Long projectId, String featureName) {
        // 1) FeatureItem 찾기
        FeatureItem featureItem = featureItemRepository
                .findByProject_ProjectIdAndTitle(projectId, featureName)
                .orElseThrow(() -> new IllegalArgumentException("해당 기능을 찾을 수 없습니다."));

        Long featureId = featureItem.getFeatureId();

        // 2) CodeReviewItem 조회
        List<CodeReviewItem> entities = codeReviewItemRepository.findByFeatureItem_FeatureId(featureId);

        if (entities.isEmpty()) {
            return Collections.emptyList(); // 반드시 빈 배열
        }

        // 3) DTO 변환 (category + checklist_item 기준 그룹핑)
        Map<String, CodeReviewItemDto> grouped = new LinkedHashMap<>();

        for (CodeReviewItem entity : entities) {
            // 널 대비
            String category = entity.getCategory() != null ? entity.getCategory() : "기타";
            FeatureItemChecklist cic = entity.getFeatureItemChecklist();
            if (cic == null) {
                // 체크리스트 연결이 없으면 스킵(또는 별도 "미지정" 버킷으로 모아도 됨)
                continue;
            }
            String item = cic.getItem() != null ? cic.getItem() : "(미지정)";

            String key = category + "::" + item;

            CodeReviewItemDto bucket = grouped.computeIfAbsent(
                    key,
                    k -> CodeReviewItemDto.builder()
                            .category(category)
                            .checklistItem(item)
                            .items(new ArrayList<>())
                            .build()
            );

            bucket.getItems().add(
                    ReviewItemDto.builder()
                            .filePath(entity.getFilePath() != null ? entity.getFilePath() : "")
                            .lineRange(entity.getLineRange() != null ? entity.getLineRange() : "")
                            .severity(entity.getSeverity() != null ? entity.getSeverity() : "INFO")
                            .message(entity.getMessage() != null ? entity.getMessage() : "")
                            .build()
            );
        }

        return new ArrayList<>(grouped.values());
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


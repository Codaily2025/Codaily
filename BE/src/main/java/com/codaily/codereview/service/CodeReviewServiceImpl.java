package com.codaily.codereview.service;

import com.codaily.codereview.dto.*;
import com.codaily.codereview.entity.CodeCommit;
import com.codaily.codereview.entity.CodeReview;
import com.codaily.codereview.entity.CodeReviewItem;
import com.codaily.codereview.entity.FeatureItemChecklist;
import com.codaily.codereview.repository.CodeReviewItemRepository;
import com.codaily.codereview.repository.CodeReviewRepository;
import com.codaily.codereview.repository.FeatureItemChecklistRepository;
import com.codaily.project.entity.FeatureItem;
import com.codaily.project.repository.FeatureItemRepository;
import com.codaily.project.repository.ProjectRepository;
import com.codaily.project.service.FeatureItemService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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

}


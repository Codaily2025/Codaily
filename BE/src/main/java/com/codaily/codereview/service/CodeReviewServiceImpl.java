package com.codaily.codereview.service;

import com.codaily.codereview.dto.*;
import com.codaily.codereview.entity.CodeCommit;
import com.codaily.codereview.entity.CodeReview;
import com.codaily.codereview.entity.CodeReviewItem;
import com.codaily.codereview.entity.FeatureItemChecklist;
import com.codaily.codereview.repository.CodeReviewItemRepository;
import com.codaily.codereview.repository.CodeReviewRepository;
import com.codaily.codereview.repository.FeatureItemChecklistRepository;
import com.codaily.common.git.service.WebhookService;
import com.codaily.project.entity.FeatureItem;
import com.codaily.project.entity.Project;
import com.codaily.project.repository.FeatureItemRepository;
import com.codaily.project.repository.ProjectRepository;
import com.codaily.project.service.FeatureItemService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CodeReviewServiceImpl implements CodeReviewService {

    private final FeatureItemRepository featureItemRepository;
    private final FeatureItemChecklistRepository featureItemChecklistRepository;
    private final CodeReviewItemRepository codeReviewItemRepository;
    private final ProjectRepository projectRepository;
    private final CodeReviewRepository codeReviewRepository;
    private final WebhookService webhookService;
    private final FeatureItemChecklistService featureItemChecklistService;
    private final FeatureItemService featureItemService;
    private final CodeCommitService codeCommitService;

//    @Override
//    public void runCodeReviewAsync(CodeReviewRunRequestDto requestDto) {
//        // ✅ diffFiles 전송
//        webhookService.sendDiffFilesToPython(
//                requestDto.getProjectId(),
//                requestDto.getCommitId(),
//                requestDto.getDiffFiles()
//        );
//    }
//
//    @Override
//    public void handleFeatureInferenceResult(FeatureInferenceResponseDto response) {
//        // 1. 해당 commit, project, featureName 저장 또는 찾기
//        String featureName = response.getFeatureName();
//        Long projectId = response.getProjectId();
//        Long commitId = response.getCommitId();
//        Long userId = response.getUserId();
//
//        // 2. featureId로 FeatureItem 조회
//        FeatureItem featureItem = featureItemRepository.findByProjectIdAndTitle(projectId, featureName)
//                .orElseThrow(() -> new IllegalArgumentException("기능명에 해당하는 FeatureItem 없음"));
//
//        Long featureId = featureItem.getFeatureId();
//
//        // 3. checklist 항목 조회
//        List<FeatureItemChecklist> checklists = featureItemChecklistService.findByFeatureItem_FeatureId(featureItem.getFeatureId());
//        List<ChecklistItemDto> checklistItems = checklists.stream()
//                .map(c -> new ChecklistItemDto(c.getItem(), c.isDone()))
//                .toList();

        // 4. fullFiles 구성
//        List<FullFile> fullFiles = webhookService.getFullFilesFromCommit(response.getCommitHash(), projectId, userId);

        // 5. Python /checklist-evaluation 요청 전송
//        webhookService.sendChecklistEvaluationRequest(
//                projectId,
//                featureId,
//                featureName,
//                fullFiles,
//                checklistItems
//        );


//    @Override
//    public void handleChecklistEvaluationResult(ChecklistEvaluationResponseDto response) {
//        // 1. 결과 저장 (checklist 평가, extra 항목 포함)
//        // extra_implemented 항목 checklist 에 추가
//        featureItemChecklistService.saveChecklistEvaluation(response);
//
//        // 2. 다음 단계로 Python에 리뷰 요청 전송
//        webhookService.sendCodeReviewItemRequest(response);
//    }
//
//    @Override
//    @Transactional
//    public void handleCodeReviewItems(FeatureReviewResultDto dto) {
//        FeatureItem feature = featureItemRepository.findById(dto.getFeatureId())
//                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 기능입니다"));
//
//        for (ChecklistReviewResult result : dto.getCodeReviewItems()) {
//            String checklistItemText = result.getChecklistItem();
//
//            FeatureItemChecklist checklist = featureItemChecklistRepository
//                    .findByFeatureItemAndItem(feature, checklistItemText)
//                    .orElseThrow(() -> new IllegalArgumentException("체크리스트 항목 없음: " + checklistItemText));
//
//            for (Map<String, Object> review : result.getCodeReviews()) {
//                CodeReviewItem item = CodeReviewItem.of(
//                        checklist,
//                        feature,
//                        (String) review.get("category"),
//                        (String) review.get("file_path"),
//                        (String) review.get("line_range"),
//                        (String) review.get("severity"),
//                        (String) review.get("message")
//                );
//
//                codeReviewItemRepository.save(item);
//            }
//        }
//    }
//
//    @Override
//    @Transactional
//    public void saveCodeReviewSummary(FeatureReviewSummaryDto dto) {
//        Project project = projectRepository.findById(dto.getProjectId())
//                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 프로젝트"));
//
//        FeatureItem feature = featureItemRepository.findById(dto.getFeatureId())
//                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 기능"));
//
//        CodeReview codeReview = CodeReview.builder()
//                .project(project)
//                .featureItem(feature)
//                .overallScore(dto.getOverallScore())
//                .summary(dto.getSummary())
//                .convention(dto.getConvention())
//                .refactorSuggestion(dto.getRefactorSuggestion())
//                .complexity(dto.getComplexity())
//                .bugRisk(dto.getBugRisk())
//                .securityRisk(dto.getSecurityRisk())
//                .build();
//
//        codeReviewRepository.save(codeReview);
//    }
//
    @Override
    public void saveCodeReviewResult(CodeReviewResultRequest request) {
        Map<String, String> summary = request.getReviewSummary();

        CodeReview review = CodeReview.builder()
                .featureItem(featureItemRepository.getReferenceById(request.getFeatureId()))
                .project(projectRepository.getReferenceById(request.getProjectId()))
                .overallScore(Double.parseDouble(summary.getOrDefault("점수", "0")))
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

                CodeReviewItem entity = CodeReviewItem.builder()
                        .featureItem(featureItemService.findById(featureId))
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

            if(!exists) {
                FeatureItemChecklist checklist = FeatureItemChecklist.builder()
                        .featureItem(featureItemService.findById(featureId))
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
}


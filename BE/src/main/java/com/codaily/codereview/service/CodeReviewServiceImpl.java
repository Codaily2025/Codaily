package com.codaily.codereview.service;

import com.codaily.codereview.dto.*;
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

    @Override
    public void runCodeReviewAsync(CodeReviewRunRequestDto requestDto) {
        // ✅ diffFiles 전송
        webhookService.sendDiffFilesToPython(
                requestDto.getProjectId(),
                requestDto.getCommitId(),
                requestDto.getDiffFiles()
        );
    }

    @Override
    public void handleFeatureInferenceResult(FeatureInferenceResponseDto response) {
        // 1. 해당 commit, project, featureName 저장 또는 찾기
        String featureName = response.getFeatureName();
        Long projectId = response.getProjectId();
        Long commitId = response.getCommitId();
        Long userId = response.getUserId();

        // 2. featureId로 FeatureItem 조회
        FeatureItem featureItem = featureItemRepository.findByProjectIdAndTitle(projectId, featureName)
                .orElseThrow(() -> new IllegalArgumentException("기능명에 해당하는 FeatureItem 없음"));

        Long featureId = featureItem.getFeatureId();

        // 3. checklist 항목 조회
        List<FeatureItemChecklist> checklists = featureItemChecklistRepository.findByFeatureItem_FeatureId(featureItem.getFeatureId());
        List<ChecklistItemDto> checklistItems = checklists.stream()
                .map(c -> new ChecklistItemDto(c.getItem(), c.isDone()))
                .toList();

        // 4. fullFiles 구성
        List<FullFile> fullFiles = webhookService.getFullFilesFromCommit(response.getCommitHash(), projectId, userId);

        // 5. Python /checklist-evaluation 요청 전송
        webhookService.sendChecklistEvaluationRequest(
                projectId,
                featureId,
                featureName,
                fullFiles,
                checklistItems
        );
    }

    @Override
    public void handleChecklistEvaluationResult(ChecklistEvaluationResponseDto response) {
        // 1. 결과 저장 (checklist 평가, extra 항목 포함)
        // extra_implemented 항목 checklist 에 추가
        featureItemChecklistService.saveChecklistEvaluation(response);

        // 2. 다음 단계로 Python에 리뷰 요청 전송
        webhookService.sendCodeReviewItemRequest(response);
    }

    @Override
    @Transactional
    public void handleCodeReviewItems(FeatureReviewResultDto dto) {
        FeatureItem feature = featureItemRepository.findById(dto.getFeatureId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 기능입니다"));

        for (ChecklistReviewResult result : dto.getCodeReviewItems()) {
            String checklistItemText = result.getChecklistItem();

            FeatureItemChecklist checklist = featureItemChecklistRepository
                    .findByFeatureItemAndItem(feature, checklistItemText)
                    .orElseThrow(() -> new IllegalArgumentException("체크리스트 항목 없음: " + checklistItemText));

            for (Map<String, Object> review : result.getCodeReviews()) {
                CodeReviewItem item = CodeReviewItem.of(
                        checklist,
                        feature,
                        (String) review.get("category"),
                        (String) review.get("file_path"),
                        (String) review.get("line_range"),
                        (String) review.get("severity"),
                        (String) review.get("message")
                );

                codeReviewItemRepository.save(item);
            }
        }
    }

    @Override
    @Transactional
    public void saveCodeReviewSummary(FeatureReviewSummaryDto dto) {
        Project project = projectRepository.findById(dto.getProjectId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 프로젝트"));

        FeatureItem feature = featureItemRepository.findById(dto.getFeatureId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 기능"));

        CodeReview codeReview = CodeReview.builder()
                .project(project)
                .featureItem(feature)
                .overallScore(dto.getOverallScore())
                .summary(dto.getSummary())
                .convention(dto.getConvention())
                .refactorSuggestion(dto.getRefactorSuggestion())
                .complexity(dto.getComplexity())
                .bugRisk(dto.getBugRisk())
                .securityRisk(dto.getSecurityRisk())
                .build();

        codeReviewRepository.save(codeReview);
    }
}


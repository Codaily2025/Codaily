package com.codaily.codereview.controller;

import com.codaily.auth.service.UserService;
import com.codaily.codereview.dto.*;
import com.codaily.codereview.entity.FeatureItemChecklist;
import com.codaily.codereview.repository.FeatureItemChecklistRepository;
import com.codaily.codereview.service.CodeReviewService;
import com.codaily.codereview.service.FeatureItemChecklistService;
import com.codaily.common.git.service.WebhookService;
import com.codaily.project.entity.FeatureItem;
import com.codaily.project.entity.Project;
import com.codaily.project.repository.FeatureItemRepository;
import com.codaily.project.service.FeatureItemService;
import com.codaily.project.service.ProjectService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/code-review")
@RequiredArgsConstructor
@Slf4j
public class CodeReviewController {

    private final CodeReviewService codeReviewService;
    private final FeatureItemService featureItemService;
    private final FeatureItemChecklistService featureItemChecklistService;
    private final WebhookService webhookService;
    private final ProjectService projectService;

    // 체크리스트 전달
    @GetMapping("/project/{projectId}/feature/{featureName}/checklist")
    public List<ChecklistItemDto> getChecklistItems(@PathVariable Long projectId, @PathVariable String featureName) {
        FeatureItem featureItem = featureItemService.findByProjectIdAndTitle(projectId, featureName);

        List<FeatureItemChecklist> list = featureItemChecklistService.findByFeatureItem_FeatureId(featureItem.getFeatureId());

        return list.stream()
                .map(item -> new ChecklistItemDto(item.getItem(), item.isDone()))
                .collect(Collectors.toList());
    }

    @PostMapping("/project/{projectId}/commit/{commitHash}/files")
    public List<FullFileDto> getFullFilesByPaths(@PathVariable Long projectId, @PathVariable String commitHash,
                                                 @RequestBody FileFetchRequestDto fileFetchRequestDto){
        List<String> paths = fileFetchRequestDto.getFilePaths();
        Project project = projectService.findById(projectId);
        Long userId = project.getUser().getUserId();

        List<FullFile> fullFiles = webhookService.getFullFilesByPaths(commitHash, projectId, userId, paths);

        return fullFiles.stream()
                .map(file -> new FullFileDto(file.getFilePath(), file.getContent()))
                .collect(Collectors.toList());
    }

    @PostMapping("/code-review/result")
    public ResponseEntity<?> receiveCodeReviewResult(@RequestBody CodeReviewResultRequest request) {
        if (request.getReviewSummary() != null) {
            // ✅ 기능 전체가 구현 완료 → 전체 저장
            codeReviewService.saveCodeReviewResult(request);
        } else {
            // ✅ 기능 일부 구현 → checklist별 리뷰만 임시 저장
            codeReviewService.saveChecklistReviewItems(request);
        }
        return ResponseEntity.ok().build();
    }



//
//    // 기능명 추론 결과 응답 수신
//    @PostMapping("/feature-inference/result")
//    public ResponseEntity<Void> receiveFeatureInferenceResult(
//            @RequestBody FeatureInferenceResponseDto response
//    ) {
//        // ❗ 기능 없음이면 파이프라인 종료
//        if ("기능 없음".equals(response.getFeatureName())) {
//            log.info("❌ 기능 없음 - 파이프라인 중단");
//            return ResponseEntity.ok().build();
//        }
//
//        // ✅ 기능 있음이면 이후 checklist 평가 요청 수행
//        codeReviewService.handleFeatureInferenceResult(response);
//        return ResponseEntity.ok().build();
//    }
//
//    // 기능 구현 결과 응답
//    @PostMapping("/checklist-evaluation/result")
//    public ResponseEntity<Void> receiveChecklistEvaluationResult(
//            @RequestBody ChecklistEvaluationResponseDto response
//    ) {
//        codeReviewService.handleChecklistEvaluationResult(response);
//        return ResponseEntity.ok().build();
//    }
//
//    // 체크리스트별 코드리뷰 응답
//    @PostMapping("/code-review/items")
//    public ResponseEntity<Void> receiveCodeReviewItems(
//            @RequestBody FeatureReviewResultDto dto
//    ) {
//        codeReviewService.handleCodeReviewItems(dto);
//        return ResponseEntity.ok().build();
//    }
//
//    // 기능 코드리뷰 요약 응답
//    @PostMapping("/code-review/summary")
//    public ResponseEntity<Void> receiveReviewSummary(@RequestBody FeatureReviewSummaryDto dto) {
//        codeReviewService.saveCodeReviewSummary(dto);
//        return ResponseEntity.ok().build();
//    }
}




package com.codaily.codereview.controller;

import com.codaily.auth.config.PrincipalDetails;
import com.codaily.codereview.dto.*;
import com.codaily.codereview.entity.FeatureItemChecklist;
import com.codaily.codereview.service.CodeReviewService;
import com.codaily.codereview.service.FeatureItemChecklistService;
import com.codaily.common.git.service.WebhookService;
import com.codaily.project.entity.FeatureItem;
import com.codaily.project.entity.Project;
import com.codaily.project.service.FeatureItemService;
import com.codaily.project.service.ProjectService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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
                                                 @RequestBody FileFetchRequestDto fileFetchRequestDto,
                                                 @AuthenticationPrincipal PrincipalDetails userDetails){
        List<String> paths = fileFetchRequestDto.getFilePaths();
        Project project = projectService.findById(projectId);
        Long userId = project.getUser().getUserId();
        Long loginUserId = userDetails.getUser().getUserId();

        if(userId != loginUserId) {
            log.info("해당 프로젝트에 접근할 권한이 없습니다.");
        }
        List<FullFile> fullFiles = webhookService.getFullFilesByPaths(commitHash, projectId, userId, paths);

        return fullFiles.stream()
                .map(file -> new FullFileDto(file.getFilePath(), file.getContent()))
                .collect(Collectors.toList());
    }

    @PostMapping("/code-review/result")
    public ResponseEntity<?> receiveCodeReviewResult(@RequestBody CodeReviewResultRequest request) {
        if(request.getFeatureNames() != null && !request.getFeatureNames().isEmpty()) {
            codeReviewService.saveFeatureName(request.getProjectId(), request.getFeatureNames(), request.getCommitId());
            log.info("기능명 저장 완료");
        }
        // 체크리스트 구현 여부 변경 or 커밋 메시지 구현 완료 or 사용자가 작업완료 버튼 클릭
        if((request.getChecklistFileMap() != null && !request.getChecklistFileMap().isEmpty()) || request.isForceDone()) {
            codeReviewService.updateChecklistEvaluation(request.getFeatureId(), request.getChecklistEvaluation(), request.getExtraImplemented());
            log.info("체크리스트 구현 여부 업데이트");
        }
        if (request.getReviewSummary() != null) {
            codeReviewService.saveCodeReviewResult(request);
            log.info("기능 구현 완료 -> 코드 리뷰 생성");
        } else {
            codeReviewService.saveChecklistReviewItems(request);
            log.info("기능 미구현, 체크리스트 코드 리뷰 생성");
        }
        return ResponseEntity.ok().build();
    }

}




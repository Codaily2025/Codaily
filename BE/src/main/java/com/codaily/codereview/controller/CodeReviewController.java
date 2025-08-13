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
import com.codaily.project.service.ProductivityEventService;
import com.codaily.project.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
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
    private final CodeReviewResponseMapper codeReviewResponseMapper;

    @Autowired
    private ProductivityEventService productivityEventService;

    // 체크리스트 전달
    @GetMapping("/project/{projectId}/feature/{featureName}/checklist")
    @Operation(summary = "백엔드용")
    public List<ChecklistItemDto> getChecklistItems(@PathVariable Long projectId, @PathVariable String featureName) {
        FeatureItem featureItem = featureItemService.findByProjectIdAndTitle(projectId, featureName);

        List<FeatureItemChecklist> list = featureItemChecklistService.findByFeatureItem_FeatureId(featureItem.getFeatureId());

        return list.stream()
                .map(item -> new ChecklistItemDto(item.getItem(), item.isDone()))
                .collect(Collectors.toList());
    }

    @PostMapping("/project/{projectId}/commit/{commitHash}/files")
    @Operation(summary = "백엔드용")
    public List<FullFileDto> getFullFilesByPaths(@PathVariable Long projectId, @PathVariable String commitHash,
                                                 @RequestBody FileFetchRequestDto fileFetchRequestDto,
                                                 @AuthenticationPrincipal PrincipalDetails userDetails){
        List<String> paths = fileFetchRequestDto.getFilePaths();
        String repoName = fileFetchRequestDto.getRepoName();
        String repoOwner = fileFetchRequestDto.getRepoOwner();

        Project project = projectService.findById(projectId);
        Long userId = project.getUser().getUserId();
        Long loginUserId = userDetails.getUser().getUserId();

        if(userId != loginUserId) {
            log.info("해당 프로젝트에 접근할 권한이 없습니다.");
        }
        List<FullFile> fullFiles = webhookService.getFullFilesByPaths(commitHash, projectId, userId, paths, repoOwner, repoName);

        return fullFiles.stream()
                .map(file -> new FullFileDto(file.getFilePath(), file.getContent()))
                .collect(Collectors.toList());
    }

    @PostMapping("/code-review/result")
    @Operation(summary = "백엔드용")
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
            //생산성 즉시 업데이트
            Project project = projectService.findById(request.getProjectId());
            productivityEventService.updateProductivityOnReview(
                    request.getProjectId(),
                    project.getUser().getUserId(),
                    LocalDate.now()
            );
            log.info("기능 구현 완료 -> 코드 리뷰 생성");
        } else {
            codeReviewService.saveChecklistReviewItems(request);
            log.info("기능 미구현, 체크리스트 코드 리뷰 생성");
        }
        return ResponseEntity.ok().build();


    }


    @GetMapping("/{featureId}/detail")
    @Operation(summary = "코드 리뷰 전체보기", description = "featureId 기준 코드 리뷰 전체 반환")
    public ResponseEntity<Map<String, Object>> getCodeReviewDetail(@PathVariable Long featureId) {

        // 1) 기존 서비스 호출
        CodeReviewSummaryResponseDto summaryDto = codeReviewService.getCodeReviewSummary(featureId);
        List<CodeReviewItemResponseDto> items = codeReviewService.getCodeReviewItems(featureId);

        // 2) 중첩 구조 변환
        Map<String, Object> detailResponse = codeReviewResponseMapper.toDetailResponse(
                featureId,
                summaryDto,  // 요약 DTO
                items        // 항목 DTO
        );

        return ResponseEntity.ok(detailResponse);
    }


    // 기능 코드리뷰 요약
    @GetMapping("/{featureId}/summary")
    @Operation(summary = "코드 리뷰 한줄 요약", description = "featureId 기준 각 코드리뷰 항목(카테고리)에 대한 한줄 요약")
    public ResponseEntity<CodeReviewSummaryResponseDto> getCodeReviewSummary(
            @PathVariable Long featureId
    ) {
        return ResponseEntity.ok(codeReviewService.getCodeReviewSummary(featureId));
    }

    /*
    {
          "summary": "이 기능은 보안과 리팩토링 측면에서 개선 여지가 있음",
          "convention": "변수명 컨벤션 미준수 있음",
          "refactorSuggestion": "중복 로직 분리 필요",
          "complexity": "복잡한 if-else 구조 있음",
          "bugRisk": "예외 누락 가능성 있음",
          "securityRisk": "하드코딩된 키 존재",
          "qualityScore": 86.0
}
     */

    @GetMapping("/{featureId}/items")
    @Operation(summary = "코드 리뷰 항목별 조회", description = "featureId 기준 코드리뷰 항목(카테고리)별 조회용")
    public ResponseEntity<List<CodeReviewItemResponseDto>> getCodeReviewItems(@PathVariable Long featureId) {
        return ResponseEntity.ok(codeReviewService.getCodeReviewItems(featureId));
    }

    /*
                [
          {
            "category": "보안",
            "filePath": "auth/jwt/JwtService.java",
            "lineRange": "30-45",
            "severity": "중간",
            "message": "JWT 서명 키가 하드코딩되어 있어 보안에 취약함"
          },
          {
            "category": "리팩토링",
            "filePath": "global/ExceptionHandler.java",
            "lineRange": "50-52",
            "severity": "낮음",
            "message": "Exception 메시지에 사용자 친화적인 안내가 부족함"
          },
          {
            "category": "버그",
            "filePath": "controller/LoginController.java",
            "lineRange": "85",
            "severity": "중간",
            "message": "실패 시 클라이언트에 구체적인 오류 메시지가 전달되지 않음"
          }
        ]
     */

    @GetMapping("{featureId}/checklist/status")
    @Operation(summary = "체크리스트 구현 상태", description = "featureId 기준 체크리스트 구현 여부 확인용")
    public ResponseEntity<ChecklistStatusResponseDto> getChecklistStatus(@PathVariable Long featureId) {
        return ResponseEntity.ok(codeReviewService.getChecklistStatus(featureId));
    }

    /*

                    {
          "allImplemented": false,
          "items": [
            {
              "item": "JWT 발급",
              "done": true
            },
            {
              "item": "예외 처리",
              "done": true
            },
            {
              "item": "로그인 실패 메시지 반환",
              "done": false
            }
          ]
        }

     */

    @GetMapping("{featureId}/score")
    @Operation(summary = "코드 리뷰 점수", description = "featureId 기준 코드 리뷰 점수 반환")
    public ResponseEntity<CodeReviewScoreResponseDto> getCodeReviewScore(@PathVariable Long featureId) {
        return ResponseEntity.ok(codeReviewService.getQualityScore(featureId));
    }

    /*
        {
             "qualityScore": 86.0
        }
     */

    @GetMapping("{featureId}/severity-by-category")
    @Operation(summary = "중요도 개수", description = "featureId 기준 중요도별 개수 반환")
    public ResponseEntity<SeverityByCategoryResponseDto> getSeverityByCategory(@PathVariable Long featureId) {
        return ResponseEntity.ok(codeReviewService.getSeverityByCategory(featureId));
    }
    /*

        {
          "보안": {
            "높음": 1,
            "중간": 2,
            "낮음": 0
          },
          "리팩토링": {
            "높음": 0,
            "중간": 1,
            "낮음": 2
          },
          "버그": {
            "높음": 2,
            "중간": 1,
            "낮음": 0
          }
        }

     */

    @GetMapping("/project/{projectId}")
    @Operation(summary = "프로젝트 코드리뷰 전체 조회", description = "projectId 기준 기능별 전체 코드리뷰 조회 가능합니다")
    public ResponseEntity<List<CodeReviewAllResponseDto>> getAllCodeReviews(@PathVariable Long projectId) {
        return ResponseEntity.ok(codeReviewService.getCodeReviewsAllSummary(projectId));
    }

    @GetMapping("/user")
    @Operation(summary = "사용자의 모든 프로젝트 코드리뷰 전체 조회", description = "로그인한 사용자 기준 전체 코드리뷰 조회 가능합니다")
    public ResponseEntity<List<CodeReviewUserAllResponseDto>> getUserAllCodeReviews(@AuthenticationPrincipal PrincipalDetails userDetails) {
        return ResponseEntity.ok(codeReviewService.getUserAllCodeReviews(userDetails.getUserId()));
    }

//    @GetMapping("/user/{userId}")
//    @Operation(summary = "사용자의 모든 프로젝트 코드리뷰 조회", description = "userId 기준 기능별 전체 코드리뷰 조회 가능합니다 \n 현재는 로그인한 유저의 프로젝트를 조회할 수 없어 userId 값을 넣어주어야 합니다")
//    public ResponseEntity<List<CodeReviewUserAllResponseDto>> getUserAllCodeReviews(@PathVariable Long userId) {
//        return ResponseEntity.ok(codeReviewService.getUserAllCodeReviews(userId));
//    }
}




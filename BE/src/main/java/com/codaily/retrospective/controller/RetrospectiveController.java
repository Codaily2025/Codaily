package com.codaily.retrospective.controller;

import com.codaily.project.entity.Project;
import com.codaily.project.repository.ProjectRepository;
import com.codaily.retrospective.dto.RetrospectiveGenerateResponse;
import com.codaily.retrospective.dto.RetrospectiveListResponse;
import com.codaily.retrospective.service.RetrospectiveGenerateService;
import com.codaily.retrospective.service.RetrospectiveService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.concurrent.CompletableFuture;

@Log4j2
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/projects/{projectId}/retrospectives")
@Tag(name = "Retrospectives", description = "프로젝트 일일 회고 생성/조회 API")
public class RetrospectiveController {

    private final ProjectRepository projectRepository;
    private final RetrospectiveGenerateService retrospectiveGenerateService;
    private final RetrospectiveService retrospectiveService;

    @Operation(
            summary = "오늘 회고 수동 생성",
            description = "지정된 프로젝트에 대해 **오늘 날짜**의 회고 생성을 비동기적으로 시작하고, 생성된 회고를 반환합니다."
    )
    @ApiResponse(
            responseCode = "200",
            description = "생성 성공",
            content = @Content(
                    schema = @Schema(implementation = RetrospectiveGenerateResponse.class),
                    examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                            name = "성공 응답 예시",
                            value = """
                                    {
                                      "date": "2025-08-09",
                                      "projectId": 1,
                                      "userId": 10,
                                      "triggerType": "MANUAL",
                                      "contentMarkdown": "## 오늘의 회고\\n- 주요 작업 완료...",
                                      "summary": {
                                        "overall": "오늘은 주요 기능을 마무리했다.",
                                        "strengths": "코드 품질 향상"
                                      },
                                      "actionItems": [
                                        {"title": "리팩토링", "description": "중복 코드 제거"}
                                      ],
                                      "productivityMetrics": {
                                        "productivityScore": 85,
                                        "codeQualityScore": 90
                                      },
                                      "completedFeatures": [],
                                      "reviewIssuesTop": []
                                    }
                                    """
                    )
            )
    )
    @PostMapping
    public CompletableFuture<ResponseEntity<RetrospectiveGenerateResponse>> generateManualRetrospective(
            @Parameter(description = "프로젝트 ID", example = "1")
            @PathVariable Long projectId) {

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 프로젝트입니다."));

        return retrospectiveGenerateService.generateProjectDailyRetrospective(project)
                .thenApply(ResponseEntity::ok);
    }

    @Operation(
            summary = "프로젝트 회고 전체 조회",
            description = "해당 프로젝트의 **모든 일일 회고를 최신순**으로 반환합니다."
    )
    @ApiResponse(
            responseCode = "200",
            description = "조회 성공",
            content = @Content(
                    schema = @Schema(implementation = RetrospectiveListResponse.class),
                    examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                            name = "성공 응답 예시",
                            value = """
                                    {
                                      "retrospectives": [
                                        {
                                          "date": "2025-08-09",
                                          "projectId": 1,
                                          "userId": 10,
                                          "triggerType": "AUTO",
                                          "contentMarkdown": "## 자동 생성 회고...",
                                          "summary": {
                                            "overall": "자동 회고 생성 완료",
                                            "strengths": "효율적인 일정 관리"
                                          },
                                          "actionItems": [],
                                          "productivityMetrics": {
                                            "productivityScore": 80,
                                            "codeQualityScore": 88
                                          },
                                          "completedFeatures": [],
                                          "reviewIssuesTop": []
                                        }
                                      ]
                                    }
                                    """
                    )
            )
    )
    @GetMapping
    public ResponseEntity<RetrospectiveListResponse> getAll(
            @Parameter(description = "프로젝트 ID", example = "1")
            @PathVariable Long projectId
    ) {
        RetrospectiveListResponse response = retrospectiveService.getAllDailyRetrospectives(projectId);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "특정 날짜 회고 조회",
            description = "해당 프로젝트의 **특정 날짜(`yyyy-MM-dd`)**에 생성된 회고를 반환합니다."
    )
    @ApiResponse(
            responseCode = "200",
            description = "조회 성공",
            content = @Content(
                    schema = @Schema(implementation = RetrospectiveGenerateResponse.class),
                    examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                            name = "성공 응답 예시",
                            value = """
                                    {
                                      "date": "2025-08-08",
                                      "projectId": 1,
                                      "userId": 11,
                                      "triggerType": "MANUAL",
                                      "contentMarkdown": "## 특정 날짜 회고...",
                                      "summary": {
                                        "overall": "주요 이슈 해결 완료",
                                        "strengths": "협업 효율 상승"
                                      },
                                      "actionItems": [
                                        {"title": "테스트 보강", "description": "단위 테스트 커버리지 90% 이상"}
                                      ],
                                      "productivityMetrics": {
                                        "productivityScore": 82,
                                        "codeQualityScore": 91
                                      },
                                      "completedFeatures": [],
                                      "reviewIssuesTop": []
                                    }
                                    """
                    )
            )
    )
    @GetMapping("/{date}")
    public ResponseEntity<RetrospectiveGenerateResponse> getOneByDate(
            @Parameter(description = "프로젝트 ID", example = "1")
            @PathVariable Long projectId,
            @Parameter(description = "조회할 날짜 (ISO 8601)", example = "2025-08-09")
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        RetrospectiveGenerateResponse response = retrospectiveService.getDailyRetrospective(projectId, date);
        return ResponseEntity.ok(response);
    }
}

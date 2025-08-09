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
import io.swagger.v3.oas.annotations.media.ExampleObject;
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
                    examples = @ExampleObject(
                            name = "성공 응답 예시",
                            value = """
                                    {
                                      "date": "2025-08-10",
                                      "projectId": 3,
                                      "userId": 3,
                                      "triggerType": "AUTO",
                                      "contentMarkdown": "## 요약\\n오늘은 두 주요 기능이 성공적으로 완료되었으며, 전체 프로젝트의 생산성과 품질이 높은 수준을 유지하였습니다. 상품 목록 조회와 주문 결제 기능 모두 안정적인 성과를 보였고, 코드 품질 및 생산성 점수도 양호합니다.\\n\\n## 진행\\n- 상품 목록 조회 기능: 성능 양호, 향후 개선 여지 확보\\n- 주문 결제 기능: 결제 흐름 안정적이며 무리 없이 완료\\n총 커밋 수는 5회로, 꾸준한 개발 활동이 이어지고 있습니다.\\n\\n## 이슈\\n- 기능별 체크리스트 일부 미완료(상품 관리 체크리스트 1/2, 결제 체크리스트 2/2)\\n- 현재 특별한 심각한 이슈 없음\\n\\n## 개선/액션\\n1. 상품 관리 체크리스트의 미완료 항목을 우선순위로 완료하기\\n2. 코드 품질 향상을 위해 정기적인 코드 리뷰 실시\\n3. 향후 배포 전 최종 기능 점검을 강화하여 품질 유지\\n4. 생산성 향상을 위한 자동화 도구 도입 검토\\n5. 오늘의 작업 기록을 문서화하여 후속 검증 용이성 확보",
                                      "summary": {
                                        "overall": "오늘의 진행상황은 전반적으로 안정적이며, 주요 기능이 성공적으로 완료되었음. 품질과 생산성 모두 양호한 수준이며 추가 개선 여지가 존재함.",
                                        "strengths": "기능 안정성, 높은 품질지표, 꾸준한 커밋과 효율적 프로그래밍 활동",
                                        "improvements": "체크리스트 미완료 항목 해결, 코드 리뷰 활성화, 배포 전 최종 점검 강화, 자동화 도구 도입 검토 필요",
                                        "risks": "체크리스트 미완료로 인한 품질 저하 가능성 및 배포 지연 우려"
                                      },
                                      "actionItems": null,
                                      "productivityMetrics": {
                                        "codeQuality": 88.75,
                                        "productivityScore": 90.5,
                                        "completedFeatures": 2,
                                        "totalCommits": 5
                                      },
                                      "completedFeatures": [
                                        {
                                          "featureId": 1001,
                                          "title": "상품 목록 조회 기능",
                                          "field": "상품 관리",
                                          "checklistCount": 2,
                                          "checklistDoneCount": 1,
                                          "codeQualityScore": 85.5,
                                          "summary": "성능 양호, 개선 여지 있음",
                                          "reviewIssues": [
                                            {
                                              "featureTitle": "상품 목록 조회 기능",
                                              "checklistItem": "상품 목록 API 연동",
                                              "category": "PERFORMANCE",
                                              "severity": "MEDIUM",
                                              "message": "N+1 쿼리 가능성",
                                              "filePath": "src/main/java/ProductService.java"
                                            }
                                          ]
                                        },
                                        {
                                          "featureId": 1002,
                                          "title": "주문 결제 기능",
                                          "field": "결제",
                                          "checklistCount": 2,
                                          "checklistDoneCount": 2,
                                          "codeQualityScore": 92.0,
                                          "summary": "결제 흐름 안정적",
                                          "reviewIssues": [
                                            {
                                              "featureTitle": "주문 결제 기능",
                                              "checklistItem": "결제 모듈 연동",
                                              "category": "SECURITY",
                                              "severity": "LOW",
                                              "message": "응답 로그 카드정보 마스킹",
                                              "filePath": "src/main/java/PaymentService.java"
                                            }
                                          ]
                                        }
                                      ]
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
                    examples = @ExampleObject(
                            name = "성공 응답 예시",
                            value = """
                        {
                          "retrospectives": [
                            {
                              "date": "2025-08-10",
                              "projectId": 3,
                              "userId": 3,
                              "triggerType": "AUTO",
                              "contentMarkdown": "## 요약\\n오늘은 두 주요 기능이 성공적으로 완료되었으며...",
                              "summary": {
                                "overall": "오늘의 진행상황은 전반적으로 안정적이며...",
                                "strengths": "기능 안정성, 높은 품질지표...",
                                "improvements": "체크리스트 미완료 항목 해결...",
                                "risks": "체크리스트 미완료로 인한 리스크..."
                              },
                              "actionItems": null,
                              "productivityMetrics": {
                                "codeQuality": 88.75,
                                "productivityScore": 90.5,
                                "completedFeatures": 2,
                                "totalCommits": 5
                              },
                              "completedFeatures": [
                                {
                                  "featureId": 1001,
                                  "title": "상품 목록 조회 기능",
                                  "field": "상품 관리",
                                  "checklistCount": 2,
                                  "checklistDoneCount": 1,
                                  "codeQualityScore": 85.5,
                                  "summary": "성능 양호, 개선 여지 있음",
                                  "reviewIssues": [
                                    {
                                      "featureTitle": "상품 목록 조회 기능",
                                      "checklistItem": "상품 목록 API 연동",
                                      "category": "PERFORMANCE",
                                      "severity": "MEDIUM",
                                      "message": "N+1 쿼리 가능성",
                                      "filePath": "src/main/java/ProductService.java"
                                    }
                                  ]
                                },
                                {
                                  "featureId": 1002,
                                  "title": "주문 결제 기능",
                                  "field": "결제",
                                  "checklistCount": 2,
                                  "checklistDoneCount": 2,
                                  "codeQualityScore": 92.0,
                                  "summary": "결제 흐름 안정적",
                                  "reviewIssues": [
                                    {
                                      "featureTitle": "주문 결제 기능",
                                      "checklistItem": "결제 모듈 연동",
                                      "category": "SECURITY",
                                      "severity": "LOW",
                                      "message": "응답 로그 카드정보 마스킹",
                                      "filePath": "src/main/java/PaymentService.java"
                                    }
                                  ]
                                }
                              ]
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
                    examples = @ExampleObject(
                            name = "성공 응답 예시",
                            value = """
                                    {
                                      "date": "2025-08-09",
                                      "projectId": 3,
                                      "userId": 3,
                                      "triggerType": "MANUAL",
                                      "contentMarkdown": "## 특정 날짜 회고...\\n- 주요 이슈 해결...",
                                      "summary": {
                                        "overall": "핵심 이슈를 해결하고 기능 안정화 진행",
                                        "strengths": "효율적인 협업과 이슈 대응",
                                        "improvements": "테스트 커버리지 상향",
                                        "risks": "결제 외부 연동 지연 가능성"
                                      },
                                      "actionItems": [
                                        {"title": "테스트 보강", "description": "단위/통합 테스트 커버리지 90% 이상"}
                                      ],
                                      "productivityMetrics": {
                                        "codeQuality": 89.0,
                                        "productivityScore": 86.0,
                                        "completedFeatures": 1,
                                        "totalCommits": 4
                                      },
                                      "completedFeatures": [
                                        {
                                          "featureId": 1002,
                                          "title": "주문 결제 기능",
                                          "field": "결제",
                                          "checklistCount": 2,
                                          "checklistDoneCount": 2,
                                          "codeQualityScore": 92.0,
                                          "summary": "결제 흐름 안정적",
                                          "reviewIssues": [
                                            {
                                              "featureTitle": "주문 결제 기능",
                                              "checklistItem": "결제 모듈 연동",
                                              "category": "SECURITY",
                                              "severity": "LOW",
                                              "message": "응답 로그 카드정보 마스킹",
                                              "filePath": "src/main/java/PaymentService.java"
                                            }
                                          ]
                                        }
                                      ]
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

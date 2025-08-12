package com.codaily.retrospective.controller;

import com.codaily.retrospective.dto.RetrospectiveScrollResponse;
import com.codaily.retrospective.service.RetrospectiveService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@Log4j2
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/projects/retrospectives")
@Tag(name = "AllRetrospectives", description = "유저의 전체 프로젝트 일일 회고 생성/조회 API")
public class AllRetrospectiveController {
    private final RetrospectiveService retrospectiveService;

    @Operation(
            summary = "모든 프로젝트 회고 무한 스크롤 조회",
            description = """
                로그인한 사용자의 모든 프로젝트 회고를 최신순으로 무한 스크롤 방식으로 조회합니다.
                `before` 파라미터를 이용해 특정 날짜 이전의 회고를 조회할 수 있습니다.
                """
    )
    @ApiResponse(
            responseCode = "200",
            description = "조회 성공",
            content = @Content(
                    schema = @Schema(implementation = RetrospectiveScrollResponse.class),
                    examples = @ExampleObject(
                            name = "성공 응답 예시",
                            value = """
                                {
                                  "items": [
                                    {
                                      "date": "2025-08-10",
                                      "projectId": 3,
                                      "userId": 3,
                                      "triggerType": "AUTO",
                                      "contentMarkdown": "## 요약\\n오늘은 두 주요 기능이 성공적으로 완료되었으며...",
                                      "summary": {
                                        "overall": "오늘의 진행상황은 전반적으로 안정적...",
                                        "strengths": "기능 안정성, 높은 품질지표, 꾸준한 커밋...",
                                        "improvements": "체크리스트 미완료 항목 해결, 코드 리뷰 활성화...",
                                        "risks": "체크리스트 미완료로 인한 품질 저하 가능성..."
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
                                        }
                                      ]
                                    }
                                  ],
                                  "hasNext": true,
                                  "nextBefore": "2025-08-07"
                                }
                                """
                    )
            )
    )
    @GetMapping
    public ResponseEntity<RetrospectiveScrollResponse> scrollAll(
            @AuthenticationPrincipal(expression = "userId") Long userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate before,
            @RequestParam(defaultValue = "15") int limit
    ) {
        RetrospectiveScrollResponse resp = retrospectiveService.getUserScroll(userId, before, limit);
        return ResponseEntity.ok(resp);
    }
}

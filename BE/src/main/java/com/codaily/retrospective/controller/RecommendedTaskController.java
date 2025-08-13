package com.codaily.retrospective.controller;

import com.codaily.retrospective.dto.RecommendedTaskListResponse;
import com.codaily.retrospective.service.RecommendedTaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/projects/{projectId}/retrospective/recommended-tasks")
@RequiredArgsConstructor
@Tag(name = "Recommended Task API", description = "회고 페이지 추천 작업 제안 API")
public class RecommendedTaskController {

    private final RecommendedTaskService recommendedTaskService;

    @Operation(
            summary = "추천 작업 목록 조회",
            description = "회고 페이지에서 사용할 추천 작업 목록을 반환합니다. " +
                    "완료되거나 진행중인 작업을 제외하고 우선순위가 낮은(숫자가 큰) TODO 상태의 작업들을 우선순위 순으로 반환합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "추천 작업 목록 조회 성공",
                    content = @Content(
                            schema = @Schema(implementation = RecommendedTaskListResponse.class),
                            examples = @ExampleObject(
                                    name = "성공 응답 예시",
                                    value = """
                    {
                      "projectId": 1,
                      "totalRecommendedTasks": 3,
                      "message": null,
                      "recommendedTasks": [
                        {
                          "featureId": 105,
                          "title": "사용자 프로필 이미지 업로드",
                          "description": "사용자가 프로필 이미지를 업로드할 수 있는 기능",
                          "field": "사용자 관리",
                          "category": "UI/UX",
                          "priorityLevel": 5,
                          "estimatedTime": 2.5,
                          "reason": "낮은 우선순위로 시간이 있을 때 진행하기 좋은 작업입니다. 적당한 분량으로 체계적으로 진행하기 좋습니다. 사용자 경험 개선에 도움이 됩니다."
                        },
                        {
                          "featureId": 108,
                          "title": "알림 설정 기능",
                          "description": "사용자별 알림 on/off 설정 기능",
                          "field": "알림",
                          "category": "API",
                          "priorityLevel": 4,
                          "estimatedTime": 1.5,
                          "reason": "낮은 우선순위로 시간이 있을 때 진행하기 좋은 작업입니다. 짧은 시간에 완료할 수 있어 부담 없이 시작할 수 있습니다. 백엔드 기능 강화에 기여합니다."
                        },
                        {
                          "featureId": 110,
                          "title": "데이터베이스 인덱스 최적화",
                          "description": "검색 성능 향상을 위한 인덱스 추가",
                          "field": "성능 최적화",
                          "category": "Database",
                          "priorityLevel": 4,
                          "estimatedTime": 3.0,
                          "reason": "낮은 우선순위로 시간이 있을 때 진행하기 좋은 작업입니다. 적당한 분량으로 체계적으로 진행하기 좋습니다. 데이터 관리 개선에 필요합니다."
                        }
                      ]
                    }
                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "200",
                    description = "추천할 작업이 없는 경우",
                    content = @Content(
                            examples = @ExampleObject(
                                    name = "추천 작업 없음",
                                    value = """
                    {
                      "projectId": 1,
                      "totalRecommendedTasks": 0,
                      "message": "현재 추천할 수 있는 작업이 없습니다. 모든 작업이 완료되었거나 진행 중입니다.",
                      "recommendedTasks": []
                    }
                    """
                            )
                    )
            ),
            @ApiResponse(responseCode = "404", description = "프로젝트를 찾을 수 없음"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 파라미터")
    })
    @GetMapping
    public ResponseEntity<RecommendedTaskListResponse> getRecommendedTasks(
            @Parameter(description = "프로젝트 ID", example = "1")
            @PathVariable Long projectId,

            @Parameter(description = "최대 추천 작업 수 (기본값: 5, 최대: 10)", example = "5")
            @RequestParam(value = "limit", required = false, defaultValue = "5") Integer limit
    ) {

        // limit 유효성 검사
        if (limit <= 0 || limit > 10) {
            limit = 5; // 기본값으로 설정
        }

        RecommendedTaskListResponse response = recommendedTaskService.getRecommendedTasks(projectId, limit);
        return ResponseEntity.ok(response);
    }
}
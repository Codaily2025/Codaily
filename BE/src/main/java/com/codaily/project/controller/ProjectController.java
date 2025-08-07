package com.codaily.project.controller;

import com.codaily.auth.config.PrincipalDetails;
import com.codaily.project.dto.FeatureItemReduceResponse;
import com.codaily.project.dto.ProjectCreateRequest;
import com.codaily.project.entity.Project;
import com.codaily.project.entity.Specification;
import com.codaily.project.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Map;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
@Tag(name = "Project API", description = "프로젝트 생성 및 기능 명세 축소 관련 API")
public class ProjectController {

    private final ProjectService projectService;

    @Operation(
            summary = "프로젝트 생성(일정 생성 페이지에서 '다음으로' 버튼 클릭 시 실행되어야 합니다.)",
            description = "신규 프로젝트를 생성하고 해당 프로젝트에 연결된 기본 명세(specification)도 함께 생성합니다.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "생성할 프로젝트의 시작일, 종료일, 작업 가능 날짜 및 요일별 작업 시간",
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    name = "Project 생성 요청 예시",
                                    value = """
                                            {
                                              "startDate": "2025-08-10",
                                              "endDate": "2025-09-20",
                                              "availableDates": [
                                                "2025-08-12",
                                                "2025-08-15",
                                                "2025-08-20"
                                              ],
                                              "workingHours": {
                                                "MONDAY": 4,
                                                "WEDNESDAY": 6,
                                                "FRIDAY": 2
                                              }
                                            }
                                            """
                            )
                    )
            )
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "프로젝트 및 명세 생성 성공",
                    headers = {
                            @Header(name = "Location", description = "생성된 프로젝트의 URI (/api/projects/{projectId})")
                    },
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    name = "성공 응답 예시",
                                    value = """
                                            {
                                              "projectId": 12,
                                              "specId": 31
                                            }
                                            """
                            )
                    )
            )
    })
    @PostMapping
    public ResponseEntity<Map<String, Long>> createProject(
            @AuthenticationPrincipal PrincipalDetails userDetails,
            @RequestBody ProjectCreateRequest request
    ) {
        Project project = projectService.createProject(request, userDetails.getUser());
        Specification spec = project.getSpecification(); // 또는 명세 생성 메서드 결과

        Map<String, Long> responseBody = Map.of(
                "projectId", project.getProjectId(),
                "specId", spec.getSpecId()
        );

        URI location = URI.create("/api/projects/" + project.getProjectId());
        return ResponseEntity.created(location).body(responseBody);
    }

    @Operation(
            summary = "기능 축소",
            description = "기능이 너무 많은 경우 일부 기능을 자동 축소합니다. \n\n" +
                    "사용자의 작업 가능 시간(`totalAvailableTime`)과 기능의 전체 예상 소요 시간(`totalEstimatedTime`)을 비교하여 " +
                    "우선순위 및 소요 시간을 기준으로 일부 기능을 제외하고(`isReduced=true`) 나머지를 유지합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "기능 축소 결과 반환",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    name = "축소 응답 예시",
                                    value = """
                                            {
                                              "totalEstimatedTime": 34,
                                              "totalAvailableTime": 20,
                                              "reducedCount": 2,
                                              "keptCount": 3,
                                              "features": [
                                                {
                                                  "featureId": 101,
                                                  "title": "장바구니 담기",
                                                  "description": "상품을 장바구니에 추가하는 기능",
                                                  "estimatedTime": 3,
                                                  "priorityLevel": 1,
                                                  "isReduced": false
                                                },
                                                {
                                                  "featureId": 102,
                                                  "title": "상품 상세 페이지",
                                                  "description": "상품에 대한 정보를 자세히 보여주는 화면",
                                                  "estimatedTime": 4,
                                                  "priorityLevel": 2,
                                                  "isReduced": false
                                                },
                                                {
                                                  "featureId": 103,
                                                  "title": "주문 내역 확인",
                                                  "description": "이전에 주문한 상품 목록 확인 기능",
                                                  "estimatedTime": 5,
                                                  "priorityLevel": 3,
                                                  "isReduced": false
                                                },
                                                {
                                                  "featureId": 104,
                                                  "title": "리뷰 신고 기능",
                                                  "description": "부적절한 리뷰를 신고하는 기능",
                                                  "estimatedTime": 2,
                                                  "priorityLevel": 5,
                                                  "isReduced": true
                                                },
                                                {
                                                  "featureId": 105,
                                                  "title": "사용자 알림 설정",
                                                  "description": "푸시 알림 수신 여부 설정",
                                                  "estimatedTime": 2,
                                                  "priorityLevel": 6,
                                                  "isReduced": true
                                                }
                                              ]
                                            }
                                            """
                            )
                    )
            )
    })
    @PostMapping("/{projectId}/specs/{specId}/features/reduce")
    public ResponseEntity<FeatureItemReduceResponse> reduceFeatures(
            @Parameter(description = "프로젝트 ID") @PathVariable Long projectId,
            @Parameter(description = "명세서 ID") @PathVariable Long specId
    ) {
        FeatureItemReduceResponse response = projectService.reduceFeatureItemsIfNeeded(projectId, specId);
        return ResponseEntity.ok(response);
    }

}

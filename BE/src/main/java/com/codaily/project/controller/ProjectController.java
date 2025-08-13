package com.codaily.project.controller;

import com.codaily.auth.config.PrincipalDetails;
import com.codaily.project.dto.*;
import com.codaily.project.entity.Project;
import com.codaily.project.entity.Specification;
import com.codaily.project.service.FeatureItemService;
import com.codaily.project.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
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
    private final FeatureItemService featureItemService;

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

    @Operation(
            summary = "감축 플래그 토글",
            description = """
                    프로젝트의 기능에 대해 감축 플래그(isReduced)를 설정/해제합니다.
                    
                    대상 선택 규칙:
                    - featureId가 주어지면 해당 기능(및 하위 트리 전체)을 대상으로 합니다.
                    - featureId가 없고 field가 주어지면 해당 필드의 모든 기능을 대상으로 합니다.
                    - field와 featureId는 동시에 줄 수 없습니다.
                    """
    )
    @Parameters({
            @Parameter(name = "projectId", description = "프로젝트 ID", required = true, example = "101"),
            @Parameter(name = "field", description = "기능 그룹(필드)명. 예: '계정', '결제'", required = false, example = "계정"),
            @Parameter(name = "featureId", description = "대상 기능 ID. 주어지면 단건(+하위 트리) 처리", required = false, example = "987")
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            description = "감축 여부 설정 바디",
            content = @Content(
                    schema = @Schema(implementation = ToggleReduceRequest.class),
                    examples = {
                            @ExampleObject(name = "단건/트리 감축 활성화", value = "{ \"isReduced\": true }"),
                            @ExampleObject(name = "필드 전체 감축 해제", value = "{ \"isReduced\": false }")
                    }
            )
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "성공 (본문 없음)"),
            @ApiResponse(
                    responseCode = "400",
                    description = "요청 오류",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "isReduced 누락",
                                            value = """
                                                    {
                                                      "code": "BAD_REQUEST",
                                                      "message": "isReduced 값이 필요합니다."
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "field/featureId 동시 또는 모두 누락",
                                            value = """
                                                    {
                                                      "code": "BAD_REQUEST",
                                                      "message": "field 또는 featureId 중 하나만 지정해야 합니다."
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "프로젝트 없음",
                                            value = """
                                                    {
                                                      "code": "BAD_REQUEST",
                                                      "message": "존재하지 않는 프로젝트입니다. projectId=101"
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "기능-프로젝트 불일치",
                                            value = """
                                                    {
                                                      "code": "BAD_REQUEST",
                                                      "message": "해당 기능이 프로젝트에 속하지 않습니다. featureId=987"
                                                    }
                                                    """
                                    )
                            }
                    )
            )
    })
    @PatchMapping("/{projectId}/specification/reduce")
    public ResponseEntity<Void> patchReduceFlag(
            @PathVariable Long projectId,
            @RequestParam(required = false) String field,
            @RequestParam(required = false) Long featureId,
            @org.springframework.web.bind.annotation.RequestBody ToggleReduceRequest body
    ) {
        featureItemService.updateIsReduced(projectId, field, featureId, body.getIsReduced());
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "스펙 확정 (감축 항목 삭제)",
            description = """
                    프로젝트에 연결된 스펙에서 isReduced=true 인 기능들을 모두 삭제합니다.
                    삭제 결과(삭제 개수 등)를 본문으로 반환합니다.
                    """
    )
    @Parameters({
            @Parameter(name = "projectId", description = "프로젝트 ID", required = true, example = "101")
    })
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "성공",
                    content = @Content(
                            schema = @Schema(implementation = SpecificationFinalizeResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "삭제 발생",
                                            value = """
                                                    {
                                                      "projectId": 101,
                                                      "specId": 555,
                                                      "deletedCount": 7
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "삭제할 항목 없음",
                                            value = """
                                                    {
                                                      "projectId": 101,
                                                      "specId": 555,
                                                      "deletedCount": 0
                                                    }
                                                    """
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "요청 오류",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "프로젝트 없음",
                                            value = """
                                                    {
                                                      "code": "BAD_REQUEST",
                                                      "message": "존재하지 않는 프로젝트입니다. id=101"
                                                    }
                                                    """
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "스펙 미연결",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "프로젝트에 스펙 없음",
                                    value = """
                                            {
                                              "code": "NOT_FOUND",
                                              "message": "프로젝트에 연결된 스펙이 없습니다. projectId=101"
                                            }
                                            """
                            )
                    )
            )
    })
    @PostMapping("/{projectId}/specification/finalize")
    public ResponseEntity<SpecificationFinalizeResponse> finalizeSpecification(
            @PathVariable Long projectId
    ) {
        SpecificationFinalizeResponse resp = featureItemService.finalizeSpecification(projectId);
        return ResponseEntity.ok(resp);
    }

    @Operation(
            summary = "프로젝트 명세 통합 조회",
            description = """
            프로젝트 기본 정보(제목/설명/연결된 명세 정보)와
            해당 프로젝트의 모든 명세 항목(주기능 + 각 주기능의 하위기능)을 반환합니다.
            
            - features: 주기능 단위 리스트
            - features[*].subFeature: 해당 주기능의 하위기능 리스트
            - features에는 하위기능이 별도 요소로 들어가지 않으며, 항상 주기능만 포함됩니다.
            """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ProjectSpecOverviewResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "성공 예시(명세 있음)",
                                            summary = "프로젝트 + 명세 + 주/하위 기능 전체",
                                            value = """
                    {
                      "project": {
                        "projectTitle": "Codaily",
                        "projectDescription": "AI 기반 명세/회고 플랫폼",
                        "specTitle": "v1.0 기능 명세",
                        "projectId": 3,
                        "specId": 10
                      },
                      "features": [
                        {
                          "projectId": 3,
                          "specId": 10,
                          "field": "회원",
                          "isReduced": false,
                          "mainFeature": {
                            "id": 101,
                            "isReduced": false,
                            "title": "회원 가입",
                            "description": "이메일/소셜 가입을 지원",
                            "estimatedTime": 2.5,
                            "priorityLevel": 1
                          },
                          "subFeature": [
                            {
                              "id": 201,
                              "isReduced": false,
                              "title": "이메일 인증",
                              "description": "토큰 기반 인증",
                              "estimatedTime": 0.5,
                              "priorityLevel": 1
                            },
                            {
                              "id": 202,
                              "isReduced": false,
                              "title": "소셜 로그인",
                              "description": "카카오/네이버/구글 로그인 지원",
                              "estimatedTime": 1.0,
                              "priorityLevel": 2
                            }
                          ]
                        },
                        {
                          "projectId": 3,
                          "specId": 10,
                          "field": "결제",
                          "isReduced": true,
                          "mainFeature": {
                            "id": 102,
                            "isReduced": true,
                            "title": "PG 결제 연동",
                            "description": "정기결제 포함",
                            "estimatedTime": 3.0,
                            "priorityLevel": 2
                          },
                          "subFeature": [
                            {
                              "id": 203,
                              "isReduced": false,
                              "title": "카드 결제",
                              "description": "신용/체크카드 결제 처리",
                              "estimatedTime": 1.5,
                              "priorityLevel": 1
                            },
                            {
                              "id": 204,
                              "isReduced": false,
                              "title": "간편 결제",
                              "description": "카카오페이/네이버페이 연동",
                              "estimatedTime": 1.0,
                              "priorityLevel": 2
                            }
                          ]
                        }
                      ]
                    }
                    """
                                    ),
                                    @ExampleObject(
                                            name = "성공 예시(명세 없음)",
                                            summary = "프로젝트는 있지만 spec 연결이 아직 없음 (예시용 subFeature 포함)",
                                            value = """
                    {
                      "project": {
                        "projectTitle": "Empty Spec Project",
                        "projectDescription": "명세 미연결 상태",
                        "specTitle": null,
                        "projectId": 7,
                        "specId": null
                      },
                      "features": [
                        {
                          "projectId": 7,
                          "specId": null,
                          "field": "기본",
                          "isReduced": false,
                          "mainFeature": {
                            "id": 301,
                            "isReduced": false,
                            "title": "임시 주기능",
                            "description": "명세 없음 상태의 예시 주기능",
                            "estimatedTime": 0.0,
                            "priorityLevel": 1
                          },
                          "subFeature": [
                            {
                              "id": 401,
                              "isReduced": false,
                              "title": "임시 하위기능",
                              "description": "명세 없음 상태의 예시 하위기능",
                              "estimatedTime": 0.0,
                              "priorityLevel": 1
                            }
                          ]
                        }
                      ]
                    }
                    """
                                    )
                            }
                    )
            )
    })
    @GetMapping("/{projectId}/spec")
    public ResponseEntity<ProjectSpecOverviewResponse> getProjectSpecOverview(
            @Parameter(description = "조회할 프로젝트 ID", example = "3")
            @PathVariable Long projectId
    ) {
        return ResponseEntity.ok(projectService.getProjectSpecOverview(projectId));
    }
}

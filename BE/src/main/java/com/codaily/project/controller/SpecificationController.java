package com.codaily.project.controller;

import com.codaily.common.gpt.dto.ChatStreamRequest;
import com.codaily.common.gpt.handler.ChatResponseStreamHandler;
import com.codaily.project.dto.SpecificationRegenerateRequest;
import com.codaily.project.dto.SpecificationTimeResponse;
import com.codaily.project.service.FeatureItemService;
import com.codaily.project.service.ProjectService;
import com.codaily.project.service.SpecificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/specifications")
@RequiredArgsConstructor
@Tag(name = "Specification API", description = "명세서 재생성, 총 시간 계산, PDF 다운로드 기능")
public class SpecificationController {

    private final ChatResponseStreamHandler chatResponseStreamHandler;
    private final SpecificationService specificationService;
    private final FeatureItemService featureItemService;
    private final ProjectService projectService;

    @Operation(
            summary = "명세서 재생성 (SSE)",
            description = """
                        기존 명세서를 삭제하고 새롭게 명세서를 생성합니다.
                    
                        생성된 주 기능 + 상세 기능 조각이 실시간으로 순차 전송되며,
                        각 조각은 event: message 형식으로 전송됩니다.
                    
                        요청은 JSON 본문으로 전달되며, 응답은 하나의 기능 그룹(field) 단위로 반복 전송됩니다.
                    """,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "명세서 재생성 요청 정보",
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    name = "요청 예시",
                                    value = """
                                            {
                                              "projectId": 3,
                                              "userId": "system"
                                            }
                                            """
                            )
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "SSE 응답 시작 - 기능 명세 조각이 여러 개 순차 전송됨",
                            content = @Content(
                                    mediaType = MediaType.TEXT_EVENT_STREAM_VALUE,
                                    examples = @ExampleObject(
                                            name = "기능 조각 예시",
                                            summary = "하나의 기능 그룹 조각",
                                            value = """
                                                    event: message
                                                    data: {
                                                      "type": "spec:regenerate",
                                                      "content": {
                                                        "projectId": 3,
                                                        "specId": 1,
                                                        "field": "상품 관리 및 카탈로그 기능",
                                                        "mainFeature": {
                                                          "id": 2075,
                                                          "title": "상품 등록",
                                                          "description": "사용자가 새로운 상품을 시스템에 추가할 수 있음",
                                                          "estimatedTime": 3,
                                                          "priorityLevel": null
                                                        },
                                                        "subFeature": [
                                                          {
                                                            "id": 2076,
                                                            "title": "상품 등록페이지 접근",
                                                            "description": "사용자가 상품 등록 페이지로 이동하는 기능",
                                                            "estimatedTime": 2,
                                                            "priorityLevel": 8
                                                          },
                                                          {
                                                            "id": 2077,
                                                            "title": "상품 정보 입력폼 표시",
                                                            "description": "사용자가 상품명, 가격, 설명 등을 입력할 수 있는 폼을 화면에 띄우기",
                                                            "estimatedTime": 1,
                                                            "priorityLevel": 9
                                                          }
                                                        ]
                                                      }
                                                    }
                                                    """
                                    )
                            )
                    )
            }
    )
    @PostMapping(value = "/{specId}/regenerate", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter regenerate(
            @Parameter(description = "명세서 ID") @PathVariable Long specId,
            @RequestBody SpecificationRegenerateRequest request
    ) {
        return chatResponseStreamHandler.stream(new ChatStreamRequest(
                "spec:regenerate",
                "[SYSTEM] 명세서를 다시 생성해 주세요.",
                request.getProjectId(),
                specId,
                null,
                null
        ));
    }

    @Operation(
            summary = "명세서 총 소요 시간 조회",
            description = "해당 명세서의 모든 기능(하위 기능 포함)에 대한 예상 소요 시간의 총합을 분 단위로 반환합니다.",
            parameters = {
                    @Parameter(name = "specId", description = "명세서 ID", required = true, example = "1")
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "소요 시간 계산 성공",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    examples = @ExampleObject(
                                            name = "예상 소요 시간 응답 예시",
                                            value = """
                                                    {
                                                      "specId": 1,
                                                      "totalEstimatedTime": 47
                                                    }
                                                    """
                                    )
                            )
                    )
            }
    )
    @GetMapping("/{specId}/total-time")
    public ResponseEntity<SpecificationTimeResponse> getTotalEstimatedTime(
            @PathVariable Long specId
    ) {
        int totalTime = featureItemService.calculateTotalEstimatedTime(specId);
        SpecificationTimeResponse response = new SpecificationTimeResponse(specId, totalTime);
        return ResponseEntity.ok(response);
    }


    @GetMapping("/{projectId}/document")
    @Operation(
            summary = "명세서 PDF 다운로드",
            description = "해당 프로젝트의 명세서를 PDF 형식으로 생성하여 다운로드합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "PDF 다운로드 성공",
                            content = @Content(mediaType = MediaType.APPLICATION_PDF_VALUE)
                    ),
//                    @ApiResponse(responseCode = "404", description = "프로젝트 또는 명세서 없음"),
//                    @ApiResponse(responseCode = "500", description = "서버 오류")
            }
    )
    public ResponseEntity<byte[]> downloadSpecDocument(
            @Parameter(description = "프로젝트 ID") @PathVariable Long projectId
    ) {
        byte[] pdf = specificationService.generateSpecDocument(projectId);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"specification.pdf\"")
                .body(pdf);
    }
}

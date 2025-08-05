package com.codaily.project.controller;

import com.codaily.common.gpt.handler.ChatResponseStreamHandler;
import com.codaily.project.dto.SpecificationTimeResponse;
import com.codaily.project.service.FeatureItemService;
import com.codaily.project.service.SpecificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
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

    @PostMapping(value = "/{specId}/regenerate", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(
            summary = "명세서 재생성 (SSE)",
            description = """
        기존 명세서를 삭제하고 새로운 명세서를 실시간으로 생성합니다. 
        결과는 SSE(Stream) 방식으로 반환되며, 생성 중간 상태도 스트리밍됩니다.
        """,
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "명세서 재생성 스트리밍 시작",
                            content = @Content(mediaType = MediaType.TEXT_EVENT_STREAM_VALUE)
                    ),
//                    @ApiResponse(
//                            responseCode = "400",
//                            description = "요청 파라미터 오류 (예: 존재하지 않는 명세서 ID)"
//                    ),
//                    @ApiResponse(
//                            responseCode = "500",
//                            description = "서버 오류"
//                    )
            }
    )
    public SseEmitter regenerate(
            @Parameter(description = "명세서 ID") @PathVariable Long specId,
            @Parameter(description = "프로젝트 ID") @RequestParam Long projectId,
            @Parameter(description = "사용자 ID", example = "system") @RequestParam(defaultValue = "system") String userId
    ) {
        String defaultMessage = "[SYSTEM] 명세서를 다시 생성해 주세요.";
        return chatResponseStreamHandler.stream("spec:regenerate", userId, defaultMessage, projectId, specId);
    }

    @GetMapping("/{specId}/total-time")
    @Operation(
            summary = "명세서 총 소요 시간 조회",
            description = "해당 명세서의 모든 기능(하위 기능 포함)에 대한 예상 소요 시간의 총합을 분 단위로 반환합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "소요 시간 계산 성공"),
//                    @ApiResponse(responseCode = "404", description = "명세서를 찾을 수 없음"),
//                    @ApiResponse(responseCode = "500", description = "서버 오류")
            }
    )
    public ResponseEntity<SpecificationTimeResponse> getTotalEstimatedTime(
            @Parameter(description = "명세서 ID", required = true) @PathVariable Long specId
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

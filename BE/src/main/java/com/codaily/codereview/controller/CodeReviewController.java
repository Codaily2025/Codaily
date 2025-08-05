package com.codaily.codereview.controller;

import com.codaily.codereview.dto.*;
import com.codaily.codereview.service.CodeReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/code-review")
@RequiredArgsConstructor
@Slf4j
public class CodeReviewController {

    private final CodeReviewService codeReviewService;

    @PostMapping("/code-review/run")
    public ResponseEntity<CodeReviewRunResponseDto> runCodeReview(
            @RequestBody CodeReviewRunRequestDto requestDto
    ) {
        // 비동기 처리 + SSE 예정
        codeReviewService.runCodeReviewAsync(requestDto);

        return ResponseEntity.accepted().body(
                CodeReviewRunResponseDto.builder()
                        .status("started")
                        .featureName(null) // 결과 처리 후 갱신 예정
                        .implementsFeature(null)
                        .reviewCount(0)
                        .build()
        );
    }


    // 기능명 추론 결과 응답 수신
    @PostMapping("/feature-inference/result")
    public ResponseEntity<Void> receiveFeatureInferenceResult(
            @RequestBody FeatureInferenceResponseDto response
    ) {
        // ❗ 기능 없음이면 파이프라인 종료
        if ("기능 없음".equals(response.getFeatureName())) {
            log.info("❌ 기능 없음 - 파이프라인 중단");
            return ResponseEntity.ok().build();
        }

        // ✅ 기능 있음이면 이후 checklist 평가 요청 수행
        codeReviewService.handleFeatureInferenceResult(response);
        return ResponseEntity.ok().build();
    }

    // 기능 구현 결과 응답
    @PostMapping("/checklist-evaluation/result")
    public ResponseEntity<Void> receiveChecklistEvaluationResult(
            @RequestBody ChecklistEvaluationResponseDto response
    ) {
        codeReviewService.handleChecklistEvaluationResult(response);
        return ResponseEntity.ok().build();
    }

    // 체크리스트별 코드리뷰 응답
    @PostMapping("/code-review/items")
    public ResponseEntity<Void> receiveCodeReviewItems(
            @RequestBody FeatureReviewResultDto dto
    ) {
        codeReviewService.handleCodeReviewItems(dto);
        return ResponseEntity.ok().build();
    }

    // 기능 코드리뷰 요약 응답
    @PostMapping("/code-review/summary")
    public ResponseEntity<Void> receiveReviewSummary(@RequestBody FeatureReviewSummaryDto dto) {
        codeReviewService.saveCodeReviewSummary(dto);
        return ResponseEntity.ok().build();
    }
}




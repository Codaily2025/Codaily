package com.codaily.codereview.controller;

import com.codaily.codereview.dto.ChecklistEvaluationResponseDto;
import com.codaily.codereview.dto.FeatureInferenceResponseDto;
import com.codaily.codereview.dto.FeatureReviewResultDto;
import com.codaily.codereview.dto.FeatureReviewSummaryDto;
import com.codaily.codereview.service.CodeReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/python")
@RequiredArgsConstructor
@Slf4j
public class PythonCallBackController {

    private final CodeReviewService codeReviewService;

    // 기능명 추론 결과 응답 수신
    @PostMapping("/feature-inference/result")
    public ResponseEntity<Void> receiveFeatureInferenceResult(
            @RequestBody FeatureInferenceResponseDto response
    ) {
        // ❗ 기능 없음이면 파이프라인 종료
        if (response.getFeatureNames().isEmpty() || response.getFeatureNames() == null) {
            log.info("❌ 기능 없음 - 파이프라인 중단");
            return ResponseEntity.ok().build();
        }

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

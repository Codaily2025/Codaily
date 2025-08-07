// ProductivityController.java
package com.codaily.project.controller;

import com.codaily.auth.config.PrincipalDetails;
import com.codaily.project.dto.*;
import com.codaily.project.service.ProductivityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ProductivityController {

    private final ProductivityService productivityService;

    /**
     * 생산성 지표 계산
     * POST /api/productivity/calculate
     */
    @PostMapping("/productivity/calculate")
    public ResponseEntity<ProductivityCalculateResponse> calculateProductivity(
            @RequestBody ProductivityCalculateRequest request,
            @AuthenticationPrincipal PrincipalDetails userDetails) {

        log.info("생산성 지표 계산 요청 - userId: {}, projectId: {}, date: {}",
                request.getUserId(), request.getProjectId(), request.getPeriod().getDate());

        try {
            ProductivityCalculateResponse response = productivityService.calculateProductivity(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("생산성 지표 계산 중 오류 발생", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 생산성 그래프 데이터 조회
     * GET /api/projects/{projectId}/analytics/productivity
     */
    @GetMapping("/projects/{projectId}/analytics/productivity")
    public ResponseEntity<ProductivityChartResponse> getProductivityChart(
            @PathVariable Long projectId,
            @RequestParam String period,
            @RequestParam String startDate,
            @RequestParam String endDate,
            @AuthenticationPrincipal PrincipalDetails userDetails) {

        log.info("생산성 그래프 데이터 조회 - projectId: {}, period: {}, startDate: {}, endDate: {}",
                projectId, period, startDate, endDate);

        try {
            // period 검증
            if (!"weekly".equals(period) && !"monthly".equals(period)) {
                log.warn("잘못된 period 파라미터: {}", period);
                return ResponseEntity.badRequest().build();
            }

            // 날짜 파싱 검증
            LocalDate inputDate = LocalDate.parse(startDate);
            LocalDate start, end;

            if ("weekly".equals(period)) {
                // 해당 날짜가 속한 주의 월요일~일요일
                start = inputDate.with(java.time.DayOfWeek.MONDAY);
                end = inputDate.with(java.time.DayOfWeek.SUNDAY);
            } else { // monthly
                // 해당 날짜가 속한 월의 1일~말일
                start = inputDate.withDayOfMonth(1);
                end = inputDate.withDayOfMonth(inputDate.lengthOfMonth());
            }

            Long userId = userDetails.getUserId();
            ProductivityChartResponse response = productivityService.getProductivityChart(
                    userId, projectId, period, start.toString(), end.toString());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("생산성 그래프 데이터 조회 중 오류 발생", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 생산성 그래프 일별 상세 조회
     * GET /api/projects/{projectId}/analytics/productivity/{date}/details
     */
    @GetMapping("/projects/{projectId}/analytics/productivity/{date}/details")
    public ResponseEntity<ProductivityDetailResponse> getProductivityDetail(
            @PathVariable Long projectId,
            @PathVariable String date,
            @AuthenticationPrincipal PrincipalDetails userDetails) {

        log.info("생산성 일별 상세 조회 - projectId: {}, date: {}", projectId, date);

        try {
            Long userId = userDetails.getUserId();
            ProductivityDetailResponse response = productivityService.getProductivityDetail(userId, projectId, date);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("생산성 일별 상세 조회 중 오류 발생", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
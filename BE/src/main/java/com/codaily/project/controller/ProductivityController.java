// ProductivityController.java
package com.codaily.project.controller;

import com.codaily.auth.config.PrincipalDetails;
import com.codaily.project.dto.*;
import com.codaily.project.service.ProductivityService;
import io.swagger.v3.oas.annotations.Operation;
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

    //생산성 지표 계산
    @Operation(summary = "생산성 지표 계산")
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

    //생산성 그래프 데이터 조회
    @Operation(summary = "생산성 그래프 데이터 조회", description = "단위(주/월) 기준")
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

    //생산성 그래프 일별 상세 조회
    @Operation(summary = "생산성 그래프 일별 조회")
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

    @Operation(summary = "전체 프로젝트 생산성 그래프 데이터 조회", description = "사용자의 모든 프로젝트를 통합한 생산성 차트")
    @GetMapping("/users/{userId}/analytics/productivity/overall")
    public ResponseEntity<ProductivityChartResponse> getOverallProductivityChart(
            @PathVariable Long userId,
            @RequestParam String period,
            @RequestParam String startDate,
            @RequestParam String endDate,
            @AuthenticationPrincipal PrincipalDetails userDetails) {

        log.info("전체 생산성 그래프 데이터 조회 - userId: {}, period: {}, startDate: {}, endDate: {}",
                userId, period, startDate, endDate);

        try {
            // 요청한 userId와 인증된 사용자가 같은지 확인
            if (!userId.equals(userDetails.getUserId())) {
                log.warn("권한 없는 사용자의 생산성 데이터 접근 시도 - requestUserId: {}, authUserId: {}",
                        userId, userDetails.getUserId());
                return ResponseEntity.badRequest().build();
            }

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

            ProductivityChartResponse response = productivityService.getOverallProductivityChart(
                    userId, period, start.toString(), end.toString());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("전체 생산성 그래프 데이터 조회 중 오류 발생", e);
            return ResponseEntity.internalServerError().build();
        }
    }

}


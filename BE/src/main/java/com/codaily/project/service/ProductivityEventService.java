package com.codaily.project.service;

import com.codaily.project.dto.ProductivityCalculateRequest;
import com.codaily.project.repository.DailyProductivityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductivityEventService {

    private final ProductivityService productivityService;
    private final DailyProductivityRepository dailyProductivityRepository;

    @Async
    public void updateProductivityOnCommit(Long projectId, Long userId, LocalDateTime commitTime) {
        LocalDate date = commitTime.toLocalDate();
        log.info("커밋 이벤트로 생산성 업데이트 - projectId: {}, date: {}", projectId, date);
        recalculateDaily(userId, projectId, date);
    }

    @Async
    public void updateProductivityOnTaskComplete(Long projectId, Long userId, LocalDate date) {
        log.info("작업 완료 이벤트로 생산성 업데이트 - projectId: {}, date: {}", projectId, date);
        recalculateDaily(userId, projectId, date);
    }

    @Async
    public void updateProductivityOnReview(Long projectId, Long userId, LocalDate date) {
        log.info("코드리뷰 이벤트로 생산성 업데이트 - projectId: {}, date: {}", projectId, date);
        recalculateDaily(userId, projectId, date);
    }

    private void recalculateDaily(Long userId, Long projectId, LocalDate date) {
        // 실시간 계산하여 Daily Productivity 업데이트
        ProductivityCalculateRequest request = ProductivityCalculateRequest.builder()
                .userId(userId.toString())
                .projectId(projectId.toString())
                .period(ProductivityCalculateRequest.Period.builder().date(date.toString()).build())
                .metrics(ProductivityCalculateRequest.Metrics.builder()
                        .includeCommits(true)
                        .includeTaskCompletion(true)
                        .includeCodeQuality(true)
                        .build())
                .build();

        productivityService.calculateProductivity(request);
    }
}
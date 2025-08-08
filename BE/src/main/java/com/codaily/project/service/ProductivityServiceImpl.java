package com.codaily.project.service;

import com.codaily.codereview.entity.CodeCommit;
import com.codaily.codereview.entity.CodeReview;
import com.codaily.codereview.repository.CodeCommitRepository;
import com.codaily.codereview.repository.CodeReviewRepository;
import com.codaily.project.dto.*;
import com.codaily.project.entity.*;
import com.codaily.project.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductivityServiceImpl implements ProductivityService {

    private final TaskRepository taskRepository;
    private final CodeCommitRepository commitRepository;
    private final CodeReviewRepository reviewRepository;
    private final ProductivityMetricRepository productivityMetricRepository;
    private final DailyProductivityRepository dailyProductivityRepository;
    private final ChartDataRepository chartDataRepository;

    private static final String PRODUCTIVITY_CHART_TYPE = "productivity";

    @Override
    @Transactional
    public ProductivityCalculateResponse calculateProductivity(ProductivityCalculateRequest request) {
        Long userId = Long.valueOf(request.getUserId());
        Long projectId = Long.valueOf(request.getProjectId());
        LocalDate targetDate = LocalDate.parse(request.getPeriod().getDate());
        LocalDateTime startOfDay = targetDate.atStartOfDay();
        LocalDateTime endOfDay = targetDate.atTime(23, 59, 59);

        Map<String, ProductivityCalculateResponse.MetricScore> breakdown = new HashMap<>();
        double overallScore = 0.0;

        // 작업 완료율 계산 (40% 가중치)
        if (request.getMetrics().isIncludeTaskCompletion()) {
            List<Task> completedTasks = taskRepository.findCompletedTasks( projectId,
                    Task.Status.COMPLETED,
                    startOfDay,
                    endOfDay);
            double taskScore = calculateTaskScore(completedTasks.size());
            breakdown.put("taskCompletion", ProductivityCalculateResponse.MetricScore.builder()
                    .score(taskScore).weight(0.4).build());
            overallScore += taskScore * 0.4;
        }

        // 커밋 활동 계산 (30% 가중치)
        if (request.getMetrics().isIncludeCommits()) {
            List<CodeCommit> commits = commitRepository.findByCommittedAtBetween(startOfDay, endOfDay);
            double commitScore = calculateCommitScore(commits.size());
            breakdown.put("commitFrequency", ProductivityCalculateResponse.MetricScore.builder()
                    .score(commitScore).weight(0.3).build());
            overallScore += commitScore * 0.3;
        }

        // 코드 품질 계산 (20% 가중치)
        if (request.getMetrics().isIncludeCodeQuality()) {
            List<CodeReview> reviews = reviewRepository.findByProject_ProjectIdAndCreatedAtBetween(
                    projectId, startOfDay, endOfDay);
            double avgQuality = reviews.stream()
                    .mapToDouble(r -> r.getQualityScore() != null ? r.getQualityScore() : 0)
                    .average().orElse(80.0);
            breakdown.put("codeQuality", ProductivityCalculateResponse.MetricScore.builder()
                    .score(avgQuality).weight(0.2).build());
            overallScore += avgQuality * 0.2;
        }

        // 벤치마크 조회
        Double personalAvg = productivityMetricRepository.findPersonalAverageScore(userId);
        Double projectAvg = productivityMetricRepository.findProjectAverageScore(projectId);

        // 추세 계산 및 메트릭 저장
        String trend = calculateTrend(userId, projectId, overallScore);
        saveProductivityMetric(userId, projectId, targetDate, overallScore, breakdown, trend);

        return ProductivityCalculateResponse.builder()
                .overallScore(overallScore)
                .breakdown(breakdown)
                .trend(trend)
                .benchmarkComparison(ProductivityCalculateResponse.BenchmarkComparison.builder()
                        .personalAverage(personalAvg != null ? personalAvg : 0.0)
                        .projectAverage(projectAvg != null ? projectAvg : 0.0)
                        .build())
                .build();
    }

    @Override
    @Transactional
    public ProductivityChartResponse getProductivityChart(Long userId, Long projectId, String period, String startDate, String endDate) {
        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);

        // 일별 생산성 데이터 조회 및 차트 데이터 생성
        List<DailyProductivity> dailyData = dailyProductivityRepository
                .findByUserIdAndProjectIdAndDateBetween(userId, projectId, start, end);

        List<ProductivityChartResponse.ChartData> chartData = dailyData.stream()
                .map(this::convertToChartData)
                .collect(Collectors.toList());

        // 빈 날짜 채우기
        chartData = fillMissingDates(chartData, start, end);

        // 통계 계산
        Double avgTasks = dailyProductivityRepository.findAverageTasksPerDay(userId, projectId, start, end);
        Double avgScore = dailyProductivityRepository.findAverageProductivityScore(userId, projectId, start, end);
        String trend = calculateChartTrend(chartData);
        double trendPercentage = calculateTrendPercentage(chartData);

        ProductivityChartResponse.Summary summary = ProductivityChartResponse.Summary.builder()
                .averageTasksPerDay(avgTasks != null ? avgTasks : 0.0)
                .averageProductivityScore(avgScore != null ? avgScore : 0.0)
                .trend(trend)
                .trendPercentage(trendPercentage)
                .build();

        ProductivityChartResponse response = ProductivityChartResponse.builder()
                .success(true)
                .data(ProductivityChartResponse.Data.builder()
                        .period(period)
                        .chartData(chartData)
                        .summary(summary)
                        .build())
                .build();

        saveChartData(userId, projectId, period, response);
        return response;
    }

    @Override
    public ProductivityDetailResponse getProductivityDetail(Long userId, Long projectId, String date) {
        LocalDate targetDate = LocalDate.parse(date);
        LocalDateTime startOfDay = targetDate.atStartOfDay();
        LocalDateTime endOfDay = targetDate.atTime(23, 59, 59);

        // 완료된 작업 조회
        List<Task> completedTasks = taskRepository.findCompletedTasks( projectId,
                Task.Status.COMPLETED,
                startOfDay,
                endOfDay);
        List<ProductivityDetailResponse.CompletedTask> taskDtos = completedTasks.stream()
                .map(task -> ProductivityDetailResponse.CompletedTask.builder()
                        .id("task_" + String.format("%03d", task.getTaskId()))
                        .title(task.getTitle())
                        .build())
                .collect(Collectors.toList());

        // 커밋 조회
        List<CodeCommit> commits = commitRepository.findByCommittedAtBetween(startOfDay, endOfDay);
        List<ProductivityDetailResponse.Commit> commitDtos = commits.stream()
                .map(commit -> ProductivityDetailResponse.Commit.builder()
                        .hash(commit.getCommitHash())
                        .message(commit.getMessage())
                        .build())
                .collect(Collectors.toList());

        // 생산성 요소 계산
        Optional<DailyProductivity> dailyProductivity = dailyProductivityRepository
                .findByUserIdAndProjectIdAndDate(userId, projectId, targetDate);

        ProductivityDetailResponse.ProductivityFactors factors = ProductivityDetailResponse.ProductivityFactors.builder()
                .codeQuality(dailyProductivity.map(DailyProductivity::getCodeQuality).orElse(0.0))
                .completedTasks(completedTasks.size())
                .productivityScore(dailyProductivity.map(DailyProductivity::getProductivityScore).orElse(0.0))
                .build();

        return ProductivityDetailResponse.builder()
                .success(true)
                .data(ProductivityDetailResponse.Data.builder()
                        .date(date)
                        .completedTasks(taskDtos)
                        .commits(commitDtos)
                        .productivityFactors(factors)
                        .build())
                .build();
    }

    // ===== Helper Methods =====

    /**
     * 작업 완료 점수 계산
     * @param completedTasks 완료된 작업 수
     * @return 작업 점수 (최대 100점)
     */
    private double calculateTaskScore(int completedTasks) {
        return Math.min(completedTasks * 25.0, 100.0);
    }

    /**
     * 커밋 활동 점수 계산
     * @param commits 커밋 수
     * @return 커밋 점수 (최대 100점)
     */
    private double calculateCommitScore(int commits) {
        return Math.min(commits * 12.5, 100.0);
    }

    /**
     * 개인 생산성 추세 계산
     */
    private String calculateTrend(Long userId, Long projectId, double currentScore) {
        List<ProductivityMetric> recentMetrics = productivityMetricRepository
                .findRecentMetrics(userId, projectId);

        if (recentMetrics.isEmpty()) return "stable";

        double recentAvg = recentMetrics.stream()
                .mapToDouble(ProductivityMetric::getProductivityScore)
                .average().orElse(currentScore);

        if (currentScore > recentAvg + 5) return "improving";
        if (currentScore < recentAvg - 5) return "declining";
        return "stable";
    }

    /**
     * 차트 데이터 기반 추세 계산
     */
    private String calculateChartTrend(List<ProductivityChartResponse.ChartData> chartData) {
        if (chartData.size() < 2) return "stable";

        double first = chartData.get(0).getProductivityScore();
        double last = chartData.get(chartData.size() - 1).getProductivityScore();

        if (last > first + 5) return "increasing";
        if (last < first - 5) return "decreasing";
        return "stable";
    }

    /**
     * 추세 변화율 계산
     */
    private double calculateTrendPercentage(List<ProductivityChartResponse.ChartData> chartData) {
        if (chartData.size() < 2) return 0.0;

        double first = chartData.get(0).getProductivityScore();
        double last = chartData.get(chartData.size() - 1).getProductivityScore();

        if (first == 0) return 0.0;
        return Math.abs(((last - first) / first) * 100);
    }

    /**
     * Daily Productivity -> Chart Data 변환
     */
    private ProductivityChartResponse.ChartData convertToChartData(DailyProductivity daily) {
        return ProductivityChartResponse.ChartData.builder()
                .date(daily.getDate().toString())
                .completedTasks(daily.getCompletedTasks())
                .productivityScore(daily.getProductivityScore())
                .commits(daily.getCommits())
                .build();
    }

    /**
     * 기간 내 빈 날짜를 0값으로 채우기
     */
    private List<ProductivityChartResponse.ChartData> fillMissingDates(
            List<ProductivityChartResponse.ChartData> chartData, LocalDate start, LocalDate end) {

        Map<String, ProductivityChartResponse.ChartData> dataMap = chartData.stream()
                .collect(Collectors.toMap(
                        ProductivityChartResponse.ChartData::getDate,
                        data -> data,
                        (existing, replacement) -> existing));

        List<ProductivityChartResponse.ChartData> result = new ArrayList<>();
        LocalDate current = start;

        while (!current.isAfter(end)) {
            String dateStr = current.toString();
            ProductivityChartResponse.ChartData data = dataMap.getOrDefault(dateStr,
                    ProductivityChartResponse.ChartData.builder()
                            .date(dateStr)
                            .completedTasks(0)
                            .productivityScore(0.0)
                            .commits(0)
                            .build());
            result.add(data);
            current = current.plusDays(1);
        }

        return result;
    }

    /**
     * 생산성 메트릭 및 일별 데이터 저장
     */
    @Transactional
    public void saveProductivityMetric(Long userId, Long projectId, LocalDate date,
                                       double overallScore,
                                       Map<String, ProductivityCalculateResponse.MetricScore> breakdown,
                                       String trend) {

        // 기존 ProductivityMetric 중복 방지
        ProductivityMetric.ProductivityMetricBuilder builder = ProductivityMetric.builder()
                .userId(userId)
                .projectId(projectId)
                .date(date)
                .productivityScore(overallScore)
                .trend(ProductivityMetric.TrendType.valueOf(trend.toUpperCase()));

        if (breakdown.containsKey("taskCompletion")) {
            ProductivityCalculateResponse.MetricScore taskMetric = breakdown.get("taskCompletion");
            builder.taskCompletionScore(taskMetric.getScore())
                    .taskWeight(taskMetric.getWeight());
        }
        if (breakdown.containsKey("commitFrequency")) {
            ProductivityCalculateResponse.MetricScore commitMetric = breakdown.get("commitFrequency");
            builder.commitFrequencyScore(commitMetric.getScore())
                    .commitWeight(commitMetric.getWeight());
        }
        if (breakdown.containsKey("codeQuality")) {
            ProductivityCalculateResponse.MetricScore qualityMetric = breakdown.get("codeQuality");
            builder.codeQualityScore(qualityMetric.getScore())
                    .codeQualityWeight(qualityMetric.getWeight());
        }

        productivityMetricRepository.save(builder.build());

        // DailyProductivity Upsert (중복 방지)
        DailyProductivity daily = dailyProductivityRepository
                .findByUserIdAndProjectIdAndDate(userId, projectId, date)
                .orElseGet(() -> DailyProductivity.builder()
                        .userId(userId)
                        .projectId(projectId)
                        .date(date)
                        .build());

        daily.setCompletedTasks(breakdown.containsKey("taskCompletion") ?
                (int)(breakdown.get("taskCompletion").getScore() / 25) : 0);
        daily.setCommits(breakdown.containsKey("commitFrequency") ?
                (int)(breakdown.get("commitFrequency").getScore() / 12.5) : 0);
        daily.setCodeQuality(breakdown.containsKey("codeQuality") ?
                breakdown.get("codeQuality").getScore() : 0.0);
        daily.setProductivityScore(overallScore);

        dailyProductivityRepository.save(daily);
    }

    /**
     * 차트 데이터를 JSONB로 저장 (선택적)
     */
    private void saveChartData(Long userId, Long projectId, String period, ProductivityChartResponse response) {
        try {
            Map<String, Object> dataJson = new HashMap<>();
            dataJson.put("success", response.isSuccess());
            dataJson.put("data", response.getData());

            ChartData chartData = ChartData.builder()
                    .userId(userId)
                    .projectId(projectId)
                    .type(PRODUCTIVITY_CHART_TYPE)
                    .granularity(period)
                    .dataJson(dataJson)
                    .build();

            chartDataRepository.save(chartData);
            log.debug("차트 데이터 저장 완료 - userId: {}, projectId: {}, period: {}", userId, projectId, period);
        } catch (Exception e) {
            log.warn("차트 데이터 저장 실패 - userId: {}, projectId: {}, error: {}", userId, projectId, e.getMessage());
        }
    }
}
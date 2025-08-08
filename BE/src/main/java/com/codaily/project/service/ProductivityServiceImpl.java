package com.codaily.project.service;

<<<<<<< Updated upstream
import com.codaily.codereview.entity.CodeCommit;
import com.codaily.codereview.entity.CodeReview;
import com.codaily.codereview.repository.CodeCommitRepository;
import com.codaily.codereview.repository.CodeReviewRepository;
import com.codaily.project.dto.*;
import com.codaily.project.entity.*;
import com.codaily.project.repository.*;
=======
import com.codaily.project.dto.ProductivityCalculateRequest;
import com.codaily.project.dto.ProductivityCalculateResponse;
import com.codaily.project.dto.ProductivityChartResponse;
import com.codaily.project.dto.ProductivityDetailResponse;
import com.codaily.project.entity.DailyProductivity;
import com.codaily.project.entity.FeatureItem;
import com.codaily.project.entity.Project;
import com.codaily.project.repository.DailyProductivityRepository;
import com.codaily.project.repository.FeatureItemRepository;
import com.codaily.project.repository.ProjectRepository;
import lombok.Data;
>>>>>>> Stashed changes
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
<<<<<<< Updated upstream
=======
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
>>>>>>> Stashed changes
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
<<<<<<< Updated upstream
    private final ChartDataRepository chartDataRepository;

    private static final String PRODUCTIVITY_CHART_TYPE = "productivity";
=======
    private final ProjectRepository projectRepository;
>>>>>>> Stashed changes

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

<<<<<<< Updated upstream
        // 작업 완료율 계산 (40% 가중치)
=======
        // 해당 날짜의 완료된 기능들 조회
        List<FeatureItem> completedFeatures = featureItemRepository.findCompletedFeaturesByProjectAndDate(projectId, targetDate);

        // DB에서 해당 프로젝트의 특정 날짜 커밋 수 조회
        Integer totalCommits = getProjectCommitCountByDate(userId, projectId, targetDate);

        // 기능 완료율 계산 (가중치 70%)
>>>>>>> Stashed changes
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

<<<<<<< Updated upstream
        // 코드 품질 계산 (20% 가중치)
        if (request.getMetrics().isIncludeCodeQuality()) {
            List<CodeReview> reviews = reviewRepository.findByProjectIdAndCreatedAtBetween(
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
=======
        // DailyProductivity에 저장
        saveDailyProductivity(userId, projectId, targetDate, completedFeatures.size(), totalCommits, overallScore);
>>>>>>> Stashed changes

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

<<<<<<< Updated upstream
        // 일별 생산성 데이터 조회 및 차트 데이터 생성
        List<DailyProductivity> dailyData = dailyProductivityRepository
                .findByUserIdAndProjectIdAndDateBetween(userId, projectId, start, end);

        List<ProductivityChartResponse.ChartData> chartData = dailyData.stream()
                .map(this::convertToChartData)
                .collect(Collectors.toList());
=======
            log.info("프로젝트별 생산성 차트 조회 - projectId: {}, period: {}, startDate: {}, endDate: {}",
                    projectId, period, startDate, endDate);

            // DB에서 해당 프로젝트의 기간별 커밋 통계 조회
            Map<LocalDate, Integer> dailyCommitStats = getProjectDailyCommitStats(userId, projectId, start, end);
>>>>>>> Stashed changes

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

<<<<<<< Updated upstream
        saveChartData(userId, projectId, period, response);
        return response;
=======
            return ProductivityChartResponse.builder()
                    .success(true)
                    .data(ProductivityChartResponse.Data.builder()
                            .period(summary.getTrend())
                            .chartData(chartData)
                            .summary(summary)
                            .build())
                    .build();

        } catch (Exception e) {
            log.error("프로젝트별 생산성 차트 조회 실패", e);
            return ProductivityChartResponse.builder()
                    .success(false)
                    .build();
        }
    }

    @Override
    @Transactional
    public ProductivityChartResponse getOverallProductivityChart(Long userId, String period, String startDate, String endDate) {
        try {
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);

            log.info("전체 생산성 차트 조회 - userId: {}, period: {}, startDate: {}, endDate: {}",
                    userId, period, startDate, endDate);

            // 사용자의 모든 프로젝트 조회
            List<Project> userProjects = projectRepository.findByUser_UserId(userId);

            if (userProjects.isEmpty()) {
                log.warn("사용자의 프로젝트가 없음 - userId: {}", userId);
                return createEmptyProductivityChart(period, startDate, endDate);
            }

            // DB에서 사용자의 모든 프로젝트에 대한 기간별 커밋 통계 조회
            Map<LocalDate, Integer> dailyCommitStats = getUserOverallDailyCommitStats(userId, start, end);

            // 모든 프로젝트의 DailyProductivity 데이터 통합
            Map<LocalDate, CombinedDailyData> combinedDataMap = getCombinedDailyProductivityMap(
                    userId, userProjects, start, end, dailyCommitStats);

            List<CombinedDailyData> combinedData = combinedDataMap.values().stream()
                    .sorted(Comparator.comparing(CombinedDailyData::getDate))
                    .collect(Collectors.toList());

            List<ProductivityChartResponse.ChartData> chartData;
            ProductivityChartResponse.Summary summary;

            if ("monthly".equals(period)) {
                chartData = generateOverallMonthlyChartData(start, end, combinedData);
                summary = generateOverallSummary(combinedData, start.getYear() + "년 " + start.getMonthValue() + "월");
            } else if ("weekly".equals(period)) {
                chartData = generateOverallWeeklyChartData(start, end, combinedData);
                summary = generateOverallSummary(combinedData, start.format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일")) +
                        " - " + end.format(DateTimeFormatter.ofPattern("MM월 dd일")));
            } else {
                throw new IllegalArgumentException("지원하지 않는 기간입니다: " + period);
            }

            return ProductivityChartResponse.builder()
                    .success(true)
                    .data(ProductivityChartResponse.Data.builder()
                            .period(period)
                            .chartData(chartData)
                            .summary(summary)
                            .build())
                    .build();

        } catch (Exception e) {
            log.error("전체 생산성 차트 조회 실패", e);
            return ProductivityChartResponse.builder()
                    .success(false)
                    .build();
        }
>>>>>>> Stashed changes
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

<<<<<<< Updated upstream
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
=======
            // DB에서 해당 프로젝트의 특정 날짜 커밋 수 조회
            Integer commitCount = getProjectCommitCountByDate(userId, projectId, targetDate);

            // 실제 커밋 정보를 기반으로 커밋 리스트 생성
            List<ProductivityDetailResponse.Commit> commits = new ArrayList<>();
            if (commitCount > 0) {
                for (int i = 0; i < commitCount; i++) {
                    commits.add(ProductivityDetailResponse.Commit.builder()
                            .hash("commit_" + (i + 1))
                            .message("커밋 #" + (i + 1))
                            .build());
                }
            }

            // 생산성 요소
            Optional<DailyProductivity> dailyOpt = dailyProductivityRepository
                    .findByUserIdAndProjectIdAndDate(userId, projectId, targetDate);

            DailyProductivity daily = dailyOpt.orElse(DailyProductivity.builder()
                    .completedFeatures(completedFeatures.size())
                    .totalCommits(commitCount)
                    .productivityScore(calculateBasicScore(completedFeatures.size(), commitCount))
                    .build());

            ProductivityDetailResponse.ProductivityFactors factors =
                    ProductivityDetailResponse.ProductivityFactors.builder()
                            .completedFeatures(daily.getCompletedFeatures())
                            .productivityScore(daily.getProductivityScore())
                            .build();

            return ProductivityDetailResponse.builder()
                    .success(true)
                    .data(ProductivityDetailResponse.Data.builder()
                            .date(date)
                            .completedFeatures(completedFeatures)
                            .commits(commits)
                            .productivityFactors(factors)
                            .build())
                    .build();

        } catch (Exception e) {
            log.error("생산성 상세 정보 조회 실패", e);
            return ProductivityDetailResponse.builder()
                    .success(false)
                    .build();
        }
    }

    // === DB 기반 커밋 데이터 조회 Helper Methods ===

    //특정 프로젝트의 특정 날짜 커밋 수 조회
    private Integer getProjectCommitCountByDate(Long userId, Long projectId, LocalDate date) {
        try {
            Optional<DailyProductivity> dailyData = dailyProductivityRepository
                    .findByUserIdAndProjectIdAndDate(userId, projectId, date);

            int commitCount = dailyData.map(DailyProductivity::getTotalCommits).orElse(0);

            log.debug("프로젝트 커밋 수 조회 - projectId: {}, date: {}, commits: {}",
                    projectId, date, commitCount);

            return commitCount;
        } catch (Exception e) {
            log.warn("프로젝트 커밋 수 조회 실패 - projectId: {}, date: {}, error: {}",
                    projectId, date, e.getMessage());
            return 0;
        }
    }

    //특정 프로젝트의 기간별 일일 커밋 통계 조회
    private Map<LocalDate, Integer> getProjectDailyCommitStats(Long userId, Long projectId, LocalDate startDate, LocalDate endDate) {
        try {
            List<Object[]> results = dailyProductivityRepository
                    .findCommitsByUserIdAndProjectIdAndDateBetween(userId, projectId, startDate, endDate);

            Map<LocalDate, Integer> dailyStats = new HashMap<>();

            // 기간 내 모든 날짜를 0으로 초기화
            LocalDate current = startDate;
            while (!current.isAfter(endDate)) {
                dailyStats.put(current, 0);
                current = current.plusDays(1);
            }

            // Repository 결과를 Map으로 변환
            for (Object[] result : results) {
                LocalDate date = (LocalDate) result[0];
                Integer commits = ((Number) result[1]).intValue();
                dailyStats.put(date, commits);
            }

            log.debug("프로젝트 일별 커밋 통계 조회 완료 - projectId: {}, 기간: {} ~ {}",
                    projectId, startDate, endDate);

            return dailyStats;
        } catch (Exception e) {
            log.warn("프로젝트 일별 커밋 통계 조회 실패 - projectId: {}, error: {}", projectId, e.getMessage());
            return createEmptyDailyStats(startDate, endDate);
        }
    }

    //사용자의 모든 프로젝트에 대한 기간별 일일 커밋 통계 조회
    private Map<LocalDate, Integer> getUserOverallDailyCommitStats(Long userId, LocalDate startDate, LocalDate endDate) {
        try {
            List<Object[]> results = dailyProductivityRepository
                    .findCommitsByUserIdAndDateBetween(userId, startDate, endDate);

            Map<LocalDate, Integer> dailyStats = new HashMap<>();

            // 기간 내 모든 날짜를 0으로 초기화
            LocalDate current = startDate;
            while (!current.isAfter(endDate)) {
                dailyStats.put(current, 0);
                current = current.plusDays(1);
            }

            // Repository 결과를 Map으로 변환
            for (Object[] result : results) {
                LocalDate date = (LocalDate) result[0];
                Integer commits = ((Number) result[1]).intValue();
                dailyStats.put(date, commits);
            }

            log.debug("사용자 전체 일별 커밋 통계 조회 완료 - userId: {}, 기간: {} ~ {}",
                    userId, startDate, endDate);

            return dailyStats;
        } catch (Exception e) {
            log.warn("사용자 전체 일별 커밋 통계 조회 실패 - userId: {}, error: {}", userId, e.getMessage());
            return createEmptyDailyStats(startDate, endDate);
        }
    }

    private Map<LocalDate, Integer> createEmptyDailyStats(LocalDate startDate, LocalDate endDate) {
        Map<LocalDate, Integer> emptyStats = new HashMap<>();
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            emptyStats.put(current, 0);
            current = current.plusDays(1);
        }
        return emptyStats;
    }

    // === 전체 프로젝트 데이터 처리 Helper Methods ===

    //전체 프로젝트 통합 데이터 클래스
    @Data
    private static class CombinedDailyData {
        private LocalDate date;
        private int totalCompletedFeatures;
        private int totalCommits;
        private double averageProductivityScore;

        public CombinedDailyData(LocalDate date) {
            this.date = date;
            this.totalCompletedFeatures = 0;
            this.totalCommits = 0;
            this.averageProductivityScore = 0.0;
        }
    }

    private Map<LocalDate, CombinedDailyData> getCombinedDailyProductivityMap(
            Long userId, List<Project> userProjects, LocalDate startDate, LocalDate endDate,
            Map<LocalDate, Integer> dailyCommitStats) {

        Map<LocalDate, CombinedDailyData> combinedDataMap = new HashMap<>();

        // 기간 내 모든 날짜 초기화
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            combinedDataMap.put(current, new CombinedDailyData(current));
            current = current.plusDays(1);
        }

        // 각 프로젝트의 데이터를 통합
        for (Project project : userProjects) {
            Map<LocalDate, DailyProductivity> projectData = getOrCreateDailyProductivityMap(
                    userId, project.getProjectId(), startDate, endDate, new HashMap<>());

            for (Map.Entry<LocalDate, DailyProductivity> entry : projectData.entrySet()) {
                LocalDate date = entry.getKey();
                DailyProductivity dailyData = entry.getValue();
                CombinedDailyData combined = combinedDataMap.get(date);

                if (combined != null) {
                    combined.setTotalCompletedFeatures(
                            combined.getTotalCompletedFeatures() + dailyData.getCompletedFeaturesOrZero());
                }
            }
        }

        // 커밋 데이터 적용
        for (Map.Entry<LocalDate, Integer> entry : dailyCommitStats.entrySet()) {
            CombinedDailyData combined = combinedDataMap.get(entry.getKey());
            if (combined != null) {
                combined.setTotalCommits(entry.getValue());
                combined.setAverageProductivityScore(
                        calculateBasicScore(combined.getTotalCompletedFeatures(), combined.getTotalCommits()));
            }
        }

        return combinedDataMap;
    }

    // === 차트 데이터 생성 Methods (전체 프로젝트용) ===

    //전체 프로젝트 월 단위로 생산성 조회
    private List<ProductivityChartResponse.ChartData> generateOverallMonthlyChartData(
            LocalDate start, LocalDate end, List<CombinedDailyData> combinedData) {

        Map<LocalDate, CombinedDailyData> dataMap = combinedData.stream()
                .collect(Collectors.toMap(CombinedDailyData::getDate, d -> d));

        List<ProductivityChartResponse.ChartData> result = new ArrayList<>();
        LocalDate current = start;

        while (!current.isAfter(end)) {
            CombinedDailyData data = dataMap.get(current);

            result.add(ProductivityChartResponse.ChartData.builder()
                    .date(String.valueOf(current.getDayOfMonth()))
                    .completedTasks(data != null ? data.getTotalCompletedFeatures() : 0)
                    .productivityScore(data != null ? data.getAverageProductivityScore() : 0.0)
                    .commits(Math.min((data != null ? data.getTotalCommits() : 0) * 10, 100))
                    .actualCommits(data != null ? data.getTotalCommits() : 0)
                    .build());

            current = current.plusDays(1);
        }

        return result;
    }

    //전체 프로젝트의 주 단위 생산성 조회
    private List<ProductivityChartResponse.ChartData> generateOverallWeeklyChartData(
            LocalDate start, LocalDate end, List<CombinedDailyData> combinedData) {

        Map<LocalDate, CombinedDailyData> dataMap = combinedData.stream()
                .collect(Collectors.toMap(CombinedDailyData::getDate, d -> d));

        List<ProductivityChartResponse.ChartData> result = new ArrayList<>();
        LocalDate current = start;

        while (!current.isAfter(end)) {
            CombinedDailyData data = dataMap.get(current);
            String dayName = current.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.KOREAN);

            result.add(ProductivityChartResponse.ChartData.builder()
                    .date(dayName)
                    .completedTasks(data != null ? data.getTotalCompletedFeatures() : 0)
                    .productivityScore(data != null ? data.getAverageProductivityScore() : 0.0)
                    .commits(Math.min((data != null ? data.getTotalCommits() : 0) * 10, 100))
                    .actualCommits(data != null ? data.getTotalCommits() : 0)
                    .build());

            current = current.plusDays(1);
        }

        return result;
    }

    private ProductivityChartResponse.Summary generateOverallSummary(List<CombinedDailyData> combinedData, String period) {
        if (combinedData.isEmpty()) {
            return ProductivityChartResponse.Summary.builder()
                    .averageTasksPerDay(0.0)
                    .averageProductivityScore(0.0)
                    .trend(period)
                    .trendPercentage(0.0)
                    .totalCommits(0)
                    .averageCommits(0.0)
                    .maxCommits(0)
                    .build();
        }

        double avgTasks = combinedData.stream().mapToInt(CombinedDailyData::getTotalCompletedFeatures).average().orElse(0.0);
        double avgScore = combinedData.stream().mapToDouble(CombinedDailyData::getAverageProductivityScore).average().orElse(0.0);

        int totalCommits = combinedData.stream().mapToInt(CombinedDailyData::getTotalCommits).sum();
        double avgCommits = combinedData.stream().mapToInt(CombinedDailyData::getTotalCommits).average().orElse(0.0);
        int maxCommits = combinedData.stream().mapToInt(CombinedDailyData::getTotalCommits).max().orElse(0);

        return ProductivityChartResponse.Summary.builder()
                .averageTasksPerDay(Math.round(avgTasks * 100.0) / 100.0)
                .averageProductivityScore(Math.round(avgScore * 100.0) / 100.0)
                .trend(period)
                .trendPercentage(0.0)
                .totalCommits(totalCommits)
                .averageCommits(Math.round(avgCommits * 100.0) / 100.0)
                .maxCommits(maxCommits)
                .build();
    }

    private ProductivityChartResponse createEmptyProductivityChart(String period, String startDate, String endDate) {
        return ProductivityChartResponse.builder()
                .success(true)
                .data(ProductivityChartResponse.Data.builder()
                        .period(period)
                        .chartData(new ArrayList<>())
                        .summary(ProductivityChartResponse.Summary.builder()
                                .averageTasksPerDay(0.0)
                                .averageProductivityScore(0.0)
                                .trend(period)
                                .trendPercentage(0.0)
                                .totalCommits(0)
                                .averageCommits(0.0)
                                .maxCommits(0)
                                .build())
                        .build())
                .build();
    }

    private Map<LocalDate, DailyProductivity> getOrCreateDailyProductivityMap(
            Long userId, Long projectId, LocalDate startDate, LocalDate endDate,
            Map<LocalDate, Integer> dailyCommitStats) {

        // 기존 데이터 조회
        List<DailyProductivity> existingData = dailyProductivityRepository
                .findByUserIdAndProjectIdAndDateBetween(userId, projectId, startDate, endDate);

        Map<LocalDate, DailyProductivity> dataMap = existingData.stream()
                .collect(Collectors.toMap(DailyProductivity::getDate, d -> d));

        // 누락된 날짜의 데이터 생성
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            if (!dataMap.containsKey(current)) {
                // 해당 날짜의 완료된 기능 수 조회
                List<FeatureItem> completedFeatures = featureItemRepository
                        .findCompletedFeaturesByProjectAndDate(projectId, current);

                Integer commits = dailyCommitStats.getOrDefault(current,
                        getProjectCommitCountByDate(userId, projectId, current));
                double score = calculateBasicScore(completedFeatures.size(), commits);

                DailyProductivity newData = DailyProductivity.builder()
                        .userId(userId)
                        .projectId(projectId)
                        .date(current)
                        .completedFeatures(completedFeatures.size())
                        .totalCommits(commits)
                        .productivityScore(score)
                        .build();

                dataMap.put(current, dailyProductivityRepository.save(newData));
            }
            current = current.plusDays(1);
        }

        return dataMap;
    }

    //프로젝트의 월 단위로 생산성 조회
    private List<ProductivityChartResponse.ChartData> generateMonthlyChartData(
            LocalDate start, LocalDate end, List<DailyProductivity> dailyData) {

        Map<LocalDate, DailyProductivity> dataMap = dailyData.stream()
                .collect(Collectors.toMap(DailyProductivity::getDate, d -> d));

        List<ProductivityChartResponse.ChartData> result = new ArrayList<>();
        LocalDate current = start;

        while (!current.isAfter(end)) {
            DailyProductivity data = dataMap.get(current);
            int commits = data != null ? data.getTotalCommits() : 0;
            double score = data != null ? data.getProductivityScore() : 0.0;

            result.add(ProductivityChartResponse.ChartData.builder()
                    .date(String.valueOf(current.getDayOfMonth()))
                    .completedTasks(data != null ? data.getCompletedFeatures() : 0)
                    .productivityScore(score)
                    .commits(Math.min(commits * 10, 100))
                    .actualCommits(commits)
                    .build());

            current = current.plusDays(1);
        }

        return result;
    }

    //프로젝트의 주 단위로 생산성 조회
    private List<ProductivityChartResponse.ChartData> generateWeeklyChartData(
            LocalDate start, LocalDate end, List<DailyProductivity> dailyData) {

        Map<LocalDate, DailyProductivity> dataMap = dailyData.stream()
                .collect(Collectors.toMap(DailyProductivity::getDate, d -> d));

        List<ProductivityChartResponse.ChartData> result = new ArrayList<>();
        LocalDate current = start;

        while (!current.isAfter(end)) {
            DailyProductivity data = dataMap.get(current);
            int commits = data != null ? data.getTotalCommits() : 0;
            double score = data != null ? data.getProductivityScore() : 0.0;

            String dayName = current.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.KOREAN);

            result.add(ProductivityChartResponse.ChartData.builder()
                    .date(dayName)
                    .completedTasks(data != null ? data.getCompletedFeatures() : 0)
                    .productivityScore(score)
                    .commits(Math.min(commits * 10, 100))
                    .actualCommits(commits)
                    .build());

            current = current.plusDays(1);
        }

        return result;
    }

    private ProductivityChartResponse.Summary generateSummary(List<DailyProductivity> dailyData, String period) {
        if (dailyData.isEmpty()) {
            return ProductivityChartResponse.Summary.builder()
                    .averageTasksPerDay(0.0)
                    .averageProductivityScore(0.0)
                    .trend(period)
                    .trendPercentage(0.0)
                    .totalCommits(0)
                    .averageCommits(0.0)
                    .maxCommits(0)
                    .build();
        }

        double avgTasks = dailyData.stream().mapToInt(DailyProductivity::getCompletedFeatures).average().orElse(0.0);
        double avgScore = dailyData.stream().mapToDouble(DailyProductivity::getProductivityScore).average().orElse(0.0);

        int totalCommits = dailyData.stream().mapToInt(DailyProductivity::getTotalCommits).sum();
        double avgCommits = dailyData.stream().mapToInt(DailyProductivity::getTotalCommits).average().orElse(0.0);
        int maxCommits = dailyData.stream().mapToInt(DailyProductivity::getTotalCommits).max().orElse(0);

        return ProductivityChartResponse.Summary.builder()
                .averageTasksPerDay(Math.round(avgTasks * 100.0) / 100.0)
                .averageProductivityScore(Math.round(avgScore * 100.0) / 100.0)
                .trend(period)
                .trendPercentage(0.0)
                .totalCommits(totalCommits)
                .averageCommits(Math.round(avgCommits * 100.0) / 100.0)
                .maxCommits(maxCommits)
                .build();
    }

    // === 점수 계산 메서드들 ===

    private double calculateTaskScore(int completedFeatures) {
        // 완료된 기능 수에 따른 점수 (0-100)
        return Math.min(completedFeatures * 25.0, 100.0);
>>>>>>> Stashed changes
    }

    /**
     * 커밋 활동 점수 계산
     * @param commits 커밋 수
     * @return 커밋 점수 (최대 100점)
     */
    private double calculateCommitScore(int commits) {
<<<<<<< Updated upstream
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
=======
        // 커밋 수에 따른 점수 (0-100)
        return Math.min(commits * 15.0, 100.0);
    }

    private double calculateBasicScore(int completedFeatures, int commits) {
        // 작업완료:커밋 = 7:3 비율
        return (calculateTaskScore(completedFeatures) * 0.7) + (calculateCommitScore(commits) * 0.3);
>>>>>>> Stashed changes
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
<<<<<<< Updated upstream
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
=======
    public void saveDailyProductivity(Long userId, Long projectId, LocalDate date,
                                      int completedFeatures, int totalCommits, double overallScore) {
>>>>>>> Stashed changes
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
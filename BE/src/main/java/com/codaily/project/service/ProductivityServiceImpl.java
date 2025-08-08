package com.codaily.project.service;

import com.codaily.project.dto.ProductivityCalculateRequest;
import com.codaily.project.dto.ProductivityCalculateResponse;
import com.codaily.project.dto.ProductivityChartResponse;
import com.codaily.project.dto.ProductivityDetailResponse;
import com.codaily.project.entity.DailyProductivity;
import com.codaily.project.entity.FeatureItem;
import com.codaily.project.repository.DailyProductivityRepository;
import com.codaily.project.repository.FeatureItemRepository;
import com.codaily.codereview.repository.CodeCommitRepository;
import com.codaily.codereview.repository.CodeReviewRepository; // 추가
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductivityServiceImpl implements ProductivityService {

    private final FeatureItemRepository featureItemRepository;
    private final DailyProductivityRepository dailyProductivityRepository;
    private final CodeCommitRepository codeCommitRepository;
    private final CodeReviewRepository codeReviewRepository; // 추가

    @Override
    @Transactional
    public ProductivityCalculateResponse calculateProductivity(ProductivityCalculateRequest request) {
        Long userId = Long.valueOf(request.getUserId());
        Long projectId = Long.valueOf(request.getProjectId());
        LocalDate targetDate = LocalDate.parse(request.getPeriod().getDate());

        Map<String, ProductivityCalculateResponse.MetricScore> breakdown = new HashMap<>();
        double overallScore = 0.0;

        // 해당 날짜의 완료된 기능들 조회
        List<FeatureItem> completedFeatures = featureItemRepository.findCompletedFeaturesByProjectAndDate(projectId, targetDate);

        // DB에서 커밋 수 조회
        Integer totalCommits = getCommitCountFromDB(projectId, targetDate);

        // 코드품질 점수 조회
        Double codeQualityScore = getCodeQualityScoreFromDB(projectId, targetDate);

        // 기능 완료율 계산 (가중치 50%)
        if (request.getMetrics().isIncludeTaskCompletion()) {
            double taskScore = calculateTaskScore(completedFeatures.size());
            breakdown.put("taskCompletion", ProductivityCalculateResponse.MetricScore.builder()
                    .score(taskScore).weight(0.5).build());
            overallScore += taskScore * 0.5;
        }

        // 커밋 활동 계산 (가중치 30%)
        if (request.getMetrics().isIncludeCommits()) {
            double commitScore = calculateCommitScore(totalCommits);
            breakdown.put("commitFrequency", ProductivityCalculateResponse.MetricScore.builder()
                    .score(commitScore).weight(0.3).build());
            overallScore += commitScore * 0.3;
        }

        // 코드품질 계산 (가중치 20%)
        if (request.getMetrics().isIncludeCodeQuality()) {
            double qualityScore = codeQualityScore != null ? codeQualityScore : 0.0;
            breakdown.put("codeQuality", ProductivityCalculateResponse.MetricScore.builder()
                    .score(qualityScore).weight(0.2).build());
            overallScore += qualityScore * 0.2;
        }

        // DailyProductivity에 저장
        saveDailyProductivity(userId, projectId, targetDate, completedFeatures.size(), totalCommits, codeQualityScore, overallScore);

        return ProductivityCalculateResponse.builder()
                .overallScore(overallScore)
                .breakdown(breakdown)
                .trend("stable")
                .benchmarkComparison(ProductivityCalculateResponse.BenchmarkComparison.builder()
                        .personalAverage(0.0)
                        .projectAverage(0.0)
                        .build())
                .build();
    }

    @Override
    @Transactional
    public ProductivityChartResponse getProjectProductivityChart(Long userId, Long projectId, String period, String startDate, String endDate) {
        try {
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);

            log.info("단일 프로젝트 생산성 차트 조회 - projectId: {}, period: {}, startDate: {}, endDate: {}",
                    projectId, period, startDate, endDate);

            // DB에서 기간별 커밋 통계 조회
            Map<LocalDate, Integer> dailyCommitStats = getDailyCommitStatsFromDB(projectId, start, end);

            // 기간별 DailyProductivity 데이터 조회 및 생성
            Map<LocalDate, DailyProductivity> dailyDataMap = getOrCreateDailyProductivityMap(
                    userId, projectId, start, end, dailyCommitStats);

            List<DailyProductivity> dailyData = dailyDataMap.values().stream()
                    .sorted(Comparator.comparing(DailyProductivity::getDate))
                    .collect(Collectors.toList());

            List<ProductivityChartResponse.ChartData> chartData;
            ProductivityChartResponse.Summary summary;

            if ("monthly".equals(period)) {
                chartData = generateMonthlyChartData(start, end, dailyData);
                summary = generateSummary(dailyData, start.getYear() + "년 " + start.getMonthValue() + "월");
            } else if ("weekly".equals(period)) {
                chartData = generateWeeklyChartData(start, end, dailyData);
                summary = generateSummary(dailyData, start.format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일")) +
                        " - " + end.format(DateTimeFormatter.ofPattern("MM월 dd일")));
            } else {
                throw new IllegalArgumentException("지원하지 않는 기간입니다: " + period);
            }

            return ProductivityChartResponse.builder()
                    .success(true)
                    .data(ProductivityChartResponse.Data.builder()
                            .period(summary.getTrend())
                            .chartData(chartData)
                            .summary(summary)
                            .build())
                    .build();

        } catch (Exception e) {
            log.error("단일 프로젝트 생산성 차트 조회 실패", e);
            return ProductivityChartResponse.builder()
                    .success(false)
                    .build();
        }
    }

    @Override
    @Transactional
    public ProductivityChartResponse getAllProjectsProductivityChart(Long userId, String period, String startDate, String endDate) {
        try {
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);

            log.info("전체 프로젝트 생산성 차트 조회 - userId: {}, period: {}, startDate: {}, endDate: {}",
                    userId, period, startDate, endDate);

            // 사용자의 모든 프로젝트에 대한 커밋 통계 조회
            Map<LocalDate, Integer> allProjectsCommitStats = getAllProjectsCommitStatsFromDB(userId, start, end);

            // 전체 프로젝트의 완료 기능 통계
            Map<LocalDate, Integer> allProjectsTaskStats = getAllProjectsTaskStatsFromDB(userId, start, end);

            // 전체 프로젝트의 코드품질 통계
            Map<LocalDate, Double> allProjectsQualityStats = getAllProjectsQualityStatsFromDB(userId, start, end);

            List<ProductivityChartResponse.ChartData> chartData;
            ProductivityChartResponse.Summary summary;

            if ("monthly".equals(period)) {
                chartData = generateAllProjectsMonthlyChartData(start, end, allProjectsCommitStats, allProjectsTaskStats, allProjectsQualityStats);
                summary = generateAllProjectsSummary(allProjectsCommitStats, allProjectsTaskStats, allProjectsQualityStats,
                        start.getYear() + "년 " + start.getMonthValue() + "월");
            } else if ("weekly".equals(period)) {
                chartData = generateAllProjectsWeeklyChartData(start, end, allProjectsCommitStats, allProjectsTaskStats, allProjectsQualityStats);
                summary = generateAllProjectsSummary(allProjectsCommitStats, allProjectsTaskStats, allProjectsQualityStats,
                        start.format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일")) +
                                " - " + end.format(DateTimeFormatter.ofPattern("MM월 dd일")));
            } else {
                throw new IllegalArgumentException("지원하지 않는 기간입니다: " + period);
            }

            return ProductivityChartResponse.builder()
                    .success(true)
                    .data(ProductivityChartResponse.Data.builder()
                            .period(summary.getTrend())
                            .chartData(chartData)
                            .summary(summary)
                            .build())
                    .build();

        } catch (Exception e) {
            log.error("전체 프로젝트 생산성 차트 조회 실패", e);
            return ProductivityChartResponse.builder()
                    .success(false)
                    .build();
        }
    }

    @Override
    public ProductivityDetailResponse getProductivityDetail(Long userId, Long projectId, String date) {
        try {
            LocalDate targetDate = LocalDate.parse(date);

            // 해당 날짜의 완료된 기능들
            List<FeatureItem> completedFeatures = featureItemRepository.findCompletedFeaturesByProjectAndDate(projectId, targetDate);

            // DB에서 커밋 정보 조회
            Integer commitCount = getCommitCountFromDB(projectId, targetDate);

            // 코드품질 점수 조회
            Double codeQualityScore = getCodeQualityScoreFromDB(projectId, targetDate);

            // 커밋 리스트 생성
            List<ProductivityDetailResponse.Commit> commits = getCommitDetailsFromDB(projectId, targetDate);

            // 생산성 요소
            Optional<DailyProductivity> dailyOpt = dailyProductivityRepository
                    .findByUserIdAndProjectIdAndDate(userId, projectId, targetDate);

            DailyProductivity daily = dailyOpt.orElse(DailyProductivity.builder()
                    .completedFeatures(completedFeatures.size())
                    .totalCommits(commitCount)
                    .codeQuality(codeQualityScore != null ? codeQualityScore : 0.0)
                    .productivityScore(calculateBasicScore(completedFeatures.size(), commitCount, codeQualityScore))
                    .build());

            ProductivityDetailResponse.ProductivityFactors factors =
                    ProductivityDetailResponse.ProductivityFactors.builder()
                            .completedFeatures(daily.getCompletedFeatures())
                            .productivityScore(daily.getProductivityScore())
                            .codeQuality(daily.getCodeQuality()) // 코드품질 포함
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

    // === 코드품질 관련 새로운 Helper Methods ===

    private Double getCodeQualityScoreFromDB(Long projectId, LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(23, 59, 59);

        return codeReviewRepository.findAverageQualityScoreByProjectAndPeriod(projectId, startOfDay, endOfDay);
    }

    private Map<LocalDate, Double> getAllProjectsQualityStatsFromDB(Long userId, LocalDate startDate, LocalDate endDate) {
        Map<LocalDate, Double> stats = new HashMap<>();

        // 기간 내 모든 날짜를 0으로 초기화
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            stats.put(current, 0.0);
            current = current.plusDays(1);
        }

        // 사용자의 모든 프로젝트에서 해당 기간의 코드품질 점수들을 평균화
        current = startDate;
        while (!current.isAfter(endDate)) {
            LocalDateTime startOfDay = current.atStartOfDay();
            LocalDateTime endOfDay = current.atTime(23, 59, 59);


            // 해당 날짜의 사용자의 모든 프로젝트 코드리뷰 조회
            var reviews = codeReviewRepository.findByProject_User_UserIdAndCreatedAtBetween(userId, startOfDay, endOfDay);
            if (!reviews.isEmpty()) {
                double avgQuality = reviews.stream()
                        .filter(review -> review.getProject().getUser().getUserId().equals(userId))
                        .mapToDouble(review -> review.getQualityScore() != null ? review.getQualityScore() : 0.0)
                        .average()
                        .orElse(0.0);
                stats.put(current, avgQuality);
            }

            current = current.plusDays(1);
        }

        return stats;
    }

    // === 기존 Helper Methods (수정됨) ===

    private Integer getCommitCountFromDB(Long projectId, LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(23, 59, 59);

        Long count = codeCommitRepository.countByProject_ProjectIdAndCommittedAtBetween(
                projectId, startOfDay, endOfDay);

        return count != null ? count.intValue() : 0;
    }

    private Map<LocalDate, Integer> getDailyCommitStatsFromDB(Long projectId, LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59);

        var commits = codeCommitRepository.findByProject_ProjectIdAndCommittedAtBetween(projectId, start, end);

        Map<LocalDate, Integer> stats = new HashMap<>();

        // 기간 내 모든 날짜를 0으로 초기화
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            stats.put(current, 0);
            current = current.plusDays(1);
        }

        // 커밋들을 날짜별로 그룹핑
        commits.forEach(commit -> {
            LocalDate commitDate = commit.getCommittedAt().toLocalDate();
            stats.put(commitDate, stats.getOrDefault(commitDate, 0) + 1);
        });

        return stats;
    }

    private Map<LocalDate, Integer> getAllProjectsCommitStatsFromDB(Long userId, LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59);

        var commits = codeCommitRepository.findByUser_UserIdAndCommittedAtBetween(userId, start, end);

        Map<LocalDate, Integer> stats = new HashMap<>();

        // 기간 내 모든 날짜를 0으로 초기화
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            stats.put(current, 0);
            current = current.plusDays(1);
        }

        // 커밋들을 날짜별로 그룹핑
        commits.forEach(commit -> {
            LocalDate commitDate = commit.getCommittedAt().toLocalDate();
            stats.put(commitDate, stats.getOrDefault(commitDate, 0) + 1);
        });

        return stats;
    }

    private Map<LocalDate, Integer> getAllProjectsTaskStatsFromDB(Long userId, LocalDate startDate, LocalDate endDate) {
        Map<LocalDate, Integer> stats = new HashMap<>();

        // 기간 내 모든 날짜를 0으로 초기화
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            stats.put(current, 0);
            current = current.plusDays(1);
        }

        // 사용자의 모든 프로젝트에서 완료된 기능들 조회
        current = startDate;
        while (!current.isAfter(endDate)) {
            List<FeatureItem> completedFeatures = featureItemRepository.findByUserIdAndDateRange(userId, current, current);
            int completedCount = (int) completedFeatures.stream()
                    .filter(f -> "DONE".equals(f.getStatus()))
                    .count();
            stats.put(current, completedCount);
            current = current.plusDays(1);
        }

        return stats;
    }

    private List<ProductivityDetailResponse.Commit> getCommitDetailsFromDB(Long projectId, LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(23, 59, 59);

        var commits = codeCommitRepository.findByProject_ProjectIdAndCommittedAtBetween(projectId, startOfDay, endOfDay);

        return commits.stream()
                .map(commit -> ProductivityDetailResponse.Commit.builder()
                        .hash(commit.getCommitHash())
                        .message(commit.getMessage())
                        .build())
                .collect(Collectors.toList());
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

                Integer commits = dailyCommitStats.getOrDefault(current, 0);
                Double codeQuality = getCodeQualityScoreFromDB(projectId, current);
                double score = calculateBasicScore(completedFeatures.size(), commits, codeQuality);

                DailyProductivity newData = DailyProductivity.builder()
                        .userId(userId)
                        .projectId(projectId)
                        .date(current)
                        .completedFeatures(completedFeatures.size())
                        .totalCommits(commits)
                        .codeQuality(codeQuality != null ? codeQuality : 0.0)
                        .productivityScore(score)
                        .build();

                dataMap.put(current, dailyProductivityRepository.save(newData));
            }
            current = current.plusDays(1);
        }

        return dataMap;
    }

    // === Chart Data Generation Methods (수정됨) ===

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

    private List<ProductivityChartResponse.ChartData> generateAllProjectsMonthlyChartData(
            LocalDate start, LocalDate end, Map<LocalDate, Integer> commitStats,
            Map<LocalDate, Integer> taskStats, Map<LocalDate, Double> qualityStats) {

        List<ProductivityChartResponse.ChartData> result = new ArrayList<>();
        LocalDate current = start;

        while (!current.isAfter(end)) {
            int commits = commitStats.getOrDefault(current, 0);
            int tasks = taskStats.getOrDefault(current, 0);
            double quality = qualityStats.getOrDefault(current, 0.0);
            double score = calculateBasicScore(tasks, commits, quality);

            result.add(ProductivityChartResponse.ChartData.builder()
                    .date(String.valueOf(current.getDayOfMonth()))
                    .completedTasks(tasks)
                    .productivityScore(score)
                    .commits(Math.min(commits * 10, 100))
                    .actualCommits(commits)
                    .build());

            current = current.plusDays(1);
        }

        return result;
    }

    private List<ProductivityChartResponse.ChartData> generateAllProjectsWeeklyChartData(
            LocalDate start, LocalDate end, Map<LocalDate, Integer> commitStats,
            Map<LocalDate, Integer> taskStats, Map<LocalDate, Double> qualityStats) {

        List<ProductivityChartResponse.ChartData> result = new ArrayList<>();
        LocalDate current = start;

        while (!current.isAfter(end)) {
            int commits = commitStats.getOrDefault(current, 0);
            int tasks = taskStats.getOrDefault(current, 0);
            double quality = qualityStats.getOrDefault(current, 0.0);
            double score = calculateBasicScore(tasks, commits, quality);

            String dayName = current.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.KOREAN);

            result.add(ProductivityChartResponse.ChartData.builder()
                    .date(dayName)
                    .completedTasks(tasks)
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

    private ProductivityChartResponse.Summary generateAllProjectsSummary(
            Map<LocalDate, Integer> commitStats, Map<LocalDate, Integer> taskStats,
            Map<LocalDate, Double> qualityStats, String period) {

        int totalCommits = commitStats.values().stream().mapToInt(Integer::intValue).sum();
        int totalTasks = taskStats.values().stream().mapToInt(Integer::intValue).sum();

        double avgCommits = commitStats.values().stream().mapToInt(Integer::intValue).average().orElse(0.0);
        double avgTasks = taskStats.values().stream().mapToInt(Integer::intValue).average().orElse(0.0);
        double avgQuality = qualityStats.values().stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        int maxCommits = commitStats.values().stream().mapToInt(Integer::intValue).max().orElse(0);

        double avgScore = calculateBasicScore((int)avgTasks, (int)avgCommits, avgQuality);

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

    // === Score Calculation Methods (수정됨) ===

    private double calculateTaskScore(int completedFeatures) {
        return Math.min(completedFeatures * 25.0, 100.0);
    }

    private double calculateCommitScore(int commits) {
        return Math.min(commits * 15.0, 100.0);
    }

    private double calculateBasicScore(int completedFeatures, int commits, Double codeQuality) {
        double taskScore = calculateTaskScore(completedFeatures) * 0.5;
        double commitScore = calculateCommitScore(commits) * 0.3;
        double qualityScore = (codeQuality != null ? codeQuality : 0.0) * 0.2;

        return taskScore + commitScore + qualityScore;
    }

    @Transactional
    public void saveDailyProductivity(Long userId, Long projectId, LocalDate date,
                                      int completedFeatures, int totalCommits, Double codeQuality, double overallScore) {
        DailyProductivity daily = dailyProductivityRepository
                .findByUserIdAndProjectIdAndDate(userId, projectId, date)
                .orElse(DailyProductivity.builder()
                        .userId(userId)
                        .projectId(projectId)
                        .date(date)
                        .build());

        daily.setCompletedFeatures(completedFeatures);
        daily.setTotalCommits(totalCommits);
        daily.setCodeQuality(codeQuality != null ? codeQuality : 0.0);
        daily.setProductivityScore(overallScore);

        dailyProductivityRepository.save(daily);
    }
}
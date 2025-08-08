package com.codaily.project.service;

import com.codaily.auth.service.UserService;
import com.codaily.common.git.service.GithubService;
import com.codaily.project.dto.ProductivityCalculateRequest;
import com.codaily.project.dto.ProductivityCalculateResponse;
import com.codaily.project.dto.ProductivityChartResponse;
import com.codaily.project.dto.ProductivityDetailResponse;
import com.codaily.project.entity.DailyProductivity;
import com.codaily.project.entity.FeatureItem;
import com.codaily.project.repository.DailyProductivityRepository;
import com.codaily.project.repository.FeatureItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
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
    private final GithubService githubService;
    private final UserService userService;

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

        // GitHub API로 실제 커밋 수 조회
        Integer totalCommits = getRealCommitCount(userId, targetDate);

        // 기능 완료율 계산 (가중치 100% - 코드품질 제외)
        if (request.getMetrics().isIncludeTaskCompletion()) {
            double taskScore = calculateTaskScore(completedFeatures.size());
            breakdown.put("taskCompletion", ProductivityCalculateResponse.MetricScore.builder()
                    .score(taskScore).weight(0.7).build());
            overallScore += taskScore * 0.7;
        }

        // 커밋 활동 계산 (가중치 30%)
        if (request.getMetrics().isIncludeCommits()) {
            double commitScore = calculateCommitScore(totalCommits);
            breakdown.put("commitFrequency", ProductivityCalculateResponse.MetricScore.builder()
                    .score(commitScore).weight(0.3).build());
            overallScore += commitScore * 0.3;
        }

        // DailyProductivity에 저장 (코드품질 제외)
        saveDailyProductivity(userId, projectId, targetDate, completedFeatures.size(), totalCommits, overallScore);

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
    public ProductivityChartResponse getProductivityChart(Long userId, Long projectId, String period, String startDate, String endDate) {
        try {
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);

            log.info("생산성 차트 조회 - projectId: {}, period: {}, startDate: {}, endDate: {}",
                    projectId, period, startDate, endDate);

            // GitHub API로 기간별 커밋 통계 조회
            Map<LocalDate, Integer> dailyCommitStats = getRealDailyCommitStats(userId, start, end);

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
            log.error("생산성 차트 조회 실패", e);
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

            // GitHub API로 실제 커밋 정보 조회
            Integer realCommitCount = getRealCommitCount(userId, targetDate);

            // 실제 커밋 정보를 기반으로 커밋 리스트 생성
            List<ProductivityDetailResponse.Commit> commits = new ArrayList<>();
            if (realCommitCount > 0) {
                // 실제로는 GitHub API에서 커밋 상세 정보를 가져와야 하지만
                // 현재는 커밋 수만 표시
                for (int i = 0; i < realCommitCount; i++) {
                    commits.add(ProductivityDetailResponse.Commit.builder()
                            .hash("commit_" + (i + 1))
                            .message("GitHub 커밋 #" + (i + 1))
                            .build());
                }
            }

            // 생산성 요소 (코드품질 제외)
            Optional<DailyProductivity> dailyOpt = dailyProductivityRepository
                    .findByUserIdAndProjectIdAndDate(userId, projectId, targetDate);

            DailyProductivity daily = dailyOpt.orElse(DailyProductivity.builder()
                    .completedFeatures(completedFeatures.size())
                    .totalCommits(realCommitCount)
                    .productivityScore(calculateBasicScore(completedFeatures.size(), realCommitCount))
                    .build());

            ProductivityDetailResponse.ProductivityFactors factors =
                    ProductivityDetailResponse.ProductivityFactors.builder()
                            .completedFeatures(daily.getCompletedFeatures())
                            .productivityScore(daily.getProductivityScore())
                            // codeQuality 제거
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

    // === GitHub API 연동 Helper Methods ===

    private Integer getRealCommitCount(Long userId, LocalDate date) {
        try {
            String accessToken = userService.getGithubAccessToken(userId);
            String username = userService.getGithubUsername(userId);

            if (accessToken == null || username == null) {
                log.warn("GitHub 토큰 또는 사용자명 없음 - userId: {}", userId);
                return 0;
            }

            Integer commitCount = githubService.getCommitsByDate(accessToken, username, date)
                    .block(); // 동기 처리

            log.debug("GitHub 커밋 수 조회 성공 - userId: {}, date: {}, commits: {}",
                    userId, date, commitCount);

            return commitCount != null ? commitCount : 0;

        } catch (Exception e) {
            log.warn("GitHub 커밋 수 조회 실패 - userId: {}, date: {}, error: {}",
                    userId, date, e.getMessage());
            return 0;
        }
    }

    private Map<LocalDate, Integer> getRealDailyCommitStats(Long userId, LocalDate startDate, LocalDate endDate) {
        try {
            String accessToken = userService.getGithubAccessToken(userId);
            String username = userService.getGithubUsername(userId);

            if (accessToken == null || username == null) {
                log.warn("GitHub 토큰 또는 사용자명 없음 - userId: {}", userId);
                return createEmptyDailyStats(startDate, endDate);
            }

            Map<LocalDate, Integer> stats = githubService.getDailyCommitStats(accessToken, username, startDate, endDate)
                    .block(); // 동기 처리

            log.debug("GitHub 일별 커밋 통계 조회 성공 - userId: {}, 기간: {} ~ {}",
                    userId, startDate, endDate);

            return stats != null ? stats : createEmptyDailyStats(startDate, endDate);

        } catch (Exception e) {
            log.warn("GitHub 일별 커밋 통계 조회 실패 - userId: {}, error: {}", userId, e.getMessage());
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
                double score = calculateBasicScore(completedFeatures.size(), commits);

                DailyProductivity newData = DailyProductivity.builder()
                        .userId(userId)
                        .projectId(projectId)
                        .date(current)
                        .completedFeatures(completedFeatures.size())
                        .totalCommits(commits)
                        .productivityScore(score)
                        // codeQuality 제거 또는 기본값 설정 안함
                        .build();

                dataMap.put(current, dailyProductivityRepository.save(newData));
            }
            current = current.plusDays(1);
        }

        return dataMap;
    }

    // === 기존 Helper Methods (수정됨) ===

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
                    .commits(Math.min(commits * 10, 100)) // 커밋 수를 0-100으로 정규화 (10배 계수)
                    .actualCommits(commits) // 실제 커밋 수
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
                    .commits(Math.min(commits * 10, 100)) // 커밋 수를 0-100으로 정규화 (10배 계수)
                    .actualCommits(commits) // 실제 커밋 수
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

    // 점수 계산 메서드들 (가중치 조정)
    private double calculateTaskScore(int completedFeatures) {
        // 완료된 기능 수에 따른 점수 (0-100)
        return Math.min(completedFeatures * 25.0, 100.0);
    }

    private double calculateCommitScore(int commits) {
        // 커밋 수에 따른 점수 (0-100)
        return Math.min(commits * 15.0, 100.0); // 커밋 당 15점으로 조정
    }

    private double calculateBasicScore(int completedFeatures, int commits) {
        // 코드품질 제외, 작업완료:커밋 = 7:3 비율
        return (calculateTaskScore(completedFeatures) * 0.7) + (calculateCommitScore(commits) * 0.3);
    }

    @Transactional
    public void saveDailyProductivity(Long userId, Long projectId, LocalDate date,
                                       int completedFeatures, int totalCommits, double overallScore) {
        DailyProductivity daily = dailyProductivityRepository
                .findByUserIdAndProjectIdAndDate(userId, projectId, date)
                .orElse(DailyProductivity.builder()
                        .userId(userId)
                        .projectId(projectId)
                        .date(date)
                        .build());

        daily.setCompletedFeatures(completedFeatures);
        daily.setTotalCommits(totalCommits);
        daily.setProductivityScore(overallScore);
        // codeQuality 필드 제거 또는 설정하지 않음

        dailyProductivityRepository.save(daily);
    }
}
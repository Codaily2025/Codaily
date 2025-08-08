package com.codaily.retrospective.service;

import com.codaily.codereview.service.CodeCommitService;
import com.codaily.project.entity.Project;
import com.codaily.project.repository.FeatureItemRepository;
import com.codaily.retrospective.dto.RetrospectiveGenerateRequest;
import com.codaily.retrospective.entity.Retrospective;
import com.codaily.retrospective.repository.RetrospectiveRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Log4j2
@Service
@RequiredArgsConstructor
public class RetrospectiveServiceImpl implements RetrospectiveService {

    private final RetrospectiveRepository retrospectiveRepository;
    private final CodeCommitService codeCommitService;
    private final FeatureItemRepository featureItemRepository;

    @Override
    @Transactional
    public void saveRetrospective(Project project, String content) {
        // 혹시 병렬 중복 방지
        if (retrospectiveRepository.existsByProjectAndDate(project, LocalDate.now())) {
            log.warn("저장 직전에 중복 확인됨. 저장 중단 - projectId: {}", project.getProjectId());
            return;
        }

        Retrospective retrospective = Retrospective.builder()
                .project(project)
                .date(LocalDate.now())
                .content(content)
                .build();

        retrospectiveRepository.save(retrospective);
        log.info("회고 저장 완료 - projectId: {}", project.getProjectId());
    }

    @Override
    @Transactional
    public boolean existsByProjectAndDate(Project project, LocalDate date) {
        return retrospectiveRepository.existsByProjectAndDate(project, date);
    }

    @Override
    @Transactional
    public RetrospectiveGenerateRequest collectRetrospectiveData(Project project, Long userId) {
        Long projectId = project.getProjectId();
        String today = LocalDate.now().toString();

        /*// 1. 커밋 정보
        List<RetrospectiveGenerateRequest.CommitSummary> commits = commitService.findTodayCommits(projectId).stream()
                .map(commit -> RetrospectiveGenerateRequest.CommitSummary.builder()
                        .message(commit.getMessage())
                        .linesChanged(commit.getChangedLines())
                        .fileCount(commit.getFileCount())
                        .addedLines(commit.getAddedLines())
                        .commitTime(commit.getCommitTime().toString())
                        .build())
                .toList();

        // 해당 날짜의 완료된 기능들 조회
        List<FeatureItem> completedFeatures = featureItemRepository.findCompletedFeaturesByProjectAndDate(projectId, targetDate);

        // 3. 작업 시간 분포
        Map<String, Double> timeDistribution = workTimeService.getWorkTimeByCategory(projectId, today);

        // 4. 코드 품질 점수
        Double codeQualityScore = codeReviewService.getQualityScore(projectId).getScore();

        // 5. 목표 달성률
        Double goalAchievementRate = projectProgressService.getProjectProgress(projectId).getCompletionRate();

        // 6. 생산성 지표
        var productivity = productivityService.calculateProductivity(
                ProductivityCalculateRequest.builder().projectId(projectId).userId(userId).date(today).build()
        );

        // 7. 생산성 피크
        List<RetrospectiveGenerateRequest.ProductivityPeak> peaks = productivityService.getProductivityChart(
                userId, projectId, "day", today, today
        ).getPeaks();

        // 8. 작업 지연 원인
        List<String> delayReasons = delayAnalysisService.getDelayReasons(projectId, today);

        // 9. 품질 추이
        List<RetrospectiveGenerateRequest.QualityTrend> qualityTrends = qualityTrendService.getTrends(projectId);

        // 10. AI 개선 제안
        List<String> suggestions = improvementService.suggestImprovements(projectId, today);

        // 11. 회고 리포트 (Markdown)
        String markdown = reportRenderService.generateMarkdownReport(projectId, today);

        // 12. 시각화 요소
        List<RetrospectiveGenerateRequest.ChartElement> charts = visualizationService.getCharts(projectId, today);

        // 13. 내일 계획
        List<String> recommendations = planService.recommendTomorrowTasks(projectId);

        return RetrospectiveGenerateRequest.builder()
                .projectId(projectId)
                .projectTitle(project.getTitle())
                .commits(commits)
                .completedTasks(completedTasks)
                .workTimeDistribution(timeDistribution)
                .codeQualityScore(codeQualityScore)
                .goalAchievementRate(goalAchievementRate)
                .productivity(RetrospectiveGenerateRequest.ProductivitySummary.builder()
                        .totalHours(productivity.getTotalHours())
                        .commitFrequency(productivity.getCommitFrequency())
                        .codeVolume(productivity.getCodeVolume())
                        .grade(productivity.getGrade())
                        .build())
                .productivityPeaks(peaks)
                .delayReasons(delayReasons)
                .qualityTrends(qualityTrends)
                .improvementSuggestions(suggestions)
                .markdownReport(markdown)
                .charts(charts)
                .tomorrowRecommendations(recommendations)
                .build();*/
        return null;
    }

}

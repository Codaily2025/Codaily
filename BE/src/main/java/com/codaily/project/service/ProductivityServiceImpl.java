package com.codaily.project.service;

import com.codaily.project.dto.ProductivityCalculateRequest;
import com.codaily.project.dto.ProductivityCalculateResponse;
import com.codaily.project.entity.CodeCommit;
import com.codaily.project.entity.CodeReview;
import com.codaily.project.entity.Task;
import com.codaily.project.repository.CodeCommitRepository;
import com.codaily.project.repository.CodeReviewRepository;
import com.codaily.project.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ProductivityServiceImpl implements ProductivityService {

    private final TaskRepository taskRepository;
    private final CodeCommitRepository commitRepository;
    private final CodeReviewRepository reviewRepository;

    @Override
    public ProductivityCalculateResponse calculateProductivity(ProductivityCalculateRequest request) {
        LocalDateTime start = request.getPeriod().getStart().atStartOfDay();
        LocalDateTime end = request.getPeriod().getEnd().atTime(23, 59, 59);

        Map<String, ProductivityCalculateResponse.MetricScore> breakdown = new HashMap<>();
        double overallScore = 0.0;

        // ✅ Task 완료율
        if (request.getMetrics().isIncludeTaskCompletion()) {
            List<Task> completedTasks = taskRepository.findCompletedTasks(Long.valueOf(request.getProjectId()), start, end);
            double taskScore = completedTasks.size() * 10;
            breakdown.put("taskCompletion", ProductivityCalculateResponse.MetricScore.builder()
                    .score(taskScore).weight(0.4).build());
            overallScore += taskScore * 0.4;
        }

        // ✅ Commit 활동
        if (request.getMetrics().isIncludeCommits()) {
            List<CodeCommit> commits = commitRepository.findByCommittedAtBetween(start, end);
            double commitScore = commits.size() * 5;
            breakdown.put("commitFrequency", ProductivityCalculateResponse.MetricScore.builder()
                    .score(commitScore).weight(0.3).build());
            overallScore += commitScore * 0.3;
        }

        // ✅ 코드 품질
        if (request.getMetrics().isIncludeCodeQuality()) {
            List<CodeReview> reviews = reviewRepository.findByProjectIdAndCreatedAtBetween(
                    Long.valueOf(request.getProjectId()), start, end);
            double avgQuality = reviews.stream()
                    .mapToDouble(r -> r.getQualityScore() != null ? r.getQualityScore() : 0)
                    .average().orElse(0);
            breakdown.put("codeQuality", ProductivityCalculateResponse.MetricScore.builder()
                    .score(avgQuality).weight(0.3).build());
            overallScore += avgQuality * 0.3;
        }

        return ProductivityCalculateResponse.builder()
                .overallScore(overallScore)
                .breakdown(breakdown)
                .trend("improving")
                .benchmarkComparison(ProductivityCalculateResponse.BenchmarkComparison.builder()
                        .personalAverage(72.3)
                        .projectAverage(68.9)
                        //.industryAverage(65.0)
                        .build())
                .build();
    }
}
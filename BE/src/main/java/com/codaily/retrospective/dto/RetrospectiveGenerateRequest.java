package com.codaily.retrospective.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class RetrospectiveGenerateRequest {
    private Long projectId;
    private String projectTitle;

    private List<CommitSummary> commits;
    private List<TaskSummary> completedTasks;
    private Map<String, Double> workTimeDistribution; // 예: {"코딩": 3.5, "회의": 2.0}
    private Double codeQualityScore;
    private Double goalAchievementRate;
    private ProductivitySummary productivity;
    private List<ProductivityPeak> productivityPeaks;
    private List<String> delayReasons;
    private List<QualityTrend> qualityTrends;
    private List<String> improvementSuggestions;
    private String markdownReport;
    private List<ChartElement> charts;
    private List<String> tomorrowRecommendations;

    @Data
    @Builder
    public static class CommitSummary {
        private String message;
        private int linesChanged;
        private int fileCount;
        private int addedLines;
        private String commitTime;
    }

    @Data
    @Builder
    public static class TaskSummary {
        private Long taskId;
        private String title;
        private String status;
        private String description;
    }

    @Data
    @Builder
    public static class ProductivitySummary {
        private Double totalHours;
        private Double commitFrequency;
        private Double codeVolume;
        private String grade; // 상/중/하
    }

    @Data
    @Builder
    public static class ProductivityPeak {
        private String timePeriod;
        private Double codeVolume;
    }

    @Data
    @Builder
    public static class QualityTrend {
        private String period;
        private Double score;
    }

    @Data
    @Builder
    public static class ChartElement {
        private String title;
        private String type; // "line", "bar", etc.
        private Map<String, Object> data;
    }
}

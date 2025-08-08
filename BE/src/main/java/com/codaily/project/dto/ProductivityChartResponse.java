package com.codaily.project.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductivityChartResponse {
    private boolean success;
    private Data data;

    @lombok.Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Data {
        private String period;
        private List<ChartData> chartData;
        private Summary summary;
    }

    @lombok.Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ChartData {
        private String date;
        private int completedTasks;
        private double productivityScore;
        private int commits; // 0-100 정규화된 값 (차트 표시용)

        private int actualCommits; // 실제 커밋 수
    }

    @lombok.Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Summary {
        private double averageTasksPerDay;
        private double averageProductivityScore;
        private String trend; // increasing, decreasing, stable
        private double trendPercentage;
        private int totalCommits; // 총 커밋 수
        private double averageCommits; // 평균 커밋 수
        private int maxCommits; // 최대 커밋 수
    }
}
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
        private int commits;
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
    }
}
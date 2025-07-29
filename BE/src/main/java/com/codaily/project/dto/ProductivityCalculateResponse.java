package com.codaily.project.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductivityCalculateResponse {
    private double overallScore;
    private Map<String, MetricScore> breakdown;
    private String trend;
    private BenchmarkComparison benchmarkComparison;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MetricScore {
        private double score;
        private double weight;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BenchmarkComparison {
        private double personalAverage;
        private double projectAverage;
       // private double industryAverage;
    }
}

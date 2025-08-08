package com.codaily.project.dto;

import com.codaily.project.entity.FeatureItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductivityDetailResponse {
    private boolean success;
    private Data data;

    @lombok.Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Data {
        private String date;
        private List<FeatureItem> completedFeatures;
        private List<Commit> commits;
        private ProductivityFactors productivityFactors;
    }

    @lombok.Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CompletedFeatures {
        private String id;
        private String title;
    }

    @lombok.Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Commit {
        private String hash;
        private String message;
    }

    @lombok.Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ProductivityFactors {
      //  private double codeQuality;
        private int completedFeatures;
        private double productivityScore;
    }
}
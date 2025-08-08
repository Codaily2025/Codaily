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
public class SubFeatureListResponse {
    private boolean success;
    private SubFeatureListData data;
    private String message;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SubFeatureListData {
        private Long parentFeatureId;
        private String parentFeatureTitle;
        private List<FeatureDetailResponse.SubFeatureInfo> subFeatures;
    }
}
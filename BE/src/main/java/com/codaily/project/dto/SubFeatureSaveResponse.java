package com.codaily.project.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SubFeatureSaveResponse {
    private String type;
    private SubFeatureItem content;

    @Data
    @Builder
    public static class SubFeatureItem {
        private Long projectId;
        private Long specId;
        private Long parentFeatureId;
        private FeatureSaveItem featureSaveItem;
    }
}

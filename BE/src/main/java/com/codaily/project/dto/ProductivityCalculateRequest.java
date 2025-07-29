package com.codaily.project.dto;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductivityCalculateRequest {
    private String userId;
    private String projectId;
    private Period period;
    private Metrics metrics;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Period {
        private LocalDate start;
        private LocalDate end;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Metrics {
        private boolean includeCommits;
        private boolean includeTaskCompletion;
        private boolean includeCodeQuality;
    }
}

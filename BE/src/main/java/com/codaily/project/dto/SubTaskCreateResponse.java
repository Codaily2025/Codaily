package com.codaily.project.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubTaskCreateResponse {
    private boolean success;
    private SubTaskData data;
    private String message;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SubTaskData {
        private Long taskId;
        private String title;
        private String description;
        private String status;
        private Long featureId;
        private LocalDateTime createdAt;
    }
}
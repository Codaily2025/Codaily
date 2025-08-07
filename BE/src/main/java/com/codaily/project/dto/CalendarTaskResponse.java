package com.codaily.project.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CalendarTaskResponse {
    private boolean success;
    private CalendarTaskData data;
    private String message;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CalendarTaskData {
        private LocalDate date;
        private List<TaskInfo> tasks;
//        private Summary summary;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TaskInfo {
        private Long taskId;
        private String title;
        private String description;
        private String status;
        private String featureTitle;
        private String category;
        private Integer priorityLevel;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private LocalDateTime completedAt;
    }

}
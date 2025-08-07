package com.codaily.project.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActiveProjectsResponse {
    private boolean success;
    private ProjectData data;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProjectData {
        private List<ProjectInfo> projects;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProjectInfo {
        private String id;
        private String name;
        private String status;
        private Integer progress;
        private LocalDateTime lastActivity;
        private LocalDate dueDate;
        private boolean isCurrentProject;
        private LocalDateTime completedAt;
    }
}
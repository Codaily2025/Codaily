package com.codaily.mypage.dto;

import com.codaily.project.dto.ProjectRepositoryResponse;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class ProjectDetailResponse {
    private Long projectId;
    private String title;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    private List<DaysOfWeekResponse> daysOfWeeks;
    private List<ScheduleResponse> schedules;
    private List<ProjectRepositoryResponse> repositories;
}

package com.codaily.mypage.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class ProjectListResponse {
    private Long projectId;
    private String title;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    private int progressRate;
    private List<String> techStacks;
}

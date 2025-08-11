package com.codaily.retrospective.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class RecommendedTaskListResponse {
    private Long projectId;
    private int totalRecommendedTasks;
    private List<RecommendedTaskResponse> recommendedTasks;
    private String message; // 추천이 없을 때 메시지
}
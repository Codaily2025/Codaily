package com.codaily.retrospective.service;

import com.codaily.retrospective.dto.RecommendedTaskListResponse;

public interface RecommendedTaskService {

    RecommendedTaskListResponse getRecommendedTasks(Long projectId, Integer limit);
}
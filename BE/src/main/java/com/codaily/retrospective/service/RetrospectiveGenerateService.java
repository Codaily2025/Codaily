package com.codaily.retrospective.service;

import com.codaily.project.entity.Project;
import com.codaily.retrospective.dto.RetrospectiveGenerateResponse;

import java.util.concurrent.CompletableFuture;

public interface RetrospectiveGenerateService {
    CompletableFuture<RetrospectiveGenerateResponse> generateProjectDailyRetrospective(Project project, RetrospectiveTriggerType type);
}

package com.codaily.retrospective.service;

import com.codaily.project.entity.Project;
import com.codaily.retrospective.dto.RetrospectiveGenerateRequest;
import com.codaily.retrospective.dto.RetrospectiveGenerateResponse;
import com.codaily.retrospective.dto.RetrospectiveListResponse;
import com.codaily.retrospective.dto.RetrospectiveScrollResponse;

import java.time.LocalDate;

public interface RetrospectiveService {
    void saveRetrospective(Project project, RetrospectiveGenerateResponse resp, LocalDate date, RetrospectiveTriggerType triggerType);
    boolean existsByProjectAndDate(Project project, LocalDate date);
    RetrospectiveGenerateRequest collectRetrospectiveData(Project project, Long userId, RetrospectiveTriggerType triggerType);
    RetrospectiveGenerateResponse getDailyRetrospective(Long projectId, LocalDate date);
    RetrospectiveListResponse getAllDailyRetrospectives(Long projectId);
    RetrospectiveScrollResponse getProjectScroll(Long projectId, LocalDate before, int limit);
    RetrospectiveScrollResponse getUserScroll(Long userId, LocalDate before, int limit);
}

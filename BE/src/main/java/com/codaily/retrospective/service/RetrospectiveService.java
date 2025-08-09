package com.codaily.retrospective.service;

import com.codaily.project.entity.Project;
import com.codaily.retrospective.dto.RetrospectiveGenerateRequest;
import com.codaily.retrospective.dto.RetrospectiveGenerateResponse;
import com.codaily.retrospective.dto.RetrospectiveListResponse;

import java.time.LocalDate;

public interface RetrospectiveService {
    void saveRetrospective(Project project, RetrospectiveGenerateResponse response);
    boolean existsByProjectAndDate(Project project, LocalDate date);
    RetrospectiveGenerateRequest collectRetrospectiveData(Project project, Long userId, RetrospectiveTriggerType triggerType);
    RetrospectiveGenerateResponse getDailyRetrospective(Long projectId, LocalDate date);
    RetrospectiveListResponse getAllDailyRetrospectives(Long projectId);
}

package com.codaily.retrospective.service;

import com.codaily.project.entity.Project;
import com.codaily.retrospective.dto.RetrospectiveGenerateRequest;

import java.time.LocalDate;

public interface RetrospectiveService {
    void saveRetrospective(Project project, String content);
    boolean existsByProjectAndDate(Project project, LocalDate date);
    RetrospectiveGenerateRequest collectRetrospectiveData(Project project, Long userId);
}

package com.codaily.project.service;

import com.codaily.project.dto.ActiveProjectsResponse;

public interface ActiveProjectService {
    ActiveProjectsResponse getActiveProjects(Long userId);
}
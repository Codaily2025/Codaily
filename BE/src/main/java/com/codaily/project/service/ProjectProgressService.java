package com.codaily.project.service;

import com.codaily.project.dto.ProjectProgressResponse;

public interface ProjectProgressService {
    ProjectProgressResponse getProjectProgress(Long projectId);
}
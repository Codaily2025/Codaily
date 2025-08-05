package com.codaily.project.service;

import com.codaily.auth.entity.User;
import com.codaily.project.dto.ProjectCreateRequest;
import com.codaily.project.dto.ProjectRepositoryResponse;
import com.codaily.project.entity.Project;

import java.util.List;

public interface ProjectService {
    void saveRepositoryForProject(Long projectId, String repoName, String repoUrl);

    List<ProjectRepositoryResponse> getRepositoriesByProjectId(Long projectId);

    void deleteRepositoryById(Long repoId);

    Project createProject(ProjectCreateRequest request, User user);

    int calculateTotalUserAvailableHours(Long projectId);
}

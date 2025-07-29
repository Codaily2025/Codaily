package com.codaily.project.service;

import com.codaily.project.dto.ProjectRepositoryResponse;

import java.util.List;

public interface ProjectService {
    public void saveRepositoryForProject(Long projectId, String repoName, String repoUrl);

    List<ProjectRepositoryResponse> getRepositoriesByProjectId(Long projectId);

    void deleteRepositoryById(Long repoId);
}

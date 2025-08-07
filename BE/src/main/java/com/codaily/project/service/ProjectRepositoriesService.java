package com.codaily.project.service;

import com.codaily.project.entity.ProjectRepositories;

import java.util.List;
import java.util.Optional;

public interface ProjectRepositoriesService {
    ProjectRepositories findByProject_ProjectIdAndRepoName(Long projectId, String repoName);
    ProjectRepositories getRepoByName(String repoName);
}

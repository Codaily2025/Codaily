package com.codaily.project.service;

import com.codaily.project.entity.ProjectRepositories;

import java.util.List;
import java.util.Optional;

public interface ProjectRepositoriesService {
    ProjectRepositories getRepoByName(String repoName);
}

package com.codaily.project.service;

import com.codaily.project.entity.ProjectRepositories;
import com.codaily.project.repository.ProjectRepositoriesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProjectRepositoriesServiceImpl implements ProjectRepositoriesService {

    private final ProjectRepositoriesRepository repository;

    @Override
    public ProjectRepositories getRepoByName(String repoName) {
        return repository.findByRepoName(repoName)
                .orElseThrow(() -> new IllegalArgumentException("레포지토리를 찾을 수 없습니다."));
    }
}

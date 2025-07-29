package com.codaily.project.service;

import com.codaily.project.dto.ProjectRepositoryResponse;
import com.codaily.project.entity.ProjectRepository;
import com.codaily.project.repository.ProjectRepositoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepositoryRepository repository;

    public void saveRepositoryForProject(Long projectId, String repoName, String repoUrl) {
        ProjectRepository entity = new ProjectRepository();
        entity.setProjectId(projectId);
        entity.setRepoName(repoName);
        entity.setRepoUrl(repoUrl);
        entity.setCreatedAt(LocalDateTime.now());

        repository.save(entity);
    }

    @Override
    public List<ProjectRepositoryResponse> getRepositoriesByProjectId(Long projectId) {
        List<ProjectRepository> entities = repository.findByProjectId(projectId);
        return entities.stream()
                .map(repo -> ProjectRepositoryResponse.builder()
                        .repoId(repo.getRepoId())
                        .repoName(repo.getRepoName())
                        .repoUrl(repo.getRepoUrl())
                        .createdAt(repo.getCreatedAt())
                        .build())
                .toList();
    }

    @Override
    public void deleteRepositoryById(Long repoId) {
        if (!repository.existsById(repoId)) {
            throw new IllegalArgumentException("해당 리포지토리가 존재하지 않습니다.");
        }
        repository.deleteById(repoId);
    }
}

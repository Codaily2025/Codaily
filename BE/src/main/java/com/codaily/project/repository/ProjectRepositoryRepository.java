package com.codaily.project.repository;
import com.codaily.project.entity.ProjectRepository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectRepositoryRepository extends JpaRepository<ProjectRepository, Long> {
    boolean existsByProjectIdAndRepoUrl(Long projectId, String repoUrl);

    List<ProjectRepository> findByProjectId(Long projectId);

}


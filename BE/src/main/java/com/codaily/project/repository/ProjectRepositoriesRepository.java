package com.codaily.project.repository;
import com.codaily.project.entity.ProjectRepositories;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProjectRepositoriesRepository extends JpaRepository<ProjectRepositories, Long> {
    boolean existsByProjectIdAndRepoUrl(Long projectId, String repoUrl);

//    List<ProjectRepositories> findByProjectId(Long projectId);
    Optional<ProjectRepositories> findByProject_Id(Long projectId);
}


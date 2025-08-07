package com.codaily.project.repository;

import com.codaily.project.entity.Project;
import com.codaily.project.entity.ProjectRepositories;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProjectRepositoriesRepository extends JpaRepository<ProjectRepositories, Long> {
    @Query("SELECT COUNT(r) > 0 FROM ProjectRepositories r WHERE r.project.projectId = :projectId AND r.repoUrl = :repoUrl")
    boolean existsByProjectIdAndRepoUrl(@Param("projectId") Long projectId, @Param("repoUrl") String repoUrl);
    Optional<ProjectRepositories> findByProject_ProjectIdAndRepoName(Long projectId, String repoName);
    Optional<ProjectRepositories> findByRepoName(String repoName);

    @Query("SELECT r FROM ProjectRepositories r WHERE r.project.projectId = :projectId")
    List<ProjectRepositories> findByProjectId(@Param("projectId") Long projectId);
}


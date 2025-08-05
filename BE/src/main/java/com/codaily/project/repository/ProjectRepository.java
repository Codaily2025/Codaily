package com.codaily.project.repository;

import com.codaily.project.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

import java.util.Optional;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    boolean existsByProjectId(Long projectId);

    Optional<Project> findByProjectId(Long projectId);

    @Query("SELECT p FROM Project p WHERE p.status IN ('IN_PROGRESS')")
    List<Project> findActiveProjects();

    List<Project> findByUser_UserId(Long userId);

    List<Project> findByUser_UserIdAndStatus(Long userId, String status);
}

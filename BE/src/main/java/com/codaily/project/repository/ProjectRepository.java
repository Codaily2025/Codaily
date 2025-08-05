package com.codaily.project.repository;

import com.codaily.project.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    boolean existsByProjectId(Long projectId);

    Optional<Project> findById(Long projectId);
}

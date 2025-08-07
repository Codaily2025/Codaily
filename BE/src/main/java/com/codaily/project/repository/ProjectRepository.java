package com.codaily.project.repository;

import com.codaily.project.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    boolean existsByProjectId(Long projectId);


    Optional<Project> findByProjectId(Long projectId);

    @Query("SELECT p FROM Project p WHERE p.status IN ('IN_PROGRESS')")
    List<Project> findActiveProjects();

    List<Project> findByUser_UserId(Long userId);

    List<Project> findByUser_UserIdAndStatus(Long userId, String status);

    Project getProjectByProjectId(Long projectId);

    // 사용자의 활성 프로젝트 조회
    @Query("SELECT p FROM Project p WHERE p.user.userId = :userId AND p.status IN ('TODO', 'IN_PROGRESS', 'COMPLETED') ORDER BY p.updatedAt DESC")
    List<Project> findActiveProjectsByUserId(@Param("userId") Long userId);

    // 현재 진행중인 프로젝트 조회
    @Query("SELECT p FROM Project p WHERE p.user.userId = :userId AND p.status = 'IN_PROGRESS'")
    List<Project> findCurrentProjectsByUserId(@Param("userId") Long userId);

    @Query("SELECT MAX(t.completedAt) FROM Task t WHERE t.projectId = :projectId AND t.status = 'COMPLETED'")
    LocalDateTime findLastTaskCompletionTime(@Param("projectId") Long projectId);

}

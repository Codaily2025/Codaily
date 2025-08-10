package com.codaily.project.repository;

import com.codaily.project.entity.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.time.LocalDate;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    boolean existsByProjectId(Long projectId);


    Optional<Project> findByProjectId(Long projectId);

    @Query("SELECT p FROM Project p WHERE p.status = 'IN_PROGRESS' ORDER BY p.projectId")
    List<Project> findActiveProjects();

    @Query("SELECT COUNT(p) FROM Project p WHERE p.status = 'IN_PROGRESS'")
    long countActiveProjects();

    List<Project> findByUser_UserId(Long userId);

    List<Project> findByUser_UserIdAndStatus(Long userId, Enum<Project.ProjectStatus> status);

    void deleteByProjectId(Long projectId);

    List<Project> findByStatusAndUser_UserIdOrderByCreatedAtDesc(Project.ProjectStatus status, Long userId);

    Project getProjectByProjectId(Long projectId);

    // 사용자의 활성 프로젝트 조회
    @Query("SELECT p FROM Project p WHERE p.user.userId = :userId AND p.status IN ('TODO', 'IN_PROGRESS', 'COMPLETED') ORDER BY p.updatedAt DESC")
    List<Project> findActiveProjectsByUserId(@Param("userId") Long userId);

    // 현재 진행중인 프로젝트 조회
    @Query("SELECT p FROM Project p WHERE p.user.userId = :userId AND p.status = 'IN_PROGRESS'")
    List<Project> findCurrentProjectsByUserId(@Param("userId") Long userId);

    @Query("SELECT MAX(f.completedAt) FROM FeatureItem f WHERE f.project.projectId = :projectId AND f.status = 'DONE'")
    LocalDateTime findLastTaskCompletionTime(@Param("projectId") Long projectId);

    // 메서드 수정 후 삭제 예정
    Optional<Project> findById(Long projectId);

    Boolean existsByProjectIdAndUser_UserId(Long projectId, Long userId);

    @Query("SELECT DISTINCT p.projectId FROM Project p " +
            "JOIN FeatureItem f ON f.project = p " +
            "JOIN FeatureItemSchedule s ON s.featureItem = f " +
            "WHERE p.status = 'ACTIVE' " +
            "AND f.status IN ('TODO', 'IN_PROGRESS') " +
            "AND s.scheduleDate <= :yesterday " +
            "AND s.withinProjectPeriod = true")
    List<Long> findProjectsWithOverdueFeatures(@Param("yesterday") LocalDate yesterday);

    @Query("SELECT DISTINCT p.projectId FROM Project p " +
            "JOIN FeatureItem f ON f.project = p " +
            "JOIN FeatureItemSchedule s ON s.featureItem = f " +
            "WHERE p.status = 'ACTIVE' " +
            "AND f.status = 'TODO' " +
            "AND s.scheduleDate = :today")
    List<Long> findProjectsWithTodayFeatures(@Param("today") LocalDate today);

    @Query("SELECT DISTINCT p.projectId FROM Project p " +
            "JOIN FeatureItem f ON f.project = p " +
            "WHERE p.status = 'ACTIVE' " +
            "AND f.status = 'IN_PROGRESS'")
    List<Long> findProjectsWithInProgressFeatures();
}

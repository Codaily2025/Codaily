package com.codaily.project.repository;

import com.codaily.project.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {

    @Query("SELECT t FROM Task t WHERE t.projectId = :projectId AND t.status = :status AND t.completedAt BETWEEN :start AND :end")
    List<Task> findCompletedTasks(
            @Param("projectId") Long projectId,
            @Param("status") Task.Status status,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );
    @Query("SELECT t FROM Task t WHERE t.userId = :userId AND t.projectId = :projectId AND t.status = 'COMPLETED' AND t.completedAt BETWEEN :start AND :end")
    List<Task> findCompletedTasksByUser(@Param("userId") Long userId, @Param("projectId") Long projectId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.projectId = :projectId AND t.status = 'COMPLETED' AND t.completedAt BETWEEN :start AND :end")
    Long countCompletedTasks(@Param("projectId") Long projectId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.userId = :userId AND t.projectId = :projectId AND t.status = 'COMPLETED' AND t.completedAt BETWEEN :start AND :end")
    Long countCompletedTasksByUser(@Param("userId") Long userId, @Param("projectId") Long projectId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    List<Task> findByProjectIdAndStatus(Long projectId, Task.Status status);

    List<Task> findByUserIdAndProjectIdAndStatus(Long userId, Long projectId, Task.Status status);

    @Query("SELECT t FROM Task t WHERE t.projectId = :projectId AND t.completedAt BETWEEN :start AND :end ORDER BY t.completedAt DESC")
    List<Task> findTasksCompletedInPeriod(@Param("projectId") Long projectId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    Long countByProjectId(Long projectId);

    Long countByProjectIdAndStatus(Long projectId, Task.Status status);

}
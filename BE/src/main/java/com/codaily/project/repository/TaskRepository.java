package com.codaily.project.repository;

import com.codaily.project.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.time.LocalDateTime;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {
    @Query("SELECT t FROM Task t WHERE t.projectId = :projectId AND t.status = 'COMPLETED' AND t.completedAt BETWEEN :start AND :end")
    List<Task> findCompletedTasks(Long projectId, LocalDateTime start, LocalDateTime end);
}

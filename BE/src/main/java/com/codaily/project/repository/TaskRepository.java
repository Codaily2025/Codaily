package com.codaily.project.repository;

import com.codaily.project.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
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

    Long countByProjectId(Long projectId);

    Long countByProjectIdAndStatus(Long projectId, Task.Status status);

    //기능별 작업 조회 (생성순 정렬)
    @Query("SELECT t FROM Task t WHERE t.featureId = :featureId ORDER BY t.createdAt ASC")
    List<Task> findByFeatureIdOrderByCreatedAtAsc(@Param("featureId") Long featureId);


    //프로젝트의 특정 날짜에 활동이 있었던 작업들 조회(생성, 수정, 완료 날짜 기준)
    @Query("SELECT t FROM Task t WHERE t.projectId = :projectId AND " +
            "(DATE(t.createdAt) = :date OR DATE(t.updatedAt) = :date OR DATE(t.completedAt) = :date) " +
            "ORDER BY t.createdAt DESC")
    List<Task> findByProjectIdAndDate(@Param("projectId") Long projectId, @Param("date") LocalDate date);


}
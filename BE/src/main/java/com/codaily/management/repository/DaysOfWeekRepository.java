package com.codaily.management.repository;

import com.codaily.management.entity.DaysOfWeek;
import jakarta.persistence.ManyToOne;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface DaysOfWeekRepository extends JpaRepository<DaysOfWeek, Long> {
    List<DaysOfWeek> findByProject_ProjectId(Long projectId);

    @Modifying
    @Query("DELETE FROM DaysOfWeek d WHERE d.project.projectId = :projectId")
    void deleteByProjectId(Long projectId);

    List<DaysOfWeek> findAllByProject_ProjectId(Long projectId);
}

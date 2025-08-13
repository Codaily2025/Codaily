package com.codaily.project.repository;

import com.codaily.management.entity.Schedule;
import com.codaily.project.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

    boolean existsByProject_ProjectIdAndScheduledDate(Long projectId, LocalDate date);

    @Modifying
    @Transactional
    @Query("DELETE FROM Schedule s WHERE s.project.projectId = :projectId")
    void deleteByProjectId(Long projectId);

    List<Schedule> findAllByProject_ProjectId(Long projectId);

    void deleteByProject(Project project);

    List<Schedule> findByProject(Project project);
}

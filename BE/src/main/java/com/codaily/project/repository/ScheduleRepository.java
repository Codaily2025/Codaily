package com.codaily.project.repository;
import com.codaily.management.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

    boolean existsByProject_ProjectIdAndScheduledDate(Long projectId, LocalDate date);

    @Modifying
    @Query("DELETE FROM Schedule s WHERE s.project.projectId = :projectId")
    void deleteByProjectId(Long projectId);
}

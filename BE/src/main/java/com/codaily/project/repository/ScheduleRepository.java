package com.codaily.project.repository;
import com.codaily.management.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

    boolean existsByProject_ProjectIdAndScheduledDate(Long projectId, LocalDate date);
}

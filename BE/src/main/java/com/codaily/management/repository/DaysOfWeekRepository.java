package com.codaily.management.repository;

import com.codaily.management.entity.DaysOfWeek;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DaysOfWeekRepository extends JpaRepository<DaysOfWeek, Long> {
    List<DaysOfWeek> findByProject_ProjectId(Long projectId);
}

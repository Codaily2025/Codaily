package com.codaily.management.repository;

import com.codaily.project.entity.FeatureItemSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface FeatureItemSchedulesRepository extends JpaRepository<FeatureItemSchedule, Long> {
    @Query("SELECT s FROM FeatureItemSchedule s " +
            "WHERE s.featureItem.project.projectId = :projectId " +
            "AND s.scheduleDate BETWEEN :startDate AND :endDate " +
            "ORDER BY s.scheduleDate ASC")
    List<FeatureItemSchedule> findByProjectAndDateRange(Long projectId, LocalDate startDate, LocalDate endDate);
}

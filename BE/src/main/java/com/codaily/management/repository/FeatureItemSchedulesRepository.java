package com.codaily.management.repository;

import com.codaily.project.entity.FeatureItemSchedules;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface FeatureItemSchedulesRepository extends JpaRepository<FeatureItemSchedules, Long> {
    @Query("SELECT s FROM FeatureItemSchedules s " +
            "WHERE s.featureItem.project.projectId = :projectId " +
            "AND s.scheduleDate BETWEEN :startDate AND :endDate " +
            "ORDER BY s.scheduleDate ASC")
    List<FeatureItemSchedules> findByProjectAndDateRange(@Param("projectId") Long projectId,
                                                         @Param("startDate") LocalDate startDate,
                                                         @Param("endDate") LocalDate endDate);
}

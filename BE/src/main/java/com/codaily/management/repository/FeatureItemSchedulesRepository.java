package com.codaily.management.repository;

import com.codaily.management.entity.FeatureItemSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface FeatureItemSchedulesRepository extends JpaRepository<FeatureItemSchedule, Long> {
    @Query("SELECT s FROM FeatureItemSchedule s " +
            "WHERE s.featureItem.project.projectId = :projectId " +
            "AND s.scheduleDate BETWEEN :startDate AND :endDate " +
            "ORDER BY s.scheduleDate ASC")
    List<FeatureItemSchedule> findByProjectAndDateRange(Long projectId, LocalDate startDate, LocalDate endDate);

    void deleteByFeatureItemFeatureId(Long featureId);

    List<FeatureItemSchedule> findByFeatureItem_Project_ProjectIdAndScheduleDate(Long projectId, LocalDate date);

    void deleteByFeatureItemFeatureIdIn(List<Long> featureIds);

    @Query("SELECT MAX(fis.scheduleDate) FROM FeatureItemSchedule fis " +
                  "WHERE fis.featureItem.project.projectId = :projectId " +
                  "AND fis.featureItem.priorityLevel < :priorityLevel")
    LocalDate findLastScheduleDateByProjectIdAndPriorityLess(Long projectId, Integer priorityLevel);
}

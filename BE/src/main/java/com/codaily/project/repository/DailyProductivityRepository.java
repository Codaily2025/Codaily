package com.codaily.project.repository;

import com.codaily.project.entity.DailyProductivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DailyProductivityRepository extends JpaRepository<DailyProductivity, Long> {

    Optional<DailyProductivity> findByUserIdAndProjectIdAndDate(
            Long userId, Long projectId, LocalDate date);

    List<DailyProductivity> findByUserIdAndProjectIdAndDateBetween(
            Long userId, Long projectId, LocalDate startDate, LocalDate endDate);

    @Query("SELECT AVG(dp.completedFeatures) FROM DailyProductivity dp WHERE dp.userId = :userId AND dp.projectId = :projectId AND dp.date BETWEEN :startDate AND :endDate")
    Double findAverageTasksPerDay(@Param("userId") Long userId, @Param("projectId") Long projectId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);


    @Query("SELECT AVG(dp.productivityScore) FROM DailyProductivity dp WHERE dp.userId = :userId AND dp.projectId = :projectId AND dp.date BETWEEN :startDate AND :endDate")
    Double findAverageProductivityScore(@Param("userId") Long userId, @Param("projectId") Long projectId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}
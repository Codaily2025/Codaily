package com.codaily.project.repository;

import com.codaily.project.entity.ProductivityMetric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ProductivityMetricRepository extends JpaRepository<ProductivityMetric, Long> {


    @Query("SELECT AVG(pm.productivityScore) FROM ProductivityMetric pm WHERE pm.userId = :userId")
    Double findPersonalAverageScore(@Param("userId") Long userId);

    @Query("SELECT AVG(pm.productivityScore) FROM ProductivityMetric pm WHERE pm.projectId = :projectId")
    Double findProjectAverageScore(@Param("projectId") Long projectId);

    @Query("SELECT pm FROM ProductivityMetric pm WHERE pm.userId = :userId AND pm.projectId = :projectId ORDER BY pm.date DESC LIMIT 7")
    List<ProductivityMetric> findRecentMetrics(@Param("userId") Long userId, @Param("projectId") Long projectId);
}
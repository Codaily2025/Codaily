package com.codaily.codereview.repository;

import com.codaily.codereview.entity.CodeReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface CodeReviewRepository extends JpaRepository<CodeReview, Long> {

    List<CodeReview> findByProjectIdAndCreatedAtBetween(Long projectId, LocalDateTime start, LocalDateTime end);

    @Query("SELECT cr FROM CodeReview cr WHERE cr.projectId = :projectId AND cr.createdAt BETWEEN :start AND :end AND cr.qualityScore IS NOT NULL")
    List<CodeReview> findByProjectIdAndCreatedAtBetweenWithQualityScore(@Param("projectId") Long projectId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT AVG(cr.qualityScore) FROM CodeReview cr WHERE cr.projectId = :projectId AND cr.createdAt BETWEEN :start AND :end AND cr.qualityScore IS NOT NULL")
    Double findAverageQualityScoreByProjectAndPeriod(@Param("projectId") Long projectId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT cr FROM CodeReview cr WHERE cr.projectId = :projectId ORDER BY cr.createdAt DESC")
    List<CodeReview> findByProjectIdOrderByCreatedAtDesc(Long projectId);
}
package com.codaily.codereview.repository;

import com.codaily.codereview.entity.CodeReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CodeReviewRepository extends JpaRepository<CodeReview, Long> {

    List<CodeReview> findByProject_ProjectIdAndCreatedAtBetween(Long projectId, LocalDateTime start, LocalDateTime end);

    // WithQualityScore 오류
//    @Query("SELECT cr FROM CodeReview cr WHERE cr.projectId = :projectId AND cr.createdAt BETWEEN :start AND :end AND cr.qualityScore IS NOT NULL")
//    List<CodeReview> findByProject_ProjectIdAndCreatedAtBetweenWithQualityScore(@Param("projectId") Long projectId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT AVG(c.qualityScore) FROM CodeReview c WHERE c.project.id = :projectId AND c.createdAt BETWEEN :start AND :end")
    Double findAverageQualityScoreByProjectAndPeriod(
            @Param("projectId") Long projectId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    // 오류
//    @Query("SELECT cr FROM CodeReview cr WHERE cr.projectId = :projectId ORDER BY cr.createdAt DESC")
//    List<CodeReview> findByProject_ProjectIdOrderByCreatedAtDesc(Long projectId);

    Optional<CodeReview> findByFeatureItem_FeatureId(Long featureId);

    @Query("SELECT cr FROM CodeReview cr WHERE cr.project.user.userId = :userId AND cr.createdAt BETWEEN :start AND :end")
    List<CodeReview> findByProject_User_UserIdAndCreatedAtBetween(
            @Param("userId") Long userId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );
}
package com.codaily.codereview.repository;

import com.codaily.codereview.entity.CodeCommit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface CodeCommitRepository extends JpaRepository<CodeCommit, Long> {

    // 기간별 커밋 조회 (전체)
    List<CodeCommit> findByCommittedAtBetween(LocalDateTime start, LocalDateTime end);

    // 특정 사용자의 특정 프로젝트 커밋 조회 (FeatureItem을 통해)
    @Query("SELECT cc FROM CodeCommit cc JOIN cc.featureItem f WHERE f.project.user.userId = :userId AND f.project.projectId = :projectId AND cc.committedAt BETWEEN :start AND :end")
    List<CodeCommit> findByUser_UserIdAndProject_ProjectIdAndCommittedAtBetween(
            @Param("userId") Long userId,
            @Param("projectId") Long projectId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    // 특정 프로젝트의 커밋 수 조회 (FeatureItem을 통해)
    @Query("SELECT COUNT(cc) FROM CodeCommit cc JOIN cc.featureItem f WHERE f.project.projectId = :projectId AND cc.committedAt BETWEEN :start AND :end")
    Long countByProject_ProjectIdAndCommittedAtBetween(
            @Param("projectId") Long projectId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    // 기간별 커밋 조회 (날짜 순 정렬)
    @Query("SELECT cc FROM CodeCommit cc WHERE cc.committedAt BETWEEN :start AND :end ORDER BY cc.committedAt DESC")
    List<CodeCommit> findCommitsInPeriodOrderByDate(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    // 특정 프로젝트의 기간별 커밋 조회 (FeatureItem을 통해)
    @Query("SELECT cc FROM CodeCommit cc JOIN cc.featureItem f WHERE f.project.projectId = :projectId AND cc.committedAt BETWEEN :start AND :end")
    List<CodeCommit> findByProject_ProjectIdAndCommittedAtBetween(
            @Param("projectId") Long projectId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    // 특정 기능에 연결된 커밋들 조회
    @Query("SELECT cc FROM CodeCommit cc WHERE cc.featureItem.featureId = :featureId ORDER BY cc.committedAt DESC")
    List<CodeCommit> findByFeature_FeatureIdOrderByCommittedAtDesc(@Param("featureId") Long featureId);

    // 특정 기능의 기간별 커밋 조회
    @Query("SELECT cc FROM CodeCommit cc WHERE cc.featureItem.featureId = :featureId AND cc.committedAt BETWEEN :start AND :end")
    List<CodeCommit> findByFeature_FeatureIdAndCommittedAtBetween(
            @Param("featureId") Long featureId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    // 특정 사용자의 전체 커밋 조회 (모든 프로젝트)
    @Query("SELECT cc FROM CodeCommit cc JOIN cc.featureItem f WHERE f.project.user.userId = :userId AND cc.committedAt BETWEEN :start AND :end ORDER BY cc.committedAt DESC")
    List<CodeCommit> findByUser_UserIdAndCommittedAtBetween(
            @Param("userId") Long userId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    // 특정 프로젝트의 최근 커밋들 조회 (개수 제한)
    @Query("SELECT cc FROM CodeCommit cc JOIN cc.featureItem f WHERE f.project.projectId = :projectId ORDER BY cc.committedAt DESC LIMIT :limit")
    List<CodeCommit> findRecentCommitsByProject_ProjectId(@Param("projectId") Long projectId, @Param("limit") int limit);
}
package com.codaily.project.repository;

import com.codaily.project.entity.FeatureItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface FeatureItemRepository extends JpaRepository<FeatureItem, Long> {
    // 기존 메서드들 유지
    Optional<FeatureItem> findByProject_ProjectIdAndFeatureId(Long projectId, Long featureId);
    void deleteBySpecification_SpecId(Long specId);
    List<FeatureItem> findBySpecification_SpecId(Long specId);

    @Query("SELECT SUM(f.estimatedTime) FROM FeatureItem f WHERE f.specification.specId = :specId AND f.parentFeature IS NULL")
    Integer getTotalEstimatedTimeBySpecId(@Param("specId") Long specId);

    Optional<FeatureItem> findByProject_ProjectIdAndTitle(Long projectId, String featureTitle);

    List<FeatureItem> findByProject_ProjectId(Long projectId);
    Optional<FeatureItem> findByFeatureId(Long featureId);

    @Query("SELECT DISTINCT f.field FROM FeatureItem f WHERE f.project.projectId = :projectId AND f.parentFeature IS NULL ORDER BY f.field")
    List<String> findDistinctFieldsByProjectId(Long projectId);

    @Query("SELECT f FROM FeatureItem f WHERE f.project.projectId = :projectId AND f.field = :field AND f.parentFeature IS NOT NULL ORDER BY f.priorityLevel")
    List<FeatureItem> findByProjectIdAndField(Long projectId, String field);

    @Query("SELECT DISTINCT s.featureItem FROM FeatureItemSchedule s " +
            "WHERE s.featureItem.project.projectId = :projectId " +
            "AND s.scheduleDate <= :endDate " +
            "AND s.featureItem.status != 'DONE'")
    List<FeatureItem> findOverdueFeatures(Long projectId, LocalDate endDate);

    @Query("SELECT DISTINCT s.featureItem FROM FeatureItemSchedule s " +
            "WHERE s.featureItem.project.projectId = :projectId " +
            "AND s.scheduleDate = :today " +
            "AND s.featureItem.status = 'TODO'")
    List<FeatureItem> findTodayFeatures(Long projectId, LocalDate today);

    List<FeatureItem> findAllBySpecification_SpecId(Long specId);

    @Query("SELECT f FROM FeatureItem f WHERE f.project.projectId = :projectId AND f.parentFeature IS NULL")
    List<FeatureItem> findMainFeaturesByProjectId(@Param("projectId") Long projectId);

    List<FeatureItem> findByParentFeature(FeatureItem parentFeature);

    @Query("SELECT f.featureId FROM FeatureItem f WHERE f.project.projectId = :projectId")
    List<Long> findFeatureIdByProject_ProjectId(Long projectId);

    FeatureItem getFeatureItemByFeatureId(Long featureId);

    // 생산성 차트용 쿼리 (GitHub API 연동)

    // 특정 날짜에 완료된 기능들 조회 (status가 DONE이고 updatedAt이 해당 날짜인 것)
    @Query("SELECT f FROM FeatureItem f WHERE f.project.projectId = :projectId " +
            "AND f.status = 'DONE' AND DATE(f.updatedAt) = :date")
    List<FeatureItem> findCompletedFeaturesByProjectAndDate(
            @Param("projectId") Long projectId,
            @Param("date") LocalDate date
    );

    // 특정 날짜에 수정된 기능들 조회 (참고용)
    @Query("SELECT f FROM FeatureItem f WHERE f.project.projectId = :projectId " +
            "AND DATE(f.updatedAt) = :date ORDER BY f.updatedAt DESC")
    List<FeatureItem> findModifiedFeaturesByProjectAndDate(
            @Param("projectId") Long projectId,
            @Param("date") LocalDate date
    );

    // 하위 기능들 조회
    @Query("SELECT f FROM FeatureItem f WHERE f.parentFeature.featureId = :parentFeatureId ORDER BY f.createdAt ASC")
    List<FeatureItem> findSubFeaturesByParentId(@Param("parentFeatureId") Long parentFeatureId);

    // ===== FeatureDetailService에서 추가로 필요한 메서드들 =====

    // 특정 날짜에 활동이 있었던 기능들 조회 (생성, 수정, 완료 날짜 기준)
    @Query("SELECT f FROM FeatureItem f WHERE f.project.projectId = :projectId AND " +
            "(DATE(f.createdAt) = :date OR DATE(f.updatedAt) = :date OR DATE(f.completedAt) = :date) " +
            "ORDER BY f.createdAt DESC")
    List<FeatureItem> findByProjectIdAndDate(@Param("projectId") Long projectId, @Param("date") LocalDate date);

    // 프로젝트의 총 기능 수 조회
    @Query("SELECT COUNT(f) FROM FeatureItem f WHERE f.project.projectId = :projectId")
    Long countByProjectId(@Param("projectId") Long projectId);

    // 프로젝트의 특정 상태 기능 수 조회
    @Query("SELECT COUNT(f) FROM FeatureItem f WHERE f.project.projectId = :projectId AND f.status = :status")
    Long countByProjectIdAndStatus(@Param("projectId") Long projectId, @Param("status") String status);

    // 부모 기능의 완료된 하위 기능 수 조회
    @Query("SELECT COUNT(f) FROM FeatureItem f WHERE f.parentFeature.featureId = :parentFeatureId AND f.status = 'DONE'")
    Long countCompletedSubFeatures(@Param("parentFeatureId") Long parentFeatureId);

    // 부모 기능의 전체 하위 기능 수 조회
    @Query("SELECT COUNT(f) FROM FeatureItem f WHERE f.parentFeature.featureId = :parentFeatureId")
    Long countSubFeatures(@Param("parentFeatureId") Long parentFeatureId);

    // 특정 사용자의 기능들 조회 (프로젝트를 통해)
    @Query("SELECT f FROM FeatureItem f WHERE f.project.user.userId = :userId ORDER BY f.updatedAt DESC")
    List<FeatureItem> findByUserId(@Param("userId") Long userId);

    // 특정 사용자의 특정 기간 기능들 조회
    @Query("SELECT f FROM FeatureItem f WHERE f.project.user.userId = :userId " +
            "AND f.updatedAt BETWEEN :startDate AND :endDate ORDER BY f.updatedAt DESC")
    List<FeatureItem> findByUserIdAndDateRange(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    // 최근 수정된 기능들 조회 (개수 제한)
    @Query("SELECT f FROM FeatureItem f WHERE f.project.projectId = :projectId ORDER BY f.updatedAt DESC LIMIT :limit")
    List<FeatureItem> findRecentlyUpdatedFeatures(@Param("projectId") Long projectId, @Param("limit") int limit);

    // 진행중인 기능들 조회
    @Query("SELECT f FROM FeatureItem f WHERE f.project.projectId = :projectId AND f.status = 'IN_PROGRESS' ORDER BY f.updatedAt DESC")
    List<FeatureItem> findInProgressFeatures(@Param("projectId") Long projectId);

    // 완료된 기능들 조회
    @Query("SELECT f FROM FeatureItem f WHERE f.project.projectId = :projectId AND f.status = 'DONE' ORDER BY f.completedAt DESC")
    List<FeatureItem> findCompletedFeatures(@Param("projectId") Long projectId);
}
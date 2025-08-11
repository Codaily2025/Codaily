package com.codaily.project.repository;

import com.codaily.project.entity.FeatureItem;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface FeatureItemRepository extends JpaRepository<FeatureItem, Long> {

    // ===== 기본 조회 메서드들 =====
    Optional<FeatureItem> findByProject_ProjectIdAndFeatureId(Long projectId, Long featureId);

    Optional<FeatureItem> findByProject_ProjectIdAndTitle(Long projectId, String featureTitle);

    List<FeatureItem> findByProject_ProjectId(Long projectId);

    Optional<FeatureItem> findByFeatureId(Long featureId);

    List<FeatureItem> findByParentFeature(FeatureItem parentFeature);

    // ===== 명세서 관련 =====
    void deleteBySpecification_SpecId(Long specId);

    List<FeatureItem> findBySpecification_SpecId(Long specId);

    List<FeatureItem> findAllBySpecification_SpecId(Long specId);

    @Query("SELECT SUM(f.estimatedTime) FROM FeatureItem f WHERE f.specification.specId = :specId AND f.parentFeature IS NULL")
    Integer getTotalEstimatedTimeBySpecId(@Param("specId") Long specId);

    // ===== 필드/카테고리별 조회 =====
    @Query("SELECT DISTINCT f.field FROM FeatureItem f WHERE f.project.projectId = :projectId AND f.parentFeature IS NULL ORDER BY f.field")
    List<String> findDistinctFieldsByProjectId(Long projectId);

    //    @Query("SELECT f FROM FeatureItem f WHERE f.project.projectId = :projectId AND f.field = :field AND f.parentFeature IS NOT NULL ORDER BY f.priorityLevel")
    @Query("SELECT f FROM FeatureItem f WHERE f.project.projectId = :projectId AND f.field = :field ORDER BY f.priorityLevel")
    List<FeatureItem> findByProjectIdAndField(Long projectId, String field);


    @Query("SELECT DISTINCT f FROM FeatureItem f " +
            "WHERE f.project.projectId = :projectId " +
            "AND f.status != 'DONE' " +
            "AND (SELECT MAX(s.scheduleDate) FROM FeatureItemSchedule s " +
            "WHERE s.featureItem.featureId = f.featureId) <= :endDate")
    List<FeatureItem> findOverdueFeatures(Long projectId, LocalDate endDate);

    @Query("SELECT DISTINCT f FROM FeatureItem f " +
            "WHERE f.project.projectId = :projectId " +
            "AND f.status = 'TODO' " +
            "AND (SELECT MIN(s.scheduleDate) FROM FeatureItemSchedule s " +
            "WHERE s.featureItem.featureId = f.featureId) = :today")
    List<FeatureItem> findTodayStartFeatures(Long projectId, LocalDate today);

    List<FeatureItem> findByStatusAndProject_ProjectId(String status, Long projectId);

    // ===== 계층 구조 관련 =====
    @Query("SELECT f FROM FeatureItem f WHERE f.project.projectId = :projectId AND f.parentFeature IS NULL")
    List<FeatureItem> findMainFeaturesByProjectId(@Param("projectId") Long projectId);

    @Query("SELECT f FROM FeatureItem f WHERE f.parentFeature.featureId = :parentFeatureId ORDER BY f.createdAt ASC")
    List<FeatureItem> findSubFeaturesByParentId(@Param("parentFeatureId") Long parentFeatureId);

    // ===== 상태별 조회 =====
    @Query("SELECT f FROM FeatureItem f WHERE f.project.projectId = :projectId AND f.status = 'IN_PROGRESS' ORDER BY f.updatedAt DESC")
    List<FeatureItem> findInProgressFeatures(@Param("projectId") Long projectId);

    @Query("SELECT f FROM FeatureItem f WHERE f.project.projectId = :projectId AND f.status = 'DONE' ORDER BY f.completedAt DESC")
    List<FeatureItem> findCompletedFeatures(@Param("projectId") Long projectId);

    // ===== 날짜별 조회 (생산성 측정용) =====
    @Query("SELECT f FROM FeatureItem f WHERE f.project.projectId = :projectId " +
            "AND f.status = 'DONE' AND DATE(f.updatedAt) = :date")
    List<FeatureItem> findCompletedFeaturesByProjectAndDate(
            @Param("projectId") Long projectId,
            @Param("date") LocalDate date
    );

    @Query("SELECT f FROM FeatureItem f WHERE f.project.projectId = :projectId " +
            "AND DATE(f.updatedAt) = :date ORDER BY f.updatedAt DESC")
    List<FeatureItem> findModifiedFeaturesByProjectAndDate(
            @Param("projectId") Long projectId,
            @Param("date") LocalDate date
    );

    @Query("SELECT f FROM FeatureItem f WHERE f.project.projectId = :projectId AND " +
            "(DATE(f.createdAt) = :date OR DATE(f.updatedAt) = :date OR DATE(f.completedAt) = :date) " +
            "ORDER BY f.createdAt DESC")
    List<FeatureItem> findByProjectIdAndDate(@Param("projectId") Long projectId, @Param("date") LocalDate date);

    // ===== 사용자별 조회 =====
    @Query("SELECT f FROM FeatureItem f WHERE f.project.user.userId = :userId ORDER BY f.updatedAt DESC")
    List<FeatureItem> findByUserId(@Param("userId") Long userId);

    @Query("SELECT f FROM FeatureItem f WHERE f.project.user.userId = :userId " +
            "AND DATE(f.updatedAt) BETWEEN :startDate AND :endDate ORDER BY f.updatedAt DESC")
    List<FeatureItem> findByUserIdAndDateRange(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    // ===== 통계/카운트 메서드들 =====
    @Query("SELECT COUNT(f) FROM FeatureItem f WHERE f.project.projectId = :projectId")
    Long countByProjectId(@Param("projectId") Long projectId);

    @Query("SELECT COUNT(f) FROM FeatureItem f WHERE f.project.projectId = :projectId AND f.status = :status")
    Long countByProjectIdAndStatus(@Param("projectId") Long projectId, @Param("status") String status);

    @Query("SELECT f FROM FeatureItem f " +
            "WHERE f.project.projectId = :projectId " +
            "AND f.status = 'TODO' " +
            "AND f.estimatedTime > 0 " +
            "AND f.parentFeature IS NOT NULL " +
            "AND f.priorityLevel >= :fromPriority " +
            "ORDER BY f.priorityLevel ASC")
    List<FeatureItem> findSchedulableFeaturesByPriorityFrom(@Param("projectId") Long projectId, @Param("fromPriority") Integer fromPriority);

    @Query("SELECT COUNT(f) FROM FeatureItem f WHERE f.parentFeature.featureId = :parentFeatureId AND f.status = 'DONE'")
    Long countCompletedSubFeatures(@Param("parentFeatureId") Long parentFeatureId);

    @Query("SELECT COUNT(f) FROM FeatureItem f WHERE f.parentFeature.featureId = :parentFeatureId")
    Long countSubFeatures(@Param("parentFeatureId") Long parentFeatureId);

    @Query("SELECT f.featureId FROM FeatureItem f WHERE f.project.projectId = :projectId")
    List<Long> findFeatureIdByProject_ProjectId(@Param("projectId") Long projectId);

    // ===== 최근 활동 조회 (Pageable 사용) =====
    @Query("SELECT f FROM FeatureItem f WHERE f.project.projectId = :projectId ORDER BY f.updatedAt DESC")
    List<FeatureItem> findRecentlyUpdatedFeatures(@Param("projectId") Long projectId, Pageable pageable);

    @Query("SELECT f FROM FeatureItem f WHERE f.featureId = :featureId")
    FeatureItem getFeatureItemByFeatureId(@Param("featureId") Long featureId);

    // ===== 배치 상태 업데이트 ===
    @Modifying
    @Query("UPDATE FeatureItem f SET f.status = :status WHERE f.featureId IN :featureIds")
    int updateStatusBatch(List<Long> featureIds, String status);

    @Modifying
    @Transactional
    @Query("UPDATE FeatureItem f SET f.status = 'IN_PROGRESS' " +
            "WHERE f.project.projectId = :projectId AND f.status = 'TODO' " +
            "AND EXISTS (SELECT 1 FROM FeatureItemSchedule s " +
            "           WHERE s.featureItem = f AND s.scheduleDate = :today)")
    int updateTodayFeaturesToInProgress(@Param("projectId") Long projectId, @Param("today") LocalDate today);

    // 추천 작업 후보 조회 (Pageable 사용)
    @Query("SELECT f FROM FeatureItem f " +
            "WHERE f.project.projectId = :projectId " +
            "AND f.status = 'TODO' " +
            "AND f.priorityLevel IS NOT NULL " +
            "ORDER BY f.priorityLevel DESC, f.estimatedTime ASC, f.createdAt ASC")
    List<FeatureItem> findCandidateTasksForRecommendation(
            @Param("projectId") Long projectId,
            Pageable pageable
    );

}
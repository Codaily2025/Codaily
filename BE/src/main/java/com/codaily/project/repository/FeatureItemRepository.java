package com.codaily.project.repository;

import com.codaily.project.entity.FeatureItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface FeatureItemRepository extends JpaRepository<FeatureItem, Long> {
    Optional<FeatureItem> findByProject_ProjectIdAndFeatureId(Long projectId, Long featureId);

    void deleteBySpecification_SpecId(Long specId);

    List<FeatureItem> findBySpecification_SpecId(Long specId);

    @Query("SELECT SUM(f.estimatedTime) FROM FeatureItem f WHERE f.specification.specId = :specId AND f.parentFeature IS NULL")
    Integer getTotalEstimatedTimeBySpecId(@Param("specId") Long specId);

    List<FeatureItem> findByProject_ProjectId(Long projectId);

    Optional<FeatureItem> findByFeatureId(Long featureId);

    @Query("SELECT DISTINCT f.field FROM FeatureItem f WHERE f.project.projectId = :projectId AND f.parentFeature IS NULL ORDER BY f.field")
    List<String> findDistinctFieldsByProjectId(Long projectId);

    @Query("SELECT f FROM FeatureItem f WHERE f.project.projectId = :projectId AND f.field = :field ORDER BY f.priorityLevel")
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

<<<<<<< HEAD
   @Query("SELECT DISTINCT s.featureItem FROM FeatureItemSchedule s " +
          "WHERE s.featureItem.project.projectId = :projectId " +
          "AND s.scheduleDate = :today " +
          "AND s.featureItem.status = 'TODO'")
   List<FeatureItem> findTodayFeatures(Long projectId, LocalDate today);

   @Query("SELECT f.featureId FROM FeatureItem f WHERE f.project.projectId = :projectId")
   List<Long> findFeatureIdByProject_ProjectId(Long projectId);
=======
>>>>>>> 2b27733e58b7b6351281d1977369c99a7bf985f9
}

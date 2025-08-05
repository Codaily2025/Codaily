package com.codaily.project.repository;

import com.codaily.project.entity.FeatureItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FeatureItemRepository extends JpaRepository<FeatureItem, Long> {
   Optional<FeatureItem> findByProject_ProjectIdAndFeatureId(Long projectId, Long featureId);
    void deleteBySpecification_SpecId(Long specId);
    List<FeatureItem> findBySpecification_SpecId(Long specId);
    @Query("SELECT SUM(f.estimatedTime) FROM FeatureItem f WHERE f.specification.specId = :specId AND f.parentFeature IS NULL")
    Integer getTotalEstimatedTimeBySpecId(@Param("specId") Long specId);
   List<FeatureItem> findByProject_ProjectId(Long projectId);
}

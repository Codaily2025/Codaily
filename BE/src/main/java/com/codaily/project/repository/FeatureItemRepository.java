package com.codaily.project.repository;

import com.codaily.project.entity.FeatureItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FeatureItemRepository extends JpaRepository<FeatureItem, Long> {
    void deleteBySpecification_SpecId(Long specId);
    List<FeatureItem> findBySpecification_SpecId(Long specId);
    @Query("SELECT SUM(f.estimatedTime) FROM FeatureItem f WHERE f.specification.specId = :specId AND f.parentFeature IS NULL")
    Integer getTotalEstimatedTimeBySpecId(@Param("specId") Long specId);

    List<FeatureItem> findByProject_ProjectId(Long projectId);
    Optional<FeatureItem> findByProjectIdAndTitle(Long projectId, String featureTitle);
}


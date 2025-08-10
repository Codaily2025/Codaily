package com.codaily.codereview.repository;

import com.codaily.codereview.entity.FeatureItemChecklist;
import com.codaily.project.entity.FeatureItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public interface FeatureItemChecklistRepository extends JpaRepository<FeatureItemChecklist, Long> {
    Optional<List<FeatureItemChecklist>> findByFeatureItem_FeatureId(Long featureId);
    
    Optional<FeatureItemChecklist> findByFeatureItem_FeatureIdAndItem(Long featureId, String item);

    boolean existsByFeatureItem_FeatureIdAndItem(Long featureId, String item);

    @Query("SELECT COUNT(c) FROM FeatureItemChecklist c WHERE c.featureItem.featureId = :featureId")
    int countTotalByFeatureId(Long featureId);

    @Query("SELECT COUNT(c) FROM FeatureItemChecklist c " +
            "WHERE c.featureItem.featureId = :featureId AND c.done = true")
    int countCompleteByFeatureId(Long featureId);

    @Query("SELECT (COUNT(CASE WHEN c.done = true THEN 1 END) * 100.0 / COUNT(c)) " +
            "FROM FeatureItemChecklist c JOIN c.featureItem f WHERE f.id = :featureId")
    Optional<Double> getProgressRateByFeatureId(@Param("featureId") Long featureId);
}

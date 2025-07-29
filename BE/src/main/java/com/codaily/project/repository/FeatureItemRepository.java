package com.codaily.project.repository;

import com.codaily.project.entity.FeatureItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FeatureItemRepository extends JpaRepository<FeatureItem, Long> {
   Optional<FeatureItem> findByProject_ProjectIdAndFeatureId(Long projectId, Long featureId);
}

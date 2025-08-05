package com.codaily.project.repository;

import com.codaily.project.entity.FeatureItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeatureItemRepository extends JpaRepository<FeatureItem, Long> {
}


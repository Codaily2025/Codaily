package com.codaily.project.repository;

import com.codaily.project.entity.FeatureItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FeatureItemRepository extends JpaRepository<FeatureItem, Long> {
    void deleteBySpecification_SpecId(Long specId);
    List<FeatureItem> findBySpecification_SpecId(Long specId);
}


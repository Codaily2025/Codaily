package com.codaily.codereview.repository;

import com.codaily.codereview.entity.CodeReviewItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

public interface CodeReviewItemRepository extends JpaRepository<CodeReviewItem, Long> {
    List<CodeReviewItem> findByFeatureItem_Id(Long featureId);
}

package com.codaily.project.repository;

import com.codaily.project.entity.CodeReview;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface CodeReviewRepository extends JpaRepository<CodeReview, Long> {
    List<CodeReview> findByProjectIdAndCreatedAtBetween(Long projectId, LocalDateTime start, LocalDateTime end);
}

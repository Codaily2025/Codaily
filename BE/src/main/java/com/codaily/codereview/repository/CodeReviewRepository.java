package com.codaily.codereview.repository;

import com.codaily.codereview.entity.CodeReview;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CodeReviewRepository extends JpaRepository<CodeReview, Long> {
}

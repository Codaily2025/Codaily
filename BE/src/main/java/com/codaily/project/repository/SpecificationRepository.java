package com.codaily.project.repository;

import com.codaily.project.entity.Specification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SpecificationRepository extends JpaRepository<Specification, Long> {
    Optional<Specification> findById(Long specId);
}

package com.codaily.project.repository;


import com.codaily.project.entity.Specification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpecificationRepository extends JpaRepository<Specification, Long> {
}


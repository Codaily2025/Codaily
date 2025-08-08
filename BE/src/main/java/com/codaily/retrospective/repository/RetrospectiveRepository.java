package com.codaily.retrospective.repository;

import com.codaily.project.entity.Project;
import com.codaily.retrospective.entity.Retrospective;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface RetrospectiveRepository extends JpaRepository<Retrospective, Long> {
    boolean existsByProjectAndDate(Project project, LocalDate date);
}

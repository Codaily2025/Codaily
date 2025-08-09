package com.codaily.retrospective.repository;

import com.codaily.project.entity.Project;
import com.codaily.retrospective.entity.Retrospective;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface RetrospectiveRepository extends JpaRepository<Retrospective, Long> {
    boolean existsByProjectAndDate(Project project, LocalDate date);
    Retrospective findByProject_ProjectIdAndDate(Long projectId, LocalDate date);
    List<Retrospective> findAllByProject_ProjectIdOrderByDateDesc(Long projectId);
//    Page<Retrospective> findAllByProject_ProjectId(Long projectId, Pageable pageable);
}

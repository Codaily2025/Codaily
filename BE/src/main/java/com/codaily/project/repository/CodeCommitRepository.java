package com.codaily.project.repository;

import com.codaily.project.entity.CodeCommit;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface CodeCommitRepository extends JpaRepository<CodeCommit, Long> {
    List<CodeCommit> findByCommittedAtBetween(LocalDateTime start, LocalDateTime end);
}

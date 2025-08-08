package com.codaily.codereview.repository;

import com.codaily.codereview.entity.CodeCommit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CodeCommitRepository extends JpaRepository<CodeCommit, Long> {

    List<CodeCommit> findByCommittedAtBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT cc FROM CodeCommit cc JOIN cc.task t WHERE t.projectId = :projectId AND cc.committedAt BETWEEN :start AND :end")
    List<CodeCommit> findByProject_ProjectIdAndCommittedAtBetween(@Param("projectId") Long projectId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT cc FROM CodeCommit cc JOIN cc.task t WHERE t.userId = :userId AND t.projectId = :projectId AND cc.committedAt BETWEEN :start AND :end")
    List<CodeCommit> findByUser_IdAndProject_ProjectIdAndCommittedAtBetween(@Param("userId") Long userId, @Param("projectId") Long projectId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COUNT(cc) FROM CodeCommit cc JOIN cc.task t WHERE t.projectId = :projectId AND cc.committedAt BETWEEN :start AND :end")
    Long countByProject_ProjectIdAndCommittedAtBetween(@Param("projectId") Long projectId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT cc FROM CodeCommit cc WHERE cc.committedAt BETWEEN :start AND :end ORDER BY cc.committedAt DESC")
    List<CodeCommit> findCommitsInPeriodOrderByDate(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
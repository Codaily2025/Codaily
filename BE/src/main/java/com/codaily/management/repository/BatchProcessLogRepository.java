package com.codaily.management.repository;

import com.codaily.management.entity.BatchProcessLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface BatchProcessLogRepository extends JpaRepository<BatchProcessLog, Long> {

    /**
     * 특정 날짜에 특정 프로젝트의 처리 로그 조회
     */
    Optional<BatchProcessLog> findByProjectIdAndProcessDate(Long projectId, LocalDate processDate);

    /**
     * 오늘 이미 처리된 프로젝트 ID 목록 조회
     */
    @Query("SELECT b.projectId FROM BatchProcessLog b " +
            "WHERE b.processDate = :date " +
            "AND b.status IN ('COMPLETED', 'SKIPPED')")
    Set<Long> findCompletedProjectIds(@Param("date") LocalDate date);

    /**
     * 오늘 처리 실패한 프로젝트들 조회 (재시도 대상)
     */
    @Query("SELECT b FROM BatchProcessLog b " +
            "WHERE b.processDate = :date " +
            "AND b.status = 'FAILED' " +
            "AND b.retryCount < 3")
    List<BatchProcessLog> findFailedProjectsForRetry(@Param("date") LocalDate date);

    /**
     * 특정 날짜의 처리 상태별 개수 조회
     */
    @Query("SELECT b.status, COUNT(b) FROM BatchProcessLog b " +
            "WHERE b.processDate = :date " +
            "GROUP BY b.status")
    List<Object[]> getProcessingStats(@Param("date") LocalDate date);

    /**
     * 처리 중 상태로 오래 남아있는 프로젝트들 조회 (좀비 프로세스 탐지)
     */
    @Query("SELECT b FROM BatchProcessLog b " +
            "WHERE b.status = 'PROCESSING' " +
            "AND b.startedAt < :threshold")
    List<BatchProcessLog> findStuckProcesses(@Param("threshold") java.time.LocalDateTime threshold);

    /**
     * 오늘 아직 처리되지 않은 프로젝트들 확인
     */
    @Query("SELECT p.projectId FROM Project p " +
            "WHERE p.projectId NOT IN (" +
            "  SELECT b.projectId FROM BatchProcessLog b " +
            "  WHERE b.processDate = :date" +
            ") " +
            "AND p.status = 'ACTIVE'")
    List<Long> findUnprocessedActiveProjects(@Param("date") LocalDate date);
}
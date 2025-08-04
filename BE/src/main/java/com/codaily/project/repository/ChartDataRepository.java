package com.codaily.project.repository;

import com.codaily.project.entity.ChartData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ChartDataRepository extends JpaRepository<ChartData, Long> {

    // 생산성 차트 데이터 조회
    List<ChartData> findByUserIdAndProjectIdAndTypeAndGranularity(
            Long userId, Long projectId, String type, String granularity);

    // 특정 유형의 차트 데이터 조회
    List<ChartData> findByUserIdAndProjectIdAndType(
            Long userId, Long projectId, String type);

    // 최신 차트 데이터 조회
    @Query("SELECT cd FROM ChartData cd WHERE cd.userId = :userId AND cd.projectId = :projectId AND cd.type = :type ORDER BY cd.createdAt DESC")
    List<ChartData> findLatestByUserAndProjectAndType(
            @Param("userId") Long userId,
            @Param("projectId") Long projectId,
            @Param("type") String type);

    // 기간별 차트 데이터 조회
    @Query("SELECT cd FROM ChartData cd WHERE cd.userId = :userId AND cd.projectId = :projectId AND cd.type = :type AND cd.createdAt BETWEEN :startDate AND :endDate")
    List<ChartData> findByUserAndProjectAndTypeAndDateRange(
            @Param("userId") Long userId,
            @Param("projectId") Long projectId,
            @Param("type") String type,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    // 특정 단위의 최신 데이터 조회
    Optional<ChartData> findFirstByUserIdAndProjectIdAndTypeAndGranularityOrderByCreatedAtDesc(
            Long userId, Long projectId, String type, String granularity);

    // 사용자별 차트 타입 조회
    @Query("SELECT DISTINCT cd.type FROM ChartData cd WHERE cd.userId = :userId AND cd.projectId = :projectId")
    List<String> findDistinctTypesByUserAndProject(@Param("userId") Long userId, @Param("projectId") Long projectId);

    // 오래된 차트 데이터 삭제용
    @Query("DELETE FROM ChartData cd WHERE cd.createdAt < :cutoffDate")
    void deleteOldChartData(@Param("cutoffDate") LocalDateTime cutoffDate);
}
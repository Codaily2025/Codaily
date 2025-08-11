package com.codaily.project.repository;

import com.codaily.project.entity.DailyProductivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DailyProductivityRepository extends JpaRepository<DailyProductivity, Long> {

    Optional<DailyProductivity> findByUserIdAndProjectIdAndDate(
            Long userId, Long projectId, LocalDate date);

    List<DailyProductivity> findByUserIdAndProjectIdAndDateBetween(
            Long userId, Long projectId, LocalDate startDate, LocalDate endDate);

    @Query("SELECT AVG(dp.completedFeatures) FROM DailyProductivity dp WHERE dp.userId = :userId AND dp.projectId = :projectId AND dp.date BETWEEN :startDate AND :endDate")
    Double findAverageTasksPerDay(@Param("userId") Long userId, @Param("projectId") Long projectId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);


    @Query("SELECT AVG(dp.productivityScore) FROM DailyProductivity dp WHERE dp.userId = :userId AND dp.projectId = :projectId AND dp.date BETWEEN :startDate AND :endDate")
    Double findAverageProductivityScore(@Param("userId") Long userId, @Param("projectId") Long projectId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    // 벤치마크 계산을 위한 쿼리 메서드들 추가

    // 사용자의 기간별 평균 생산성 점수 (개인 평균 계산용)
    @Query("SELECT AVG(dp.productivityScore) FROM DailyProductivity dp " +
            "WHERE dp.userId = :userId AND dp.date BETWEEN :startDate AND :endDate")
    Double findAverageProductivityScoreByUserAndDateRange(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    // 프로젝트의 전체 평균 생산성 점수 (프로젝트 평균 계산용)
    @Query("SELECT AVG(dp.productivityScore) FROM DailyProductivity dp " +
            "WHERE dp.projectId = :projectId")
    Double findAverageProductivityScoreByProject(@Param("projectId") Long projectId);

    // 추가 유용한 쿼리들

    // 사용자의 전체 평균 생산성 점수
    @Query("SELECT AVG(dp.productivityScore) FROM DailyProductivity dp WHERE dp.userId = :userId")
    Double findAverageProductivityScoreByUser(@Param("userId") Long userId);

    // 프로젝트의 기간별 평균 생산성 점수
    @Query("SELECT AVG(dp.productivityScore) FROM DailyProductivity dp " +
            "WHERE dp.projectId = :projectId AND dp.date BETWEEN :startDate AND :endDate")
    Double findAverageProductivityScoreByProjectAndDateRange(
            @Param("projectId") Long projectId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    // 사용자의 최고 생산성 점수
    @Query("SELECT MAX(dp.productivityScore) FROM DailyProductivity dp WHERE dp.userId = :userId")
    Double findMaxProductivityScoreByUser(@Param("userId") Long userId);

    // 프로젝트의 최고 생산성 점수
    @Query("SELECT MAX(dp.productivityScore) FROM DailyProductivity dp WHERE dp.projectId = :projectId")
    Double findMaxProductivityScoreByProject(@Param("projectId") Long projectId);

    // 사용자의 데이터 존재 여부 확인
    @Query("SELECT COUNT(dp) > 0 FROM DailyProductivity dp WHERE dp.userId = :userId")
    boolean existsByUserId(@Param("userId") Long userId);

    // 프로젝트의 데이터 존재 여부 확인
    @Query("SELECT COUNT(dp) > 0 FROM DailyProductivity dp WHERE dp.projectId = :projectId")
    boolean existsByProjectId(@Param("projectId") Long projectId);


}
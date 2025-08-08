package com.codaily.project.repository;

import com.codaily.project.entity.DailyProductivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DailyProductivityRepository extends JpaRepository<DailyProductivity, Long> {

    Optional<DailyProductivity> findByUserIdAndProjectIdAndDate(Long userId, Long projectId, LocalDate date);
    List<DailyProductivity> findByUserIdAndProjectIdAndDateBetween(Long userId, Long projectId, LocalDate start, LocalDate end);

    // 프로젝트별: 날짜별 커밋 수 (해당 날짜 값 그대로)
    @Query("""
      select d.date, coalesce(d.totalCommits,0)
      from DailyProductivity d
      where d.userId = :userId
        and d.projectId = :projectId
        and d.date between :start and :end
      order by d.date
    """)
    List<Object[]> findCommitsByUserIdAndProjectIdAndDateBetween(
            @Param("userId") Long userId,
            @Param("projectId") Long projectId,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end);

<<<<<<< Updated upstream
    @Query("SELECT AVG(dp.completedTasks) FROM DailyProductivity dp WHERE dp.userId = :userId AND dp.projectId = :projectId AND dp.date BETWEEN :startDate AND :endDate")
    Double findAverageTasksPerDay(@Param("userId") Long userId, @Param("projectId") Long projectId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT AVG(dp.productivityScore) FROM DailyProductivity dp WHERE dp.userId = :userId AND dp.projectId = :projectId AND dp.date BETWEEN :startDate AND :endDate")
    Double findAverageProductivityScore(@Param("userId") Long userId, @Param("projectId") Long projectId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
=======
    // 전체 프로젝트 합산: 날짜별 커밋 수 합계
    @Query("""
      select d.date, sum(coalesce(d.totalCommits,0))
      from DailyProductivity d
      where d.userId = :userId
        and d.date between :start and :end
      group by d.date
      order by d.date
    """)
    List<Object[]> findCommitsByUserIdAndDateBetween(
            @Param("userId") Long userId,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end);

    // 전체 프로젝트 DailyProductivity 데이터 조회 (getOverallProductivityDetail에서 사용)
    List<DailyProductivity> findByUserIdAndDateBetween(Long userId, LocalDate start, LocalDate end);
>>>>>>> Stashed changes
}
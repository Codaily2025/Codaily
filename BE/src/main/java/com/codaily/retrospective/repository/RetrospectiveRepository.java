package com.codaily.retrospective.repository;

import com.codaily.project.entity.Project;
import com.codaily.retrospective.entity.Retrospective;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface RetrospectiveRepository extends JpaRepository<Retrospective, Long> {
    boolean existsByProjectAndDate(Project project, LocalDate date);

    Retrospective findByProject_ProjectIdAndDate(Long projectId, LocalDate date);

    List<Retrospective> findAllByProject_ProjectIdOrderByDateDesc(Long projectId);
//    Page<Retrospective> findAllByProject_ProjectId(Long projectId, Pageable pageable);

    /**
     * 한 프로젝트의 회고를 최신순으로 페이징 조회
     * - before 가 null이면 최신부터
     * - before 가 있으면 그 날짜보다 과거(r.date < :before)만
     */
    @Query("""
            SELECT r
            FROM Retrospective r
            WHERE r.project.projectId = :projectId
              AND (cast(:before as date) is null or r.date < cast(:before as date))
            ORDER BY r.date DESC, r.retroId DESC
            """)
    Slice<Retrospective> findProjectSlice(@Param("projectId") Long projectId, @Param("before") LocalDate before, Pageable pageable);

    /**
     * 특정 사용자(로그인 유저)의 모든 프로젝트 회고를 최신순으로 페이징 조회
     */
    @Query("""
            SELECT r
            FROM Retrospective r
            WHERE r.project.user.userId = :userId
              AND (cast(:before as date) is null or r.date < cast(:before as date))
            ORDER BY r.date DESC, r.retroId DESC
            """)
    Slice<Retrospective> findUserSlice(@Param("userId") Long userId, @Param("before") LocalDate before, Pageable pageable);
}

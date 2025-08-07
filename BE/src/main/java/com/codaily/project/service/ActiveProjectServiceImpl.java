package com.codaily.project.service;

import com.codaily.project.dto.ActiveProjectsResponse;
import com.codaily.project.dto.ProjectProgressResponse;
import com.codaily.project.entity.Project;
import com.codaily.project.repository.ProjectRepository;
import com.codaily.codereview.entity.CodeCommit;
import com.codaily.codereview.repository.CodeCommitRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Log4j2
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ActiveProjectServiceImpl implements ActiveProjectService {

    private final ProjectRepository projectRepository;
    private final CodeCommitRepository codeCommitRepository;
    private final ProjectProgressService projectProgressService;

    @Override
    public ActiveProjectsResponse getActiveProjects(Long userId) {
        try {
            log.info("활성 프로젝트 조회 시작 - userId: {}", userId);

            // 1. 사용자의 활성 프로젝트 조회
            List<Project> projects = projectRepository.findActiveProjectsByUserId(userId);
            log.debug("조회된 활성 프로젝트 수: {}", projects.size());

            // 2. 현재 진행중인 프로젝트 조회 (isCurrentProject 판단용)
            List<Project> currentProjects = projectRepository.findCurrentProjectsByUserId(userId);
            List<Long> currentProjectIds = currentProjects.stream()
                    .map(Project::getProjectId)
                    .collect(Collectors.toList());
            log.debug("현재 진행중인 프로젝트 수: {}", currentProjectIds.size());

            // 3. 프로젝트별 정보 변환
            List<ActiveProjectsResponse.ProjectInfo> projectInfos = projects.stream()
                    .map(project -> convertToProjectInfo(project, currentProjectIds.contains(project.getProjectId())))
                    .collect(Collectors.toList());

            log.info("활성 프로젝트 조회 완료 - userId: {}, 결과 수: {}", userId, projectInfos.size());

            return ActiveProjectsResponse.builder()
                    .success(true)
                    .data(ActiveProjectsResponse.ProjectData.builder()
                            .projects(projectInfos)
                            .build())
                    .build();

        } catch (Exception e) {
            log.error("활성 프로젝트 조회 중 오류 발생 - userId: {}", userId, e);
            return ActiveProjectsResponse.builder()
                    .success(false)
                    .build();
        }
    }

    // === Helper Methods ===

    private ActiveProjectsResponse.ProjectInfo convertToProjectInfo(Project project, boolean isCurrentProject) {
        log.debug("프로젝트 정보 변환 시작 - projectId: {}, projectName: {}",
                project.getProjectId(), project.getTitle());

        // 진행률 계산 (기존 ProjectProgressService 활용)
        Integer progress = calculateProgress(project.getProjectId());

        // 마지막 활동 시간 계산
        LocalDateTime lastActivity = calculateLastActivity(project.getProjectId(), project.getUpdatedAt());

        // 프로젝트 상태 매핑
        String mappedStatus = mapProjectStatus(project.getStatus());

        ActiveProjectsResponse.ProjectInfo.ProjectInfoBuilder builder =
                ActiveProjectsResponse.ProjectInfo.builder()
                        .id("proj_" + String.format("%03d", project.getProjectId()))
                        .name(project.getTitle())
                        .status(mappedStatus)
                        .progress(progress)
                        .dueDate(project.getEndDate())
                        .isCurrentProject(isCurrentProject);

        // 상태에 따른 조건부 필드 설정
        if ("active".equals(mappedStatus)) {
            builder.lastActivity(lastActivity);
        } else if ("completed".equals(mappedStatus)) {
            builder.completedAt(project.getUpdatedAt());
            builder.progress(100); // 완료된 프로젝트는 100%
        }

        log.debug("프로젝트 정보 변환 완료 - projectId: {}, status: {}, progress: {}",
                project.getProjectId(), mappedStatus, progress);

        return builder.build();
    }

    private String mapProjectStatus(Project.ProjectStatus dbStatus) {
        if (dbStatus == null) {
            log.debug("프로젝트 상태가 null, 기본값 'active' 사용");
            return "active";
        }

        String mappedStatus = switch (dbStatus) {
            case TODO, IN_PROGRESS -> "active";
            case COMPLETED -> "completed";
            default -> {
                log.debug("프로젝트 상태: {}, 기본값 'active' 사용", dbStatus);
                yield "active";
            }
        };

        log.debug("프로젝트 상태 매핑: {} -> {}", dbStatus, mappedStatus);
        return mappedStatus;
    }

    private Integer calculateProgress(Long projectId) {
        try {
            log.debug("프로젝트 진행률 계산 시작 - projectId: {}", projectId);

            ProjectProgressResponse response = projectProgressService.getProjectProgress(projectId);

            if (response.isSuccess() &&
                    response.getData() != null &&
                    response.getData().getOverallProgress() != null &&
                    response.getData().getOverallProgress().getPercentage() != null) {

                Integer progress = response.getData().getOverallProgress().getPercentage().intValue();
                log.debug("프로젝트 진행률 계산 성공 - projectId: {}, progress: {}%", projectId, progress);
                return progress;
            }

            log.warn("프로젝트 진행률 계산 결과가 null - projectId: {}", projectId);
            return 0;

        } catch (Exception e) {
            log.warn("프로젝트 진행률 계산 실패 - projectId: {}, error: {}", projectId, e.getMessage());
            return 0;
        }
    }

    //마지막 활동 시간 계산, 기존 엔티티들을 활용하여 가장 최근 활동 시간을 반환
    private LocalDateTime calculateLastActivity(Long projectId, LocalDateTime projectUpdatedAt) {
        LocalDateTime lastActivity = projectUpdatedAt; // 기본값은 프로젝트 업데이트 시간

        log.debug("마지막 활동 시간 계산 시작 - projectId: {}, projectUpdatedAt: {}",
                projectId, projectUpdatedAt);

        try {
            // 1. 최근 완료된 작업 시간 확인
            LocalDateTime lastTaskCompletion = projectRepository.findLastTaskCompletionTime(projectId);
            if (lastTaskCompletion != null && lastTaskCompletion.isAfter(lastActivity)) {
                lastActivity = lastTaskCompletion;
                log.debug("최근 작업 완료 시간으로 업데이트 - projectId: {}, lastTaskCompletion: {}",
                        projectId, lastTaskCompletion);
            }

            // 2. 최근 커밋 시간 확인 (지난 3개월 내)
            List<CodeCommit> recentCommits = codeCommitRepository.findByProjectIdAndCommittedAtBetween(
                    projectId,
                    LocalDateTime.now().minusMonths(3),
                    LocalDateTime.now()
            );

            if (!recentCommits.isEmpty()) {
                log.debug("최근 커밋 {} 개 발견 - projectId: {}", recentCommits.size(), projectId);

                // 가장 최근 커밋 시간
                LocalDateTime lastCommitTime = recentCommits.stream()
                        .map(CodeCommit::getCommittedAt)
                        .max(LocalDateTime::compareTo)
                        .orElse(null);

                if (lastCommitTime != null && lastCommitTime.isAfter(lastActivity)) {
                    lastActivity = lastCommitTime;
                    log.debug("최근 커밋 시간으로 업데이트 - projectId: {}, lastCommitTime: {}",
                            projectId, lastCommitTime);
                }
            } else {
                log.debug("최근 3개월 내 커밋이 없음 - projectId: {}", projectId);
            }

            log.debug("마지막 활동 시간 계산 완료 - projectId: {}, finalLastActivity: {}",
                    projectId, lastActivity);

        } catch (Exception e) {
            log.warn("마지막 활동 시간 계산 실패 - projectId: {}, 기본값 사용, error: {}",
                    projectId, e.getMessage());
        }

        return lastActivity;
    }
}

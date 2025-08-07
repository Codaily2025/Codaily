package com.codaily.project.service;

import com.codaily.project.dto.ProjectProgressResponse;
import com.codaily.project.entity.Task;
import com.codaily.project.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectProgressServiceImpl implements ProjectProgressService {

    private final TaskRepository taskRepository;

    @Override
    public ProjectProgressResponse getProjectProgress(Long projectId) {
        try {
            // 전체 작업 수 조회
            Long totalTasks = taskRepository.countByProjectId(projectId);

            // 완료된 작업 수 조회
            Long completedTasks = taskRepository.countByProjectIdAndStatus(
                    projectId, Task.Status.COMPLETED);

            // 진행률 계산 (소수점 한자리까지)
            Double percentage = calculateProgressPercentage(completedTasks, totalTasks);

            // 응답 객체 생성
            ProjectProgressResponse.OverallProgress overallProgress =
                    ProjectProgressResponse.OverallProgress.builder()
                            .percentage(percentage)
                            .completedTasks(completedTasks.intValue())
                            .totalTasks(totalTasks.intValue())
                            .build();

            ProjectProgressResponse.ProjectProgressData data =
                    ProjectProgressResponse.ProjectProgressData.builder()
                            .overallProgress(overallProgress)
                            .build();

            return ProjectProgressResponse.builder()
                    .success(true)
                    .data(data)
                    .build();

        } catch (Exception e) {
            log.error("프로젝트 진행률 조회 중 오류 발생 - projectId: {}, error: {}",
                    projectId, e.getMessage());

            return ProjectProgressResponse.builder()
                    .success(false)
                    .build();
        }
    }

    /**
     * 진행률 계산 (소수점 한자리까지)
     */
    private Double calculateProgressPercentage(Long completedTasks, Long totalTasks) {
        if (totalTasks == 0) {
            return 0.0;
        }

        double percentage = (completedTasks.doubleValue() / totalTasks.doubleValue()) * 100;
        return Math.round(percentage * 10.0) / 10.0; // 소수점 한자리까지
    }

}
package com.codaily.project.service;

import com.codaily.codereview.repository.CodeCommitRepository;
import com.codaily.project.dto.*;
import com.codaily.project.entity.*;
import com.codaily.project.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TaskDetailServiceImpl implements TaskDetailService {

    private final TaskRepository taskRepository;
    private final FeatureItemRepository featureItemRepository;
    private final CodeCommitRepository codeCommitRepository;
    private final ProjectRepository projectRepository;

    @Override
    public TaskDetailResponse getTaskDetail(Long projectId, Long taskId, Long userId) {
        try {
            log.info("작업 상세 조회 - projectId: {}, taskId: {}, userId: {}", projectId, taskId, userId);

            // 작업 조회 및 검증
            Task task = validateTaskAccess(projectId, taskId, userId);

            // 연관된 기능 정보 조회
            TaskDetailResponse.FeatureInfo featureInfo = null;
            List<TaskDetailResponse.SubTaskInfo> subTasks = null;

            if (task.getFeatureId() != null) {
                FeatureItem feature = featureItemRepository.findById(task.getFeatureId()).orElse(null);
                if (feature != null) {
                    featureInfo = convertToFeatureInfo(feature);
                    // 같은 기능의 모든 작업들 조회
                    subTasks = getSubTasks(task.getFeatureId(), taskId);
                }
            }

            // 메트릭 계산
            TaskDetailResponse.TaskMetrics metrics = calculateTaskMetrics(task);

            // 응답 데이터 구성
            TaskDetailResponse.TaskDetailData data = TaskDetailResponse.TaskDetailData.builder()
                    .taskId(task.getTaskId())
                    .title(task.getTitle())
                    .description(task.getDescription())
                    .status(task.getStatus().name())
                    .createdAt(task.getCreatedAt())
                    .updatedAt(task.getUpdatedAt())
                    .completedAt(task.getCompletedAt())
                    .feature(featureInfo)
                    .subTasks(subTasks)
                    .metrics(metrics)
                    .build();

            return TaskDetailResponse.builder()
                    .success(true)
                    .data(data)
                    .build();

        } catch (Exception e) {
            log.error("작업 상세 조회 실패 - taskId: {}, error: {}", taskId, e.getMessage(), e);
            return TaskDetailResponse.builder()
                    .success(false)
                    .message(e.getMessage())
                    .build();
        }
    }

    @Override
    @Transactional
    public TaskDetailResponse updateTask(Long projectId, Long taskId, TaskUpdateRequest request, Long userId) {
        try {
            log.info("작업 수정 - projectId: {}, taskId: {}, userId: {}", projectId, taskId, userId);

            // 작업 조회 및 검증
            Task task = validateTaskAccess(projectId, taskId, userId);

            // 작업 정보 업데이트
            updateTaskInfo(task, request);
            taskRepository.save(task);

            log.info("작업 수정 성공 - taskId: {}", taskId);

            // 업데이트된 정보 반환
            return getTaskDetail(projectId, taskId, userId);

        } catch (Exception e) {
            log.error("작업 수정 실패 - taskId: {}, error: {}", taskId, e.getMessage(), e);
            return TaskDetailResponse.builder()
                    .success(false)
                    .message(e.getMessage())
                    .build();
        }
    }

    @Override
    @Transactional
    public SubTaskCreateResponse createSubTask(Long projectId, Long taskId, SubTaskCreateRequest request, Long userId) {
        try {
            log.info("하위 작업 생성 - projectId: {}, parentTaskId: {}, userId: {}", projectId, taskId, userId);

            // 부모 작업 조회 및 검증
            Task parentTask = validateTaskAccess(projectId, taskId, userId);

            if (parentTask.getFeatureId() == null) {
                throw new IllegalArgumentException("기능에 속하지 않은 작업에는 하위 작업을 생성할 수 없습니다.");
            }

            // 새로운 하위 작업 생성
            Task subTask = Task.builder()
                    .projectId(projectId)
                    .userId(userId)
                    .featureId(parentTask.getFeatureId())
                    .title(request.getTitle())
                    .description(request.getDescription())
                    .status(request.getStatus() != null ?
                            Task.Status.valueOf(request.getStatus()) : Task.Status.PLANNED)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            Task savedSubTask = taskRepository.save(subTask);

            // 응답 데이터 구성
            SubTaskCreateResponse.SubTaskData data = SubTaskCreateResponse.SubTaskData.builder()
                    .taskId(savedSubTask.getTaskId())
                    .title(savedSubTask.getTitle())
                    .description(savedSubTask.getDescription())
                    .status(savedSubTask.getStatus().name())
                    .featureId(savedSubTask.getFeatureId())
                    .createdAt(savedSubTask.getCreatedAt())
                    .build();

            return SubTaskCreateResponse.builder()
                    .success(true)
                    .data(data)
                    .message("하위 작업이 성공적으로 생성되었습니다.")
                    .build();

        } catch (Exception e) {
            log.error("하위 작업 생성 실패 - parentTaskId: {}, error: {}", taskId, e.getMessage(), e);
            return SubTaskCreateResponse.builder()
                    .success(false)
                    .message(e.getMessage())
                    .build();
        }
    }

    @Override
    public CalendarTaskResponse getCalendarTasks(Long projectId, LocalDate date, Long userId) {
        try {
            log.info("캘린더 작업 조회 - projectId: {}, date: {}, userId: {}", projectId, date, userId);

            // 프로젝트 접근 권한 검증
            validateProjectAccess(projectId, userId);

            // 특정 날짜의 작업들 조회
            List<Task> tasks = taskRepository.findByProjectIdAndDate(projectId, date);

            // Task를 TaskInfo로 변환
            List<CalendarTaskResponse.TaskInfo> taskInfos = tasks.stream()
                    .map(this::convertToTaskInfo)
                    .collect(Collectors.toList());


            // 응답 데이터 구성
            CalendarTaskResponse.CalendarTaskData data = CalendarTaskResponse.CalendarTaskData.builder()
                    .date(date)
                    .tasks(taskInfos)
                    .build();

            return CalendarTaskResponse.builder()
                    .success(true)
                    .data(data)
                    .build();

        } catch (Exception e) {
            log.error("캘린더 작업 조회 실패 - projectId: {}, date: {}, error: {}",
                    projectId, date, e.getMessage(), e);
            return CalendarTaskResponse.builder()
                    .success(false)
                    .message(e.getMessage())
                    .build();
        }
    }

    // === Helper Methods ===

    private Task validateTaskAccess(Long projectId, Long taskId, Long userId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("작업을 찾을 수 없습니다."));

        if (!task.getProjectId().equals(projectId)) {
            throw new IllegalArgumentException("해당 프로젝트의 작업이 아닙니다.");
        }

        validateProjectAccess(projectId, userId);
        return task;
    }

    private void validateProjectAccess(Long projectId, Long userId) {
        boolean hasAccess = projectRepository.findById(projectId)
                .map(project -> project.getUser().getUserId().equals(userId))
                .orElseThrow(() -> new IllegalArgumentException("프로젝트를 찾을 수 없습니다."));

        if (!hasAccess) {
            throw new IllegalArgumentException("해당 프로젝트에 대한 접근 권한이 없습니다.");
        }
    }

    private TaskDetailResponse.FeatureInfo convertToFeatureInfo(FeatureItem feature) {
        return TaskDetailResponse.FeatureInfo.builder()
                .featureId(feature.getFeatureId())
                .title(feature.getTitle())
                .description(feature.getDescription())
                .category(feature.getCategory())
                .priorityLevel(feature.getPriorityLevel())
                .estimatedTime(feature.getEstimatedTime())
                .build();
    }

    private List<TaskDetailResponse.SubTaskInfo> getSubTasks(Long featureId, Long currentTaskId) {
        List<Task> tasks = taskRepository.findByFeatureIdOrderByCreatedAtAsc(featureId);

        return tasks.stream()
                .map(task -> TaskDetailResponse.SubTaskInfo.builder()
                        .taskId(task.getTaskId())
                        .title(task.getTitle())
                        .description(task.getDescription())
                        .status(task.getStatus().name())
                        .createdAt(task.getCreatedAt())
                        .completedAt(task.getCompletedAt())
                        .isMainTask(task.getTaskId().equals(currentTaskId))
                        .build())
                .collect(Collectors.toList());
    }

    private TaskDetailResponse.TaskMetrics calculateTaskMetrics(Task task) {
        // 기능 관련 메트릭
        int totalTasks = 1;
        int completedTasks = task.getStatus() == Task.Status.COMPLETED ? 1 : 0;
        double progressPercentage = task.getStatus() == Task.Status.COMPLETED ? 100.0 : 0.0;

        if (task.getFeatureId() != null) {
            List<Task> featureTasks = taskRepository.findByFeatureIdOrderByCreatedAtAsc(task.getFeatureId());
            totalTasks = featureTasks.size();
            completedTasks = (int) featureTasks.stream()
                    .mapToLong(t -> t.getStatus() == Task.Status.COMPLETED ? 1 : 0)
                    .sum();
            progressPercentage = totalTasks > 0 ? (double) completedTasks / totalTasks * 100.0 : 0.0;
        }

        // 커밋 수 계산
        int commitsCount = 0;
        LocalDateTime lastActivityAt = task.getUpdatedAt();

        try {
            LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
            if (task.getUserId() != null) {
                var commits = codeCommitRepository.findByUserIdAndProjectIdAndCommittedAtBetween(
                        task.getUserId(), task.getProjectId(), thirtyDaysAgo, LocalDateTime.now());
                commitsCount = commits.size();

                // 최근 커밋 시간
                if (!commits.isEmpty()) {
                    LocalDateTime lastCommitTime = commits.stream()
                            .map(commit -> commit.getCommittedAt())
                            .max(LocalDateTime::compareTo)
                            .orElse(null);

                    if (lastCommitTime != null && lastCommitTime.isAfter(lastActivityAt)) {
                        lastActivityAt = lastCommitTime;
                    }
                }
            }
        } catch (Exception e) {
            log.warn("커밋 수 조회 실패 - taskId: {}", task.getTaskId());
        }

        return TaskDetailResponse.TaskMetrics.builder()
                .totalTasksInFeature(totalTasks)
                .completedTasksInFeature(completedTasks)
                .featureProgressPercentage(Math.round(progressPercentage * 10.0) / 10.0)
                .recentCommitsCount(commitsCount)
                .lastActivityAt(lastActivityAt)
                .build();
    }

    private void updateTaskInfo(Task task, TaskUpdateRequest request) {
        boolean isModified = false;

        if (request.getTitle() != null && !request.getTitle().trim().isEmpty()) {
            task.setTitle(request.getTitle().trim());
            isModified = true;
        }

        if (request.getDescription() != null) {
            task.setDescription(request.getDescription().trim());
            isModified = true;
        }

        if (request.getStatus() != null) {
            Task.Status newStatus = Task.Status.valueOf(request.getStatus());
            Task.Status oldStatus = task.getStatus();

            if (newStatus != oldStatus) {
                task.setStatus(newStatus);
                isModified = true;

                // 상태 변경에 따른 completedAt 처리
                if (newStatus == Task.Status.COMPLETED && oldStatus != Task.Status.COMPLETED) {
                    task.setCompletedAt(LocalDateTime.now());
                } else if (newStatus != Task.Status.COMPLETED && oldStatus == Task.Status.COMPLETED) {
                    task.setCompletedAt(null);
                }
            }
        }

        if (isModified) {
            task.setUpdatedAt(LocalDateTime.now());
        }
    }

    private CalendarTaskResponse.TaskInfo convertToTaskInfo(Task task) {
        String featureTitle = null;
        String category = null;
        Integer priorityLevel = null;

        if (task.getFeatureId() != null) {
            FeatureItem feature = featureItemRepository.findById(task.getFeatureId()).orElse(null);
            if (feature != null) {
                featureTitle = feature.getTitle();
                category = feature.getCategory();
                priorityLevel = feature.getPriorityLevel();
            }
        }

        return CalendarTaskResponse.TaskInfo.builder()
                .taskId(task.getTaskId())
                .title(task.getTitle())
                .description(task.getDescription())
                .status(task.getStatus().name())
                .featureTitle(featureTitle)
                .category(category)
                .priorityLevel(priorityLevel)
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .completedAt(task.getCompletedAt())
                .build();
    }

}
package com.codaily.project.controller;

import com.codaily.auth.config.PrincipalDetails;
import com.codaily.project.dto.*;
import com.codaily.project.service.TaskDetailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Slf4j
@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class TaskDetailController {

    private final TaskDetailService taskDetailService;

    /**
     * 작업 상세 정보 조회
     * GET /api/projects/{projectId}/tasks/{taskId}/details
     */
    @GetMapping("/{projectId}/tasks/{taskId}/details")
    public ResponseEntity<TaskDetailResponse> getTaskDetail(
            @PathVariable Long projectId,
            @PathVariable Long taskId,
            @AuthenticationPrincipal PrincipalDetails userDetails) {

        log.info("작업 상세 조회 요청 - projectId: {}, taskId: {}, userId: {}",
                projectId, taskId, userDetails.getUserId());

        TaskDetailResponse response = taskDetailService.getTaskDetail(
                projectId, taskId, userDetails.getUserId());

        return response.isSuccess() ?
                ResponseEntity.ok(response) :
                ResponseEntity.badRequest().body(response);
    }

    /**
     * 작업 정보 수정
     * PUT /api/projects/{projectId}/tasks/{taskId}/details
     */
    @PutMapping("/{projectId}/tasks/{taskId}/details")
    public ResponseEntity<TaskDetailResponse> updateTask(
            @PathVariable Long projectId,
            @PathVariable Long taskId,
            @RequestBody TaskUpdateRequest request,
            @AuthenticationPrincipal PrincipalDetails userDetails) {

        log.info("작업 수정 요청 - projectId: {}, taskId: {}, userId: {}",
                projectId, taskId, userDetails.getUserId());

        TaskDetailResponse response = taskDetailService.updateTask(
                projectId, taskId, request, userDetails.getUserId());

        return response.isSuccess() ?
                ResponseEntity.ok(response) :
                ResponseEntity.badRequest().body(response);
    }

    /**
     * 작업 상태만 수정 (간단한 상태 변경용)
     * PATCH /api/projects/{projectId}/tasks/{taskId}/status
     */
    @PatchMapping("/{projectId}/tasks/{taskId}/status")
    public ResponseEntity<TaskDetailResponse> updateTaskStatus(
            @PathVariable Long projectId,
            @PathVariable Long taskId,
            @RequestParam String status,
            @AuthenticationPrincipal PrincipalDetails userDetails) {

        log.info("작업 상태 변경 요청 - projectId: {}, taskId: {}, status: {}, userId: {}",
                projectId, taskId, status, userDetails.getUserId());

        TaskUpdateRequest request = TaskUpdateRequest.builder()
                .status(status)
                .build();

        TaskDetailResponse response = taskDetailService.updateTask(
                projectId, taskId, request, userDetails.getUserId());

        return response.isSuccess() ?
                ResponseEntity.ok(response) :
                ResponseEntity.badRequest().body(response);
    }

    /**
     * 하위 작업 생성
     * POST /api/projects/{projectId}/tasks/{taskId}/subtasks
     */
    @PostMapping("/{projectId}/tasks/{taskId}/subtasks")
    public ResponseEntity<SubTaskCreateResponse> createSubTask(
            @PathVariable Long projectId,
            @PathVariable Long taskId,
            @RequestBody SubTaskCreateRequest request,
            @AuthenticationPrincipal PrincipalDetails userDetails) {

        log.info("하위 작업 생성 요청 - projectId: {}, parentTaskId: {}, userId: {}",
                projectId, taskId, userDetails.getUserId());

        SubTaskCreateResponse response = taskDetailService.createSubTask(
                projectId, taskId, request, userDetails.getUserId());

        return response.isSuccess() ?
                ResponseEntity.ok(response) :
                ResponseEntity.badRequest().body(response);
    }

    /**
     * 캘린더 특정 날짜의 작업 정보 조회
     * GET /api/projects/{projectId}/calendar/{date}
     */
    @GetMapping("/{projectId}/calendar/{date}")
    public ResponseEntity<CalendarTaskResponse> getCalendarTasks(
            @PathVariable Long projectId,
            @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
            @AuthenticationPrincipal PrincipalDetails userDetails) {

        log.info("캘린더 작업 조회 요청 - projectId: {}, date: {}, userId: {}",
                projectId, date, userDetails.getUserId());

        CalendarTaskResponse response = taskDetailService.getCalendarTasks(
                projectId, date, userDetails.getUserId());

        return response.isSuccess() ?
                ResponseEntity.ok(response) :
                ResponseEntity.badRequest().body(response);
    }
}
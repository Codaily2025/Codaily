package com.codaily.project.service;

import com.codaily.project.dto.*;
import java.time.LocalDate;

public interface TaskDetailService {

    //작업 상세 정보 조회
    TaskDetailResponse getTaskDetail(Long projectId, Long taskId, Long userId);

    //작업 정보 수정
    TaskDetailResponse updateTask(Long projectId, Long taskId, TaskUpdateRequest request, Long userId);

    //하위 작업 생성 (같은 FeatureItem에 새로운 Task 생성)
    SubTaskCreateResponse createSubTask(Long projectId, Long taskId, SubTaskCreateRequest request, Long userId);

    //캘린더 특정 날짜의 작업 정보 조회
    CalendarTaskResponse getCalendarTasks(Long projectId, LocalDate date, Long userId);
}
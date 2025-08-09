package com.codaily.management.controller;

import com.codaily.auth.config.PrincipalDetails;
import com.codaily.management.dto.CalendarResponse;
import com.codaily.management.dto.TodayScheduleResponse;
import com.codaily.management.service.CalendarService;
import com.codaily.project.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.time.YearMonth;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ScheduleController {

    private final CalendarService calendarService;
    private final ProjectService projectService;

    @GetMapping("/calendar")
    @Operation(summary= "사용자 전체 프로젝트 캘린더 조회", description = "사용자의 모든 프로젝트 월별 일정 조회")
    public ResponseEntity<CalendarResponse> getAllProjectsCalendar(
            @AuthenticationPrincipal PrincipalDetails userDetails,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth yearMonth
    ){
        if(yearMonth == null){
            yearMonth = YearMonth.now();
        }
        Long userId = userDetails.getUserId();
        CalendarResponse response = calendarService.getAllProjectsCalendar(userId, yearMonth);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/projects/{projectId}/calendar")
    @Operation(summary = "프로젝트별 calendar 조회", description = "월별 일정 조회")
    public ResponseEntity<CalendarResponse> getCalendar(
            @PathVariable Long projectId,
            @AuthenticationPrincipal PrincipalDetails userDetails,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth yearMonth
    ) throws AccessDeniedException {
        if(!projectService.isProjectOwner(userDetails.getUserId(), projectId)){
            throw new AccessDeniedException("접근 권한이 없습니다.");
        }

        if(yearMonth == null){
            yearMonth = YearMonth.now();
        }
        CalendarResponse response = calendarService.getCalendar(projectId, yearMonth);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/today")
    @Operation(summary = "사용자 전체 프로젝트 중 오늘 할 일 조회")
    public ResponseEntity<TodayScheduleResponse> getTodayScheduleForUser(
            @AuthenticationPrincipal PrincipalDetails userDetails
    ){
        Long userId = userDetails.getUserId();
        TodayScheduleResponse response = calendarService.getTodayScheduleForUser(userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/projects/{projectId}/today")
    @Operation(summary = "프로젝트 보드에서 오늘 할 일 조회")
    public ResponseEntity<TodayScheduleResponse> getTodayScheduleForProject(
            @PathVariable Long projectId,
            @AuthenticationPrincipal PrincipalDetails userDetails
    ) throws AccessDeniedException {
        if(!projectService.isProjectOwner(userDetails.getUserId(), projectId)){
            throw new AccessDeniedException("접근 권한이 없습니다.");
        }
        TodayScheduleResponse response = calendarService.getTodayScheduleForProject(projectId);
        return ResponseEntity.ok(response);
    }
}

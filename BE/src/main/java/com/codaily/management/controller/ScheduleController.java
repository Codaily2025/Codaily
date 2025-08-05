package com.codaily.management.controller;

import com.codaily.management.dto.CalendarResponse;
import com.codaily.management.dto.TodayScheduleResponse;
import com.codaily.management.service.CalendarServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ScheduleController {

    private final CalendarServiceImpl calendarServiceImpl;

    @GetMapping("/users/{userId}/calendar")
    @Operation(summary= "사용자 전체 프로젝트 캘린더 조회", description = "사용자의 모든 프로젝트 월별 일정 조회")
    public ResponseEntity<CalendarResponse> getAllProjectsCalendar(
            @PathVariable Long userId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth yearMonth
    ){
        if(yearMonth == null){
            yearMonth = YearMonth.now();
        }
        CalendarResponse response = calendarServiceImpl.getAllProjectsCalendar(userId, yearMonth);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/projects/{projectId}/calendar")
    @Operation(summary = "프로젝트별 calendar 조회", description = "월별 일정 조회")
    public ResponseEntity<CalendarResponse> getCalendar(
            @PathVariable Long projectId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth yearMonth
    ){
        if(yearMonth == null){
            yearMonth = YearMonth.now();
        }
        CalendarResponse response = calendarServiceImpl.getCalendar(projectId, yearMonth);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/users/{userId}/today")
    @Operation(summary = "사용자 전체 프로젝트 중 오늘 할 일 조회")
    public ResponseEntity<TodayScheduleResponse> getTodayScheduleForUser(
            @PathVariable Long userId
    ){
        TodayScheduleResponse response = calendarServiceImpl.getTodayScheduleForUser(userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/projects/{projectId}/today")
    @Operation(summary = "프로젝트 보드에서 오늘 할 일 조회")
    public ResponseEntity<TodayScheduleResponse> getTodayScheduleForProject(
            @PathVariable Long projectId
    ){
        TodayScheduleResponse response = calendarServiceImpl.getTodayScheduleForProject(projectId);
        return ResponseEntity.ok(response);
    }
}

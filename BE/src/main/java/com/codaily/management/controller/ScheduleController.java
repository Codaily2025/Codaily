package com.codaily.management.controller;

import com.codaily.management.dto.CalendarResponse;
import com.codaily.management.service.CalendarServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ScheduleController {

    private final CalendarServiceImpl calendarServiceImpl;

    @GetMapping("/{projectId}/calendar")
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
}

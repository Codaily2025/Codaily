package com.codaily.management.service;

import com.codaily.management.dto.CalendarResponse;
import com.codaily.management.dto.TodayScheduleResponse;

import java.time.YearMonth;

public interface CalendarService {
    CalendarResponse getAllProjectsCalendar(Long userId, YearMonth yearMonth);
    CalendarResponse getCalendar(Long projectId, YearMonth yearMonth);
    TodayScheduleResponse getTodayScheduleForUser(Long userId);
    TodayScheduleResponse getTodayScheduleForProject(Long projectId);
}

package com.codaily.management.service;

import com.codaily.management.dto.CalendarResponse;
import com.codaily.management.dto.TodayScheduleResponse;

import java.time.YearMonth;

public interface CalendarService {
    public CalendarResponse getAllProjectsCalendar(Long userId, YearMonth yearMonth);
    public CalendarResponse getCalendar(Long projectId, YearMonth yearMonth);
    public TodayScheduleResponse getTodayScheduleForUser(Long userId);
    public TodayScheduleResponse getTodayScheduleForProject(Long projectId);
}

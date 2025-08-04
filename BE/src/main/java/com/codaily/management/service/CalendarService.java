package com.codaily.management.service;

import com.codaily.management.dto.CalendarResponse;

import java.time.YearMonth;

public interface CalendarService {
    CalendarResponse getAllProjectsCalendar(Long userId, YearMonth yearMonth);
    CalendarResponse getCalendar(Long projectId, YearMonth yearMonth);
}

package com.codaily.management.service;

import com.codaily.management.dto.CalendarResponse;

import java.time.YearMonth;

public interface CalendarService {
    public CalendarResponse getCalendar(Long projectId, YearMonth yearMonth);
}

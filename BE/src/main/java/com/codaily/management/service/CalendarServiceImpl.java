package com.codaily.management.service;

import com.codaily.management.dto.CalendarResponse;
import com.codaily.management.repository.FeatureItemSchedulesRepository;
import com.codaily.project.entity.FeatureItemSchedules;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

@Service
@RequiredArgsConstructor
public class CalendarServiceImpl implements CalendarService{

    private final FeatureItemSchedulesRepository scheduleRepository;

    @Override
    public CalendarResponse getCalendar(Long projectId, YearMonth yearMonth) {

        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        List<FeatureItemSchedules> schedules = scheduleRepository
                .findByProjectAndDateRange(projectId, startDate, endDate);

        List<CalendarResponse.CalendarEvent> events = new ArrayList<>();
        for (FeatureItemSchedules schedule : schedules) {
            CalendarResponse.CalendarEvent event = this.convertToCalendarEvent(schedule);
            events.add(event);
        }

        return CalendarResponse.builder()
                .baseDate(startDate)
                .startDate(startDate)
                .endDate(endDate)
                .events(events)
                .build();
    }
    private CalendarResponse.CalendarEvent convertToCalendarEvent(FeatureItemSchedules schedule) {
        return CalendarResponse.CalendarEvent.builder()
                .scheduleId(schedule.getScheduleId())
                .featureId(schedule.getFeatureItem().getFeatureId())
                .featureTitle(schedule.getFeatureItem().getTitle())
                .featureDescription(schedule.getFeatureItem().getDescription())
                .scheduleDate(schedule.getScheduleDate())
                .allocatedHours(schedule.getAllocatedHours())
                .category(schedule.getFeatureItem().getCategory())
                .priorityLevel(schedule.getFeatureItem().getPriorityLevel())
                .status(schedule.getFeatureItem().getStatus())
                .build();
    }
}

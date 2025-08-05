package com.codaily.management.service;

import com.codaily.management.dto.CalendarResponse;
import com.codaily.management.exception.CalendarDataException;
import com.codaily.global.exception.ProjectNotFoundException;
import com.codaily.management.repository.FeatureItemSchedulesRepository;
import com.codaily.management.entity.FeatureItemSchedule;
import com.codaily.project.entity.FeatureItem;
import com.codaily.project.entity.Project;
import com.codaily.project.repository.ProjectRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CalendarServiceImpl implements CalendarService{

    private final FeatureItemSchedulesRepository scheduleRepository;
    private final ProjectRepository projectRepository;

    @Override
    public CalendarResponse getAllProjectsCalendar(Long userId, YearMonth yearMonth) {
        if(userId == null || userId <= 0){
            throw new IllegalArgumentException("사용자 ID는 양수여야 합니다.");
        }

        if(yearMonth == null) {
            throw new IllegalArgumentException("조회할 년월을 입력해주세요.");
        }

        try{
            LocalDate startDate = yearMonth.atDay(1);
            LocalDate endDate = yearMonth.atEndOfMonth();

            // 사용자의 모든 프로젝트 조회
            List<Project> userProjects = projectRepository.findByUser_UserId(userId);

            List<CalendarResponse.CalendarEvent> events = new ArrayList<>();

            // 각 프로젝트별로 스케줄 조회
            for (Project project : userProjects) {
                List<FeatureItemSchedule> schedules = scheduleRepository
                        .findByProjectAndDateRange(project.getProjectId(), startDate, endDate);

                for (FeatureItemSchedule schedule : schedules) {
                    CalendarResponse.CalendarEvent event = this.convertToCalendarEvent(schedule);
                    events.add(event);
                }
            }

            return CalendarResponse.builder()
                    .baseDate(startDate)
                    .startDate(startDate)
                    .endDate(endDate)
                    .events(events)
                    .build();
        }
        catch(Exception e){
            log.error("사용자 전체 캘린더 조회 중 오류 발생 - 사용자 ID: {}, 년월: {}", userId, yearMonth, e);
            throw new CalendarDataException("캘린더 데이터 조회 중 오류가 발생했습니다.", e);
        }
    }

    @Override
    public CalendarResponse getCalendar(Long projectId, YearMonth yearMonth) {

        if(projectId == null || projectId <= 0){
            throw new IllegalArgumentException("프로젝트 ID는 양수여야 합니다.");
        }

        if(!projectRepository.existsByProjectId(projectId)){
            throw new ProjectNotFoundException(projectId);
        }
        if(yearMonth == null) {
            throw new IllegalArgumentException("조회할 년월을 입력해주세요.");
        }
        try{
            LocalDate startDate = yearMonth.atDay(1);
            LocalDate endDate = yearMonth.atEndOfMonth();

            List<FeatureItemSchedule> schedules = scheduleRepository
                    .findByProjectAndDateRange(projectId, startDate, endDate);

            List<CalendarResponse.CalendarEvent> events = new ArrayList<>();
            for (FeatureItemSchedule schedule : schedules) {
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
        catch(Exception e){
            log.error("캘린더 조회 중 오류 발생 - 프로젝트 ID: {}, 년월: {}", projectId, yearMonth, e);
            throw new CalendarDataException("캘린더 데이터 조회 중 오류가 발생했습니다.", e);
        }
    }
    private CalendarResponse.CalendarEvent convertToCalendarEvent(FeatureItemSchedule schedule) {
        FeatureItem featureItem = schedule.getFeatureItem();
        Project project = featureItem.getProject();

        return CalendarResponse.CalendarEvent.builder()
                .projectId(project.getProjectId())
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

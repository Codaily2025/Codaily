package com.codaily.project.service;

import com.codaily.codereview.repository.FeatureItemChecklistRepository;
import com.codaily.global.exception.ProjectNotFoundException;
import com.codaily.management.entity.DaysOfWeek;
import com.codaily.management.entity.FeatureItemSchedule;
import com.codaily.management.repository.DaysOfWeekRepository;
import com.codaily.management.repository.FeatureItemSchedulesRepository;
import com.codaily.project.entity.FeatureItem;
import com.codaily.project.entity.Project;
import com.codaily.project.repository.FeatureItemRepository;
import com.codaily.project.repository.ProjectRepository;
import com.codaily.project.repository.ScheduleRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduleServiceImpl implements ScheduleService {

    private final ProjectRepository projectRepository;
    private final FeatureItemRepository featureItemRepository;
    private final DaysOfWeekRepository daysOfWeekRepository;
    private final FeatureItemSchedulesRepository featureItemSchedulesRepository;
    private final FeatureItemChecklistRepository checklistRepository;
    private final ScheduleRepository scheduleRepository;

    @Override
    @Transactional
    public void rescheduleProject(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException("프로젝트를 찾을 수 없습니다. ID: " + projectId));

        // 전체 TODO 기능들 조회
        List<FeatureItem> features = getSchedulableFeatures(projectId);

        if (features.isEmpty()) {
            log.info("재스케줄링할 기능이 없습니다.");
            return;
        }

        // 기존 스케줄 모두 삭제
        deleteExistingSchedules(features);

        // 프로젝트 시작일부터 다시 스케줄링
        LocalDate today = LocalDate.now();
        LocalDate projectStartDate = project.getStartDate();

        LocalDate startDate = (projectStartDate != null && projectStartDate.isAfter(today))
                ? projectStartDate : today;

        // 전체 다시 스케줄링
        scheduleFeatures(features, startDate, projectId);

        log.info("프로젝트 전체 재스케줄링 완료 - 기능 수: {}", features.size());
    }

    @Override
    @Transactional
    public void scheduleProjectInitially(Long projectId) {
        Project project = projectRepository.findByProjectId(projectId)
                .orElseThrow(() -> new ProjectNotFoundException(projectId));

        List<FeatureItem> features = featureItemRepository.findByProject_ProjectId(projectId)
                .stream()
                .filter(feature -> feature.getParentFeature() != null)
                .collect(Collectors.toList());

        LocalDate startDate = project.getStartDate() != null ? project.getStartDate() : LocalDate.now();
        scheduleFeatures(features, startDate, projectId);
    }

    @Override
    @Transactional
    public void rescheduleFromFeatureCreate(Long projectId, FeatureItem newFeature) {
        rescheduleProjectWithPriority(projectId, newFeature, null, newFeature.getPriorityLevel());
    }

    @Override
    @Transactional
    public void rescheduleFromFeatureUpdate(Long projectId, FeatureItem updatedFeature,
                                            Integer oldPriorityLevel, Double oldEstimatedTime) {

        Integer newPriorityLevel = updatedFeature.getPriorityLevel();
        Double newEstimatedTime = updatedFeature.getEstimatedTime();

        // 우선순위나 예상시간이 변경되지 않았으면 재스케줄링 불필요
        if (Objects.equals(oldPriorityLevel, newPriorityLevel) &&
                Objects.equals(oldEstimatedTime, newEstimatedTime)) {
            return;
        }

        rescheduleProjectWithPriority(projectId, updatedFeature, oldPriorityLevel, newPriorityLevel);
    }

    @Override
    @Transactional
    public void rescheduleFromFeatureDelete(Long projectId, FeatureItem deletedFeature) {
        rescheduleProjectWithPriority(projectId, null, deletedFeature.getPriorityLevel(), null);
    }

    @Override
    public void updateDailyStatus() {
        List<Project> activeProjects = projectRepository.findActiveProjects();

        for (Project project : activeProjects) {
            updateInProgressEstimatedTime(project.getProjectId());
            handleOverdueFeatures(project.getProjectId());
            startTodayFeatures(project.getProjectId(), LocalDate.now());
        }
    }

    private void rescheduleProjectWithPriority(Long projectId, FeatureItem changedFeature,
                                               Integer oldPriority, Integer newPriority) {

        // 영향받는 우선순위 범위 계산
        Integer fromPriority = calculateFromPriority(oldPriority, newPriority);

        // 영향받는 기능들 조회
        List<FeatureItem> featuresToReschedule = getAffectedFeatures(projectId, fromPriority);

        // 변경된 기능 포함
        if (changedFeature != null && !featuresToReschedule.contains(changedFeature)) {
            featuresToReschedule.add(changedFeature);
        }

        if (featuresToReschedule.isEmpty()) {
            log.info("재스케줄링할 기능이 없습니다.");
            return;
        }

        deleteExistingSchedules(featuresToReschedule);
        LocalDate startDate = calculateStartDate(projectId, fromPriority);
        scheduleFeatures(featuresToReschedule, startDate, projectId);

        log.info("스마트 재스케줄링 완료 - 대상 기능 수: {}", featuresToReschedule.size());
    }

    private Integer calculateFromPriority(Integer oldPriority, Integer newPriority) {
        if (oldPriority == null && newPriority == null) return null;
        if (oldPriority == null) return newPriority;
        if (newPriority == null) return oldPriority;
        return Math.min(oldPriority, newPriority);
    }

    private List<FeatureItem> getAffectedFeatures(Long projectId, Integer fromPriority) {
        if (fromPriority == null) {
            return getSchedulableFeatures(projectId);
        }

        return featureItemRepository.findSchedulableFeaturesByPriorityFrom(projectId, fromPriority);
    }

    private LocalDate calculateStartDate(Long projectId, Integer fromPriority) {
        LocalDate today = LocalDate.now();

        if (fromPriority == null || fromPriority <= 1) {
            Project project = projectRepository.findById(projectId).orElseThrow();
            LocalDate projectStartDate = project.getStartDate();
            return (projectStartDate != null && projectStartDate.isAfter(today)) ? projectStartDate : today;
        }

        LocalDate lastEndDate = featureItemSchedulesRepository.findLastScheduleDateByProjectIdAndPriorityLess(projectId, fromPriority);
        return (lastEndDate != null && lastEndDate.isAfter(today)) ? lastEndDate.plusDays(1) : today;
    }

    private void scheduleFeatures(List<FeatureItem> features, LocalDate startDate, Long projectId) {
        Project project = projectRepository.findByProjectId(projectId)
                .orElseThrow(() -> new IllegalArgumentException("해당 프로젝트가 존재하지 않습니다."));

        LocalDate projectEndDate = project.getEndDate();
        Map<String, Integer> weeklySchedule = getWeeklyScheduleMap(projectId);

        PriorityQueue<FeatureItem> remainingFeatures = new PriorityQueue<>(
                Comparator.comparing(FeatureItem::getPriorityLevel, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(FeatureItem::getEstimatedTime)
                        .thenComparing(FeatureItem::getFeatureId)
        );
        remainingFeatures.addAll(features);

        List<FeatureItem> unscheduledFeatures = new ArrayList<>();

        while (!remainingFeatures.isEmpty()) {
            FeatureItem currentFeature = remainingFeatures.poll();
            double remainingHours = scheduleWithinProject(currentFeature, startDate, weeklySchedule, projectId, projectEndDate);

            if (remainingHours != 0) {
                currentFeature.setRemainingTime(remainingHours);
                unscheduledFeatures.add(currentFeature);
            }
        }

        if (!unscheduledFeatures.isEmpty()) {
            LocalDate extendedStartDate = projectEndDate.plusDays(1);
            scheduleWithWeeklyPattern(unscheduledFeatures, extendedStartDate, weeklySchedule, projectId);
        }
    }

    private double scheduleWithinProject(FeatureItem feature, LocalDate startDate, Map<String, Integer> weeklySchedule, Long projectId, LocalDate projectEndDate) {
        double remainingHours = feature.getEstimatedTime();
        LocalDate currentDate = startDate;

        while (remainingHours > 0 && !currentDate.isAfter(projectEndDate)) {
            double availableHours = getAvailableHours(projectId, currentDate, weeklySchedule);

            if (availableHours > 0) {
                double allocatedHours = getAllocatedHours(projectId, currentDate);
                double freeHours = Math.max(0, availableHours - allocatedHours);

                if (freeHours > 0) {
                    double hoursToSchedule = Math.min(remainingHours, freeHours);

                    FeatureItemSchedule schedule = FeatureItemSchedule.builder()
                            .featureItem(feature)
                            .scheduleDate(currentDate)
                            .allocatedHours(hoursToSchedule)
                            .withinProjectPeriod(true)
                            .build();

                    featureItemSchedulesRepository.save(schedule);
                    remainingHours -= hoursToSchedule;
                }
            }
            currentDate = currentDate.plusDays(1);
        }
        return remainingHours;
    }

    private void scheduleWithWeeklyPattern(List<FeatureItem> features, LocalDate startDate, Map<String, Integer> weeklySchedule, Long projectId) {
        LocalDate currentDate = startDate;

        for (FeatureItem feature : features) {
            currentDate = scheduleFeatureWithWeeklyPattern(feature, currentDate, weeklySchedule, projectId);
        }
    }

    private LocalDate scheduleFeatureWithWeeklyPattern(FeatureItem feature, LocalDate startDate, Map<String, Integer> weeklySchedule, Long projectId) {
        double remainingHours = feature.getRemainingTime();
        LocalDate currentDate = startDate;
        int maxDays = 365;
        int dayCount = 0;

        while (remainingHours > 0 && dayCount < maxDays) {
            String dayName = currentDate.getDayOfWeek().toString();
            double availableHours = weeklySchedule.getOrDefault(dayName, 0);

            if (availableHours > 0) {
                double allocatedHours = getAllocatedHours(projectId, currentDate);
                double freeHours = Math.max(0, availableHours - allocatedHours);

                if (freeHours > 0) {
                    double hoursToSchedule = Math.min(remainingHours, freeHours);

                    FeatureItemSchedule schedule = FeatureItemSchedule.builder()
                            .featureItem(feature)
                            .scheduleDate(currentDate)
                            .allocatedHours(hoursToSchedule)
                            .withinProjectPeriod(false)
                            .build();

                    featureItemSchedulesRepository.save(schedule);
                    remainingHours -= hoursToSchedule;

                    if (remainingHours == 0) {
                        double totalUsed = allocatedHours + hoursToSchedule;
                        return (totalUsed < availableHours) ? currentDate : currentDate.plusDays(1);
                    }
                }
            }

            currentDate = currentDate.plusDays(1);
            dayCount++;
        }

        if (remainingHours > 0) {
            log.warn("스케줄링 미완료 - 기능 ID: {}, 남은 시간: {}h", feature.getFeatureId(), remainingHours);
        }
        return currentDate;
    }

    private double getAvailableHours(Long projectId, LocalDate date, Map<String, Integer> weeklySchedule) {
        boolean isWorkingDay = scheduleRepository.existsByProject_ProjectIdAndScheduledDate(projectId, date);
        if (!isWorkingDay) return 0;

        String dayName = date.getDayOfWeek().toString();
        return weeklySchedule.getOrDefault(dayName, 0);
    }

    private double getAllocatedHours(Long projectId, LocalDate date) {
        List<FeatureItemSchedule> schedules = featureItemSchedulesRepository
                .findByFeatureItem_Project_ProjectIdAndScheduleDate(projectId, date);
        return schedules.stream().mapToDouble(FeatureItemSchedule::getAllocatedHours).sum();
    }

    private Map<String, Integer> getWeeklyScheduleMap(Long projectId) {
        List<DaysOfWeek> daysOfWeekList = daysOfWeekRepository.findByProject_ProjectId(projectId);
        return daysOfWeekList.stream()
                .collect(Collectors.toMap(DaysOfWeek::getDateName, DaysOfWeek::getHours));
    }

    private void deleteExistingSchedules(List<FeatureItem> features) {
        List<Long> featureIds = features.stream()
                .map(FeatureItem::getFeatureId)
                .collect(Collectors.toList());
        featureItemSchedulesRepository.deleteByFeatureItemFeatureIdIn(featureIds);
        log.debug("기존 스케줄 삭제 완료 - 기능 수: {}", featureIds.size());
    }

    private List<FeatureItem> getSchedulableFeatures(Long projectId) {
        List<FeatureItem> allFeatures = featureItemRepository.findByProject_ProjectId(projectId);
        return allFeatures.stream()
                .filter(f -> "TODO".equals(f.getStatus()))
                .filter(f -> f.getEstimatedTime() != null && f.getEstimatedTime() > 0)
                .filter(f -> f.getParentFeature() != null)
                .collect(Collectors.toList());
    }

    private void updateInProgressEstimatedTime(Long projectId) {
        List<FeatureItem> inProgressFeatures = featureItemRepository
                .findByStatusAndProject_ProjectId("IN_PROGRESS", projectId);

        for (FeatureItem feature : inProgressFeatures) {
            double remainingTime = calculateRemainingTimeFromChecklist(feature.getFeatureId());
            if (remainingTime == 0) {
                feature.setStatus("DONE");
            } else {
                feature.setEstimatedTime(remainingTime);
            }
            featureItemRepository.save(feature);
        }
    }

    private void startTodayFeatures(Long projectId, LocalDate today) {
        List<FeatureItem> todayFeatures = featureItemRepository.findTodayStartFeatures(projectId, today);
        for (FeatureItem feature : todayFeatures) {
            if ("TODO".equals(feature.getStatus())) {
                feature.setStatus("IN_PROGRESS");
                featureItemRepository.save(feature);
            }
        }
    }

    private void handleOverdueFeatures(Long projectId) {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        List<FeatureItem> overdueFeatures = featureItemRepository.findOverdueFeatures(projectId, yesterday);

        for (FeatureItem feature : overdueFeatures) {
            if (!"DONE".equals(feature.getStatus())) {
                double remainingTime = feature.getEstimatedTime();
                if (remainingTime == 0) {
                    feature.setStatus("DONE");
                } else {
                    feature.setStatus("TODO");
                }
                featureItemRepository.save(feature);
            }
        }

        List<FeatureItem> allSchedulable = getSchedulableFeatures(projectId);
        if (!allSchedulable.isEmpty()) {
            deleteExistingSchedules(allSchedulable);
            Project project = projectRepository.findById(projectId).orElseThrow();
            LocalDate startDate = (project.getStartDate() != null && project.getStartDate().isAfter(LocalDate.now()))
                    ? project.getStartDate() : LocalDate.now();
            scheduleFeatures(allSchedulable, startDate, projectId);
        }
    }

    private double calculateRemainingTimeFromChecklist(Long featureItemId) {
        FeatureItem feature = featureItemRepository.findById(featureItemId)
                .orElseThrow(() -> new EntityNotFoundException("FeatureItem not found: " + featureItemId));

        int totalChecklists = checklistRepository.countTotalByFeatureId(featureItemId);
        if (totalChecklists == 0) {
            return feature.getEstimatedTime();
        }

        Double progressRate = checklistRepository.getProgressRateByFeatureId(featureItemId)
                .orElseThrow(() -> new EntityNotFoundException("FeatureItem not found: " + featureItemId));

        if (progressRate == null || progressRate == 0.0) {
            return feature.getEstimatedTime();
        }

        double calculatedTime = feature.getEstimatedTime() * (100 - progressRate) / 100;
        double remainingTime = Math.ceil(calculatedTime * 2) / 2.0;
        return Math.max(remainingTime, 0);
    }
}
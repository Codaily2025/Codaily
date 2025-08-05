package com.codaily.project.service;

import com.codaily.global.exception.ProjectNotFoundException;
import com.codaily.management.entity.DaysOfWeek;
import com.codaily.management.entity.FeatureItemSchedule;
import com.codaily.management.repository.DaysOfWeekRepository;
import com.codaily.management.repository.FeatureItemSchedulesRepository;
import com.codaily.project.dto.FeatureItemCreateRequest;
import com.codaily.project.dto.FeatureItemResponse;
import com.codaily.project.dto.FeatureItemUpdateRequest;
import com.codaily.project.entity.FeatureItem;
import com.codaily.project.entity.Project;
import com.codaily.project.entity.Specification;
import com.codaily.project.exception.FeatureNotFoundException;
import com.codaily.project.exception.SpecificationNotFoundException;
import com.codaily.project.repository.FeatureItemRepository;
import com.codaily.project.repository.ProjectRepository;
import com.codaily.project.repository.ScheduleRepository;
import com.codaily.project.repository.SpecificationRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class FeatureItemServiceImpl implements FeatureItemService {

    private final ProjectRepository projectRepository;
    private final SpecificationRepository specificationRepository;
    private final FeatureItemRepository featureItemRepository;
    private final FeatureItemSchedulesRepository featureItemScheduleRepository;
    private final DaysOfWeekRepository daysOfWeekRepository;
    private final ScheduleRepository scheduleRepository;

    @Override
    public FeatureItemResponse createFeature(Long projectId, FeatureItemCreateRequest featureItem) {
        if (projectId == null || featureItem == null) {
            throw new IllegalArgumentException("프로젝트 ID와 생성 정보는 필수입니다.");
        }
        if (featureItem.getTitle() == null || featureItem.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("기능 제목은 필수입니다.");
        }

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException(projectId));

        FeatureItem feature = FeatureItem.builder()
                .title(featureItem.getTitle().trim())
                .description(featureItem.getDescription())
                .field(featureItem.getField())
                .category(featureItem.getCategory())
                .priorityLevel(featureItem.getPriorityLevel())
                .estimatedTime(featureItem.getEstimatedTime())
                .isCustom(true)
                .isSelected(true)
                .isReduced(false)
                .project(project)
                .build();

        // 스펙 설정
        if (featureItem.getSpecificationId() != null) {
            Specification specification = specificationRepository.findById(featureItem.getSpecificationId())
                    .orElseThrow(() -> new SpecificationNotFoundException(featureItem.getSpecificationId()));
            feature.setSpecification(specification);
        }

        // 부모 기능 설정
        if (featureItem.getParentFeatureId() != null) {
            FeatureItem parentFeature = featureItemRepository.findByProject_ProjectIdAndFeatureId(
                            projectId, featureItem.getParentFeatureId())
                    .orElseThrow(() -> new FeatureNotFoundException(featureItem.getParentFeatureId()));
            feature.setParentFeature(parentFeature);
        }

        FeatureItem savedFeature = featureItemRepository.save(feature);

        if (featureItem.getEstimatedTime() != null && featureItem.getEstimatedTime() > 0) {
            rescheduleProject(projectId);
        }

        log.info("기능 생성 완료 - 프로젝트 ID: {}, 기능 ID: {}", projectId, savedFeature.getFeatureId());

        return convertToResponseDto(feature);
    }

    @Override
    public FeatureItemResponse getFeature(Long projectId, Long featureId) {
        if (projectId == null || featureId == null) {
            throw new IllegalArgumentException("프로젝트 ID와 기능 ID는 필수입니다.");
        }

        FeatureItem feature = featureItemRepository.findByProject_ProjectIdAndFeatureId(projectId, featureId)
                .orElseThrow(() -> new FeatureNotFoundException(featureId));

        return convertToResponseDto(feature);
}

    @Override
    public FeatureItemResponse updateFeature(Long projectId, Long featureId, FeatureItemUpdateRequest update) {
        if (projectId == null || featureId == null || update == null) {
            throw new IllegalArgumentException();
        }

        if (!projectRepository.existsById(projectId)) {
            throw new ProjectNotFoundException(projectId);
        }

        FeatureItem feature = featureItemRepository.findByProject_ProjectIdAndFeatureId(projectId, featureId)
                .orElseThrow(() -> new FeatureNotFoundException(featureId));


        boolean needsRescheduling = false;

        if (update.getTitle() != null) {
            if (update.getTitle().trim().isEmpty()) {
                throw new IllegalArgumentException("기능 제목은 비어있을 수 없습니다.");
            }
            feature.setTitle(update.getTitle().trim());
        }

        if (update.getDescription() != null) {
            feature.setDescription(update.getDescription());
        }
        if (update.getField() != null) {
            feature.setField(update.getField());
        }
        if (update.getCategory() != null) {
            feature.setCategory(update.getCategory());
        }
        if (update.getStatus() != null) {
            feature.setStatus(update.getStatus());
        }
        if (update.getPriorityLevel() != null) {
            Integer oldPriority = feature.getPriorityLevel();
            feature.setPriorityLevel(update.getPriorityLevel());
            if (!java.util.Objects.equals(oldPriority, update.getPriorityLevel())) {
                needsRescheduling = true;
            }
        }
        if (update.getEstimatedTime() != null) {
            if (update.getEstimatedTime() < 0) {
                throw new IllegalArgumentException("예상 시간은 0 이상이어야 합니다.");
            }
            Double oldTime = feature.getEstimatedTime();
            feature.setEstimatedTime(update.getEstimatedTime());
            if (!java.util.Objects.equals(oldTime, update.getEstimatedTime())) {
                needsRescheduling = true;
            }
        }
        if (update.getIsReduced() != null) {
            feature.setIsReduced(update.getIsReduced());
        }

        if(needsRescheduling) rescheduleProject(projectId);

        log.info("기능 수정 완료 - 프로젝트 ID: {}, 기능 ID: {}", projectId, featureId);
        return convertToResponseDto(feature);
    }

    @Override
    public void deleteFeature(Long projectId, Long featureId) {
        if (projectId == null || featureId == null) {
            throw new IllegalArgumentException("프로젝트 ID와 기능 ID는 필수입니다.");
        }

        FeatureItem feature = featureItemRepository.findByProject_ProjectIdAndFeatureId(projectId, featureId)
                .orElseThrow(() -> new FeatureNotFoundException(featureId));

        // 연관된 스케줄들 삭제
        if (featureItemScheduleRepository != null) {
            featureItemScheduleRepository.deleteByFeatureItemFeatureId(featureId);
            log.info("기능 관련 스케줄 삭제 완료 - 기능 ID: {}", featureId);
        }

        // 하위 기능들의 부모 참조 제거
        feature.getChildFeatures().forEach(child -> child.setParentFeature(null));

        featureItemRepository.delete(feature);
        rescheduleProject(projectId);
        log.info("기능 삭제 완료 - 프로젝트 ID: {}, 기능 ID: {}", projectId, featureId);
    }

    @Override
    public void rescheduleProject(Long projectId) {
        // 재스케줄링 가능한 기능들을 조회
        List<FeatureItem> features = getSchedulableFeatures(projectId);

        if (features.isEmpty()) {
            log.info("재스케줄링할 기능이 없습니다.");
            return;
        }

        // 기존 스케줄 삭제 (재스케줄링 대상만)
        deleteExistingSchedules(features);

        LocalDate startDate = LocalDate.now();

        // 우선순위 순으로 다시 스케줄링
        scheduleFeatures(features, startDate, projectId);

        log.info("프로젝트 재스케줄링 완료 - 기능 수: {}", features.size());
    }

    @Override
    public void scheduleProjectInitially(Long projectId) {
        Project project = projectRepository.findByProjectId(projectId)
                .orElseThrow(() -> new ProjectNotFoundException(projectId));

        List<FeatureItem> features = featureItemRepository.findByProject_ProjectId(projectId);

        LocalDate startDate = project.getStartDate() != null ? project.getStartDate() : LocalDate.now();
        scheduleFeatures(features, startDate, projectId);

    }

    @Override
    public void updateDailyStatus() {
        List<Project> activeProjects = projectRepository.findActiveProjects();

        for(Project project : activeProjects){
            // 지연된 기능들 일정 재생성
            handleOverdueFeatures(project.getProjectId());
            // 오늘 시작할 기능들 IN_PROGRESS로
            startTodayFeatures(project.getProjectId(), LocalDate.now());
        }
    }

    private void startTodayFeatures(Long projectId, LocalDate today) {
        List<FeatureItem> todayFeatures = featureItemRepository.findTodayFeatures(projectId, today);

        for(FeatureItem feature : todayFeatures){
            if("TODO".equals(feature.getStatus())){
                feature.setStatus("IN_PROGRESS");
                featureItemRepository.save(feature);
            }
        }
    }

    private void handleOverdueFeatures(Long projectId) {
        LocalDate yesterday = LocalDate.now().minusDays(1);

        List<FeatureItem> overdueFeatures = featureItemRepository.findOverdueFeatures(projectId, yesterday);

        if(!overdueFeatures.isEmpty()){
            for(FeatureItem feature : overdueFeatures){
                if(! "DONE".equals(feature.getStatus())){
                    feature.setStatus("TODO");
                    featureItemRepository.save(feature);
                }
            }
        }

        List<Long> featureIds = overdueFeatures.stream()
                .map(FeatureItem::getFeatureId)
                .collect(Collectors.toList());
        featureItemScheduleRepository.deleteByFeatureItemFeatureIdIn(featureIds);

        rescheduleProject(projectId);
    }



    private void scheduleFeatures(List<FeatureItem> features, LocalDate startDate, Long projectId) {
        Project project = projectRepository.findByProjectId(projectId)
                .orElseThrow(() -> new IllegalArgumentException("해당 프로젝트가 존재하지 않습니다."));

        LocalDate projectEndDate = project.getEndDate();
        Map<String, Integer> weeklySchedule = getWeeklyScheduleMap(projectId);
        
        PriorityQueue<FeatureItem> remainingFeatures = new PriorityQueue<>(
                Comparator.comparing(FeatureItem::getPriorityLevel, Comparator.nullsLast(Comparator.naturalOrder()))
        );
        remainingFeatures.addAll(features);
        
        List<FeatureItem> scheduledFeatures = new ArrayList<>();
        List<FeatureItem> unscheduledFeatures = new ArrayList<>();
        LocalDate currentDate = startDate;
        
        while(!remainingFeatures.isEmpty()){
            FeatureItem currentFeature = remainingFeatures.poll();

            double remainingHours = scheduleWithinProject(currentFeature, currentDate, weeklySchedule, projectId, projectEndDate);

            //remainingHours가 0이면 해당 작업의 estimatedTime만큼 다 배정이 된 것
            if(remainingHours == 0){
                scheduledFeatures.add(currentFeature);
            }
            //배정해야할 시간이 남았으면 남은 시간 저장 후 unscheduledFeatures에 추가
            else{
                currentFeature.setRemainingTime(remainingHours);
                unscheduledFeatures.add(currentFeature);
            }
        }
        //배정되지 않은 스케줄들이 있으면 요일별 작업 가능 시간 기반으로 스케줄링
        if(!unscheduledFeatures.isEmpty()){
            LocalDate extendedStartDate = projectEndDate.plusDays(1);
            scheduleWithWeeklyPattern(unscheduledFeatures, extendedStartDate, weeklySchedule, projectId);
        }
        
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
            // 해당 날짜의 요일별 작업 가능 시간 확인
            String dayName = currentDate.getDayOfWeek().toString();
            double availableHours = weeklySchedule.getOrDefault(dayName, 0);

            if (availableHours > 0) {
                // 해당 날짜에 이미 배정된 시간 확인
                double allocatedHours = getAllocatedHours(projectId, currentDate);
                double freeHours = Math.max(0, availableHours - allocatedHours);

                if (freeHours > 0) {
                    double hoursToSchedule = Math.min(remainingHours, freeHours);

                    // 스케줄 생성
                    FeatureItemSchedule schedule = FeatureItemSchedule.builder()
                            .featureItem(feature)
                            .scheduleDate(currentDate)
                            .allocatedHours(hoursToSchedule)
                            .withinProjectPeriod(false)
                            .build();

                    featureItemScheduleRepository.save(schedule);
                    remainingHours -= hoursToSchedule;

                    // 현재 날짜 작업이 완료되었고 시간이 남은 경우
                    if (remainingHours == 0) {
                        double totalUsed = allocatedHours + hoursToSchedule;
                        if (totalUsed < availableHours) {
                            return currentDate; // 같은 날 반환 (다음 기능도 같은 날 시작 가능)
                        } else {
                            return currentDate.plusDays(1);
                        }
                    }
                }
            }

            currentDate = currentDate.plusDays(1);
            dayCount++;
        }

        if (remainingHours > 0) {
            log.warn("스케줄링 미완료 - 기능 ID: {}, 남은 시간: {}h (최대 기간 초과)",
                    feature.getFeatureId(), remainingHours);
        }

        return currentDate;
    }

    private double scheduleWithinProject(FeatureItem feature, LocalDate startDate, Map<String, Integer> weeklySchedule, Long projectId, LocalDate projectEndDate) {
        double remainingHours = feature.getEstimatedTime();
        LocalDate currentDate = startDate;

        while(remainingHours > 0 && !currentDate.isAfter(projectEndDate)){
            double availableHours = getAvailableHours(projectId, currentDate, weeklySchedule);

            //availableHours > 0 이면 그 날에 작업 배정 가능
            if(availableHours > 0){
                // 해당 날짜에 이미 배정된 시간
                double allocatedHours = getAllocatedHours(projectId, currentDate);
                // 작업 배정 가능한 시간
                double freeHours = Math.max(0, availableHours - allocatedHours);

                if(freeHours > 0){
                    //작업하는 데 걸리는 시간과 작업 배정 가능 시간 중 작은 것을 골라 그 시간만큼 배정
                    double hoursToSchedule = Math.min(remainingHours, freeHours);

                    FeatureItemSchedule schedule = FeatureItemSchedule.builder()
                            .featureItem(feature)
                            .scheduleDate(currentDate)
                            .allocatedHours(hoursToSchedule)
                            .withinProjectPeriod(true)
                            .build();

                    featureItemScheduleRepository.save(schedule);
                    remainingHours -= hoursToSchedule;

                }
            }

            currentDate = currentDate.plusDays(1);
        }
        return remainingHours;
    }

    private int getAvailableHours(Long projectId, LocalDate date, Map<String, Integer> weeklySchedule) {
        // 해당 날짜가 작업 가능한 날인지 확인
        boolean isWorkingDay = scheduleRepository.existsByProject_ProjectIdAndScheduledDate(projectId, date);

        if (!isWorkingDay) {
            return 0; // 작업 불가능한 날
        }

        // 요일별 기본 작업 시간 반환
        String dayName = date.getDayOfWeek().toString();
        return weeklySchedule.getOrDefault(dayName, 0);
    }

    private double getAllocatedHours(Long projectId, LocalDate date) {
        List<FeatureItemSchedule> schedules = featureItemScheduleRepository
                .findByFeatureItem_Project_ProjectIdAndScheduleDate(projectId, date);

        return schedules.stream()
                .mapToDouble(FeatureItemSchedule::getAllocatedHours)
                .sum();
    }

    private Map<String, Integer> getWeeklyScheduleMap(Long projectId) {
        List<DaysOfWeek> daysOfWeekList = daysOfWeekRepository.findByProject_ProjectId(projectId);

        return daysOfWeekList.stream()
                .collect(Collectors.toMap(
                        DaysOfWeek::getDateName,
                        DaysOfWeek::getHours,
                        (existing, replacement) -> existing
                ));
    }

    private void deleteExistingSchedules(List<FeatureItem> features) {
        List<Long> featureIds = features.stream()
                .map(FeatureItem::getFeatureId)
                .collect(Collectors.toList());

        featureItemScheduleRepository.deleteByFeatureItemFeatureIdIn(featureIds);
        log.debug("기존 스케줄 삭제 완료 - 기능 수: {}", featureIds.size());
    }

    private List<FeatureItem> getSchedulableFeatures(Long projectId) {
        List<FeatureItem> allFeatures = featureItemRepository.findByProject_ProjectId(projectId);

        return allFeatures.stream()
                .filter(this::isSchedulable)
                .filter(feature -> feature.getEstimatedTime() != null && feature.getEstimatedTime() > 0)
                .collect(Collectors.toList());
    }

    private boolean isSchedulable(FeatureItem feature) {
        String status = feature.getStatus();

        if(status.equals("DONE") || status.equals("IN_PROGRESS"))
            return false;

        return true;
    }

    private FeatureItemResponse convertToResponseDto(FeatureItem feature) {
        return FeatureItemResponse.builder()
                .featureId(feature.getFeatureId())
                .title(feature.getTitle())
                .description(feature.getDescription())
                .field(feature.getField())
                .category(feature.getCategory())
                .status(feature.getStatus())
                .priorityLevel(feature.getPriorityLevel())
                .estimatedTime(feature.getEstimatedTime())
                .isSelected(feature.getIsSelected())
                .isCustom(feature.getIsCustom())
                .isReduced(feature.getIsReduced())
                .projectId(feature.getProject().getProjectId())
                .specificationId(feature.getSpecification() != null ? feature.getSpecification().getSpecId() : null)
                .parentFeatureId(feature.getParentFeature() != null ? feature.getParentFeature().getFeatureId() : null)
                .build();
    }
}
